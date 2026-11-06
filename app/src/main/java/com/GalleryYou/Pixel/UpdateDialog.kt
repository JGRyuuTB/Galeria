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
import androidx.compose.ui.res.stringResource
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
            title = { Text(stringResource(R.string.update_available_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.update_available_body, st.info.version, updater.currentVersion))
                    if (st.info.sizeBytes > 0) {
                        Text(
                            stringResource(R.string.update_size_mb, st.info.sizeBytes / 1_048_576.0),
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
                    Text(stringResource(R.string.update_download_install))
                }
            },
            dismissButton = { TextButton(onClick = { updater.dismiss() }) { Text(stringResource(R.string.update_not_now)) } },
        )

        is UpdateState.Downloading -> AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.update_downloading_title, st.info.version)) },
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
            title = { Text(stringResource(R.string.update_status_ready)) },
            text = {
                Text(
                    stringResource(R.string.update_ready_body),
                )
            },
            confirmButton = { TextButton(onClick = { updater.install() }) { Text(stringResource(R.string.update_install)) } },
            dismissButton = { TextButton(onClick = { updater.dismiss() }) { Text(stringResource(R.string.action_close)) } },
        )

        is UpdateState.Failed -> AlertDialog(
            onDismissRequest = { updater.dismiss() },
            title = { Text(stringResource(R.string.update_failed_title)) },
            text = { Text(st.message) },
            confirmButton = { TextButton(onClick = { updater.dismiss() }) { Text(stringResource(R.string.action_close)) } },
            dismissButton = {
                TextButton(onClick = {
                    updater.dismiss()
                    context.startActivity(
                        android.content.Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            android.net.Uri.fromParts("package", context.packageName, null),
                        ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }) { Text(stringResource(R.string.onboard_open_app_settings)) }
            },
        )

        UpdateState.UpToDate -> AlertDialog(
            onDismissRequest = { updater.dismiss() },
            title = { Text(stringResource(R.string.update_uptodate_title)) },
            text = { Text(stringResource(R.string.update_uptodate_body, updater.currentVersion)) },
            confirmButton = { TextButton(onClick = { updater.dismiss() }) { Text(stringResource(R.string.action_ok)) } },
        )

        UpdateState.Checking -> AlertDialog(
            onDismissRequest = { updater.dismiss() },
            title = { Text(stringResource(R.string.update_checking_title)) },
            text = { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) },
            confirmButton = {},
        )

        UpdateState.Idle -> Unit
    }
}
