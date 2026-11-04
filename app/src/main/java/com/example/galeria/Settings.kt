package com.example.galeria

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Ajustes de la interfaz, guardados en SharedPreferences y observables desde Compose. */
class AppSettings(context: Context) {
    private val prefs = context.getSharedPreferences("galeria_settings", Context.MODE_PRIVATE)

    var photoColumns by mutableIntStateOf(prefs.getInt("photoColumns", 3).coerceIn(2, 5))
        private set
    var albumColumns by mutableIntStateOf(prefs.getInt("albumColumns", 2).coerceIn(2, 5))
        private set

    /** 0 = sistema, 1 = claro, 2 = oscuro */
    var themeMode by mutableIntStateOf(prefs.getInt("themeMode", 0))
        private set
    var dynamicColor by mutableStateOf(prefs.getBoolean("dynamicColor", true))
        private set
    var animations by mutableStateOf(prefs.getBoolean("animations", true))
        private set

    /** Espacio entre miniaturas en dp. */
    var gap by mutableIntStateOf(prefs.getInt("gap", 2))
        private set
    var roundedCorners by mutableStateOf(prefs.getBoolean("roundedCorners", false))
        private set
    var autoplay by mutableStateOf(prefs.getBoolean("autoplay", true))
        private set

    /** 0 = sin borde, 1 = color del tema, 2 = animado */
    var playerBorder by mutableIntStateOf(prefs.getInt("playerBorder", 2))
        private set
    var materialYouIcon by mutableStateOf(prefs.getBoolean("materialYouIcon", true))
        private set

    fun setPhotoColumns(value: Int) {
        photoColumns = value.coerceIn(2, 5)
        prefs.edit().putInt("photoColumns", photoColumns).apply()
    }

    fun setAlbumColumns(value: Int) {
        albumColumns = value.coerceIn(2, 5)
        prefs.edit().putInt("albumColumns", albumColumns).apply()
    }

    fun setThemeMode(value: Int) {
        themeMode = value
        prefs.edit().putInt("themeMode", value).apply()
    }

    fun setDynamicColor(value: Boolean) {
        dynamicColor = value
        prefs.edit().putBoolean("dynamicColor", value).apply()
    }

    fun setAnimations(value: Boolean) {
        animations = value
        prefs.edit().putBoolean("animations", value).apply()
    }

    fun setGap(value: Int) {
        gap = value
        prefs.edit().putInt("gap", value).apply()
    }

    fun setRoundedCorners(value: Boolean) {
        roundedCorners = value
        prefs.edit().putBoolean("roundedCorners", value).apply()
    }

    fun setAutoplay(value: Boolean) {
        autoplay = value
        prefs.edit().putBoolean("autoplay", value).apply()
    }

    fun setPlayerBorder(value: Int) {
        playerBorder = value
        prefs.edit().putInt("playerBorder", value).apply()
    }

    fun setMaterialYouIcon(value: Boolean) {
        materialYouIcon = value
        prefs.edit().putBoolean("materialYouIcon", value).apply()
    }
}

/** Activa el alias del icono elegido y desactiva el otro (ver AndroidManifest). */
fun applyAppIcon(context: Context, materialYou: Boolean) {
    val pm = context.packageManager
    val you = ComponentName(context.packageName, "com.example.galeria.AliasMaterialYou")
    val default = ComponentName(context.packageName, "com.example.galeria.AliasDefault")
    val enable = if (materialYou) you else default
    val disable = if (materialYou) default else you
    pm.setComponentEnabledSetting(
        enable,
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP,
    )
    pm.setComponentEnabledSetting(
        disable,
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP,
    )
}

internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
