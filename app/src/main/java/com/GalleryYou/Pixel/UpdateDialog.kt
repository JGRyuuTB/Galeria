package com.GalleryYou.Pixel

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/** Diálogo de actualizaciones: disponible, descargando, lista para instalar, al día o error. */
@Composable
fun UpdateDialog(updater: Updater) {
    if (!updater.dialogVisible) return
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    when (val st = updater.state) {
        is UpdateState.Available -> AlertDialog(
            onDismissRequest = { updater.dismiss() },
            title = { Text("Actualización disponible") },
            text = {
                Column {
                    Text("Versión ${st.info.version} (tienes la ${updater.currentVersion}).")
                    if (st.info.sizeBytes > 0) {
                        Text(
                            "Tamaño: %.1f MB".format(st.info.sizeBytes / 1_048_576.0),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (st.info.notes.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            st.info.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .verticalScroll(rememberScrollState()),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { scope.launch { updater.downloadAndInstall() } }) {
                    Text("Descargar e instalar")
                }
            },
            dismissButton = { TextButton(onClick = { updater.dismiss() }) { Text("Ahora no") } },
        )

        is UpdateState.Downloading -> AlertDialog(
            onDismissRequest = {},
            title = { Text("Descargando ${st.info.version}") },
            text = {
                Column {
                    LinearProgressIndicator(
                        progress = { st.progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("${(st.progress * 100).toInt()} %", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {},
        )

        is UpdateState.Ready -> AlertDialog(
            onDismissRequest = { updater.dismiss() },
            title = { Text("Lista para instalar") },
            text = {
                Text(
                    "Si Android te pide permiso para instalar desde esta app, actívalo, vuelve y pulsa Instalar.",
                )
            },
            confirmButton = { TextButton(onClick = { updater.install() }) { Text("Instalar") } },
            dismissButton = { TextButton(onClick = { updater.dismiss() }) { Text("Cerrar") } },
        )

        is UpdateState.Failed -> AlertDialog(
            onDismissRequest = { updater.dismiss() },
            title = { Text("No se pudo actualizar") },
            text = { Text(st.message) },
            confirmButton = { TextButton(onClick = { updater.dismiss() }) { Text("Cerrar") } },
            dismissButton = {
                TextButton(onClick = {
                    updater.dismiss()
                    context.startActivity(
                        android.content.Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            android.net.Uri.fromParts("package", context.packageName, null),
                        ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }) { Text("Ajustes de la app") }
            },
        )

        UpdateState.UpToDate -> AlertDialog(
            onDismissRequest = { updater.dismiss() },
            title = { Text("Estás al día") },
            text = { Text("Tienes la última versión (${updater.currentVersion}).") },
            confirmButton = { TextButton(onClick = { updater.dismiss() }) { Text("Aceptar") } },
        )

        UpdateState.Checking -> AlertDialog(
            onDismissRequest = { updater.dismiss() },
            title = { Text("Buscando actualizaciones") },
            text = { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) },
            confirmButton = {},
        )

        UpdateState.Idle -> Unit
    }
}
