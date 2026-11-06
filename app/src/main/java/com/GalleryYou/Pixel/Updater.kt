package com.GalleryYou.Pixel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** Repositorio de donde salen las versiones (GitHub Releases). */
object UpdateConfig {
    const val OWNER = "JGRyuuTB"
    const val REPO = "Pixel-Gallery"
}

data class UpdateInfo(
    val version: String,
    val notes: String,
    val apkUrl: String,
    val sizeBytes: Long,
)

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data class Available(val info: UpdateInfo) : UpdateState
    data class Downloading(val info: UpdateInfo, val progress: Float) : UpdateState
    data class Ready(val info: UpdateInfo, val file: File) : UpdateState
    data class Failed(val message: String) : UpdateState
}

/** Versión instalada, tal como está en el APK (por ejemplo "1.1.0"). */
fun appVersion(context: Context): String = try {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
} catch (e: Exception) {
    "1.0.0"
}

/**
 * Actualizaciones OTA: consulta la última Release de GitHub, descarga el APK y lanza el
 * instalador de Android. Como todas las versiones se firman con la misma clave, se instala
 * encima de la actual conservando los ajustes.
 */
class Updater(context: Context) {
    private val appContext = context.applicationContext

    var state: UpdateState by mutableStateOf(UpdateState.Idle)
        private set

    /** Si el diálogo de actualización está abierto. */
    var dialogVisible by mutableStateOf(false)

    val currentVersion: String = appVersion(appContext)

    /** Busca una versión nueva. Con [manual] el resultado (al día o error) también se muestra. */
    suspend fun check(manual: Boolean) {
        if (state is UpdateState.Checking || state is UpdateState.Downloading) return
        state = UpdateState.Checking
        if (manual) dialogVisible = true
        withContext(Dispatchers.IO) {
            try {
                val url = "https://api.github.com/repos/${UpdateConfig.OWNER}/${UpdateConfig.REPO}/releases/latest"
                val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10_000
                    readTimeout = 10_000
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("User-Agent", "PixelGalleryYou")
                }
                if (conn.responseCode == 404) {
                    // Todavía no hay ninguna versión publicada.
                    state = UpdateState.UpToDate
                    return@withContext
                }
                if (conn.responseCode != 200) throw IllegalStateException("HTTP ${conn.responseCode}")
                val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                val remote = json.getString("tag_name").trim().trimStart('v', 'V')
                if (!isNewer(remote, currentVersion)) {
                    state = UpdateState.UpToDate
                    return@withContext
                }
                val assets = json.getJSONArray("assets")
                var apkUrl: String? = null
                var size = 0L
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.getString("name").endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.getString("browser_download_url")
                        size = asset.optLong("size", 0L)
                        break
                    }
                }
                if (apkUrl == null) {
                    state = UpdateState.UpToDate
                    return@withContext
                }
                state = UpdateState.Available(
                    UpdateInfo(remote, json.optString("body", "").trim(), apkUrl, size),
                )
                dialogVisible = true
            } catch (e: Exception) {
                state = if (manual) {
                    UpdateState.Failed("No se pudo comprobar. Revisa tu conexión a internet.")
                } else {
                    UpdateState.Idle
                }
            }
        }
    }

    /** Descarga el APK de la versión disponible y abre el instalador. */
    suspend fun downloadAndInstall() {
        val info = (state as? UpdateState.Available)?.info ?: return
        state = UpdateState.Downloading(info, 0f)
        val file = withContext(Dispatchers.IO) {
            try {
                val dir = File(appContext.cacheDir, "updates").apply { mkdirs() }
                val target = File(dir, "update.apk")
                val conn = (URL(info.apkUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15_000
                    readTimeout = 30_000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "PixelGalleryYou")
                }
                if (conn.responseCode != 200) throw IllegalStateException("HTTP ${conn.responseCode}")
                val total = conn.contentLengthLong.takeIf { it > 0 } ?: info.sizeBytes
                conn.inputStream.use { input ->
                    target.outputStream().use { out ->
                        val buffer = ByteArray(64 * 1024)
                        var done = 0L
                        var lastReported = -1
                        while (true) {
                            val n = input.read(buffer)
                            if (n < 0) break
                            out.write(buffer, 0, n)
                            done += n
                            if (total > 0) {
                                val pct = (done * 100 / total).toInt()
                                if (pct != lastReported) {
                                    lastReported = pct
                                    state = UpdateState.Downloading(info, done.toFloat() / total)
                                }
                            }
                        }
                    }
                }
                target
            } catch (e: Exception) {
                null
            }
        }
        if (file == null) {
            state = UpdateState.Failed("Falló la descarga. Inténtalo de nuevo.")
            return
        }
        state = UpdateState.Ready(info, file)
        install()
    }

    /** Abre el instalador del sistema con el APK descargado. */
    fun install() {
        val ready = state as? UpdateState.Ready ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !appContext.packageManager.canRequestPackageInstalls()
        ) {
            // Android pide permitir instalar desde esta app; al volver se pulsa "Instalar" otra vez.
            appContext.startActivity(
                Intent(
                    android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${appContext.packageName}"),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            return
        }
        val uri = FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", ready.file)
        appContext.startActivity(
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION),
        )
    }

    fun dismiss() {
        dialogVisible = false
        if (state !is UpdateState.Downloading) {
            if (state is UpdateState.UpToDate || state is UpdateState.Failed) state = UpdateState.Idle
        }
    }

    companion object {
        fun isNewer(remote: String, local: String): Boolean {
            fun parts(v: String) = v.trim().trimStart('v', 'V').split('.', '-').mapNotNull { it.toIntOrNull() }
            val r = parts(remote)
            val l = parts(local)
            for (i in 0 until maxOf(r.size, l.size)) {
                val a = r.getOrElse(i) { 0 }
                val b = l.getOrElse(i) { 0 }
                if (a != b) return a > b
            }
            return false
        }
    }
}
