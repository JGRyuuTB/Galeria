package com.GalleryYou.Pixel

import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// ------------------------------------------------------------------ Ajustes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(settings: AppSettings, onBack: () -> Unit) {
    val context = LocalContext.current
    val canDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val gapValues = listOf(0, 2, 6)
    var showAbout by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = showAbout) { showAbout = false }
    if (showAbout) {
        AboutScreen(onBack = { showAbout = false })
        return
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            SettingsGroup("Apariencia") {
                ChoiceRow(
                    title = "Tema",
                    options = listOf("Sistema", "Claro", "Oscuro"),
                    selected = settings.themeMode,
                    onSelect = { settings.updateThemeMode(it) },
                )
                SwitchRow(
                    title = "Colores dinámicos",
                    subtitle = if (canDynamic) {
                        "Usa los colores de tu fondo de pantalla (Material You)"
                    } else {
                        "Requiere Android 12 o superior"
                    },
                    checked = settings.dynamicColor && canDynamic,
                    enabled = canDynamic,
                    onChange = { settings.updateDynamicColor(it) },
                )
                val canBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ChoiceRow(
                    title = "Desenfoque de la interfaz",
                    subtitle = if (canBlur) {
                        "Efecto de cristal en las barras (puede usar más batería)"
                    } else {
                        "Requiere Android 12 o superior"
                    },
                    options = listOf("Ninguno", "Suave", "Fuerte"),
                    selected = if (canBlur) settings.blur else 0,
                    onSelect = { if (canBlur) settings.updateBlur(it) },
                )
                SwitchRow(
                    title = "Animaciones",
                    subtitle = "Transiciones y efectos de movimiento",
                    checked = settings.animations,
                    onChange = { settings.updateAnimations(it) },
                )
            }

            SettingsGroup("Fotos y álbumes") {
                ChoiceRow(
                    title = "Columnas en Fotos",
                    subtitle = "También puedes pellizcar la pantalla para cambiarlas",
                    options = listOf("2", "3", "4", "5"),
                    selected = settings.photoColumns - 2,
                    onSelect = { settings.updatePhotoColumns(it + 2) },
                )
                ChoiceRow(
                    title = "Columnas en Álbumes",
                    options = listOf("2", "3", "4", "5"),
                    selected = settings.albumColumns - 2,
                    onSelect = { settings.updateAlbumColumns(it + 2) },
                )
                ChoiceRow(
                    title = "Espacio entre miniaturas",
                    options = listOf("Ninguno", "Pequeño", "Grande"),
                    selected = gapValues.indexOf(settings.gap).coerceAtLeast(0),
                    onSelect = { settings.updateGap(gapValues[it]) },
                )
                SwitchRow(
                    title = "Esquinas redondeadas",
                    subtitle = "Miniaturas con las esquinas más suaves",
                    checked = settings.roundedCorners,
                    onChange = { settings.updateRoundedCorners(it) },
                )
            }

            SettingsGroup("Reproductor") {
                ChoiceRow(
                    title = "Borde del panel del reproductor",
                    options = listOf("Ninguno", "Tema", "Animado"),
                    selected = settings.playerBorder,
                    onSelect = { settings.updatePlayerBorder(it) },
                )
                SwitchRow(
                    title = "Reproducir al abrir",
                    subtitle = "Los videos empiezan solos",
                    checked = settings.autoplay,
                    onChange = { settings.updateAutoplay(it) },
                )
            }

            SettingsGroup("Icono de la aplicación") {
                ChoiceRow(
                    title = "Icono",
                    subtitle = "Material You toma los colores de tu fondo de pantalla y, en Android 13+, " +
                        "admite iconos temáticos. Puede tardar unos segundos en cambiar en el launcher.",
                    options = listOf("Material You", "Por defecto"),
                    selected = if (settings.materialYouIcon) 0 else 1,
                    onSelect = {
                        val you = it == 0
                        settings.updateMaterialYouIcon(you)
                        applyAppIcon(context, you)
                    },
                )
            }

            SettingsGroup("Acerca de") {
                ListItem(
                    headlineContent = { Text("Galería") },
                    supportingContent = { Text("Versión 1.0.0 · Estable") },
                    leadingContent = { Icon(AppIcons.Info, contentDescription = null) },
                    trailingContent = { Icon(AppIcons.ArrowForward, contentDescription = null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { showAbout = true },
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
        )
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp), content = content)
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(enabled = enabled) { onChange(!checked) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChoiceRow(
    title: String,
    options: List<String>,
    selected: Int,
    subtitle: String? = null,
    onSelect: (Int) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    selected = index == selected,
                    onClick = { onSelect(index) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    icon = {},
                    label = { Text(label) },
                )
            }
        }
    }
}

// ------------------------------------------------------------------ Papelera

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    items: List<MediaFile>,
    settings: AppSettings,
    onBack: () -> Unit,
    onRestore: (List<MediaFile>) -> Unit,
    onDeleteForever: (List<MediaFile>) -> Unit,
) {
    var selected by remember { mutableStateOf<Set<Uri>>(emptySet()) }
    var confirm by remember { mutableStateOf<List<MediaFile>?>(null) }
    val supported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    val picked = items.filter { it.uri in selected }

    BackHandler(enabled = selected.isNotEmpty()) { selected = emptySet() }

    fun toggle(m: MediaFile) {
        selected = if (m.uri in selected) selected - m.uri else selected + m.uri
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (selected.isNotEmpty()) {
                SelectionTopBar(
                    count = selected.size,
                    onClose = { selected = emptySet() },
                    onSelectAll = { selected = items.map { it.uri }.toSet() },
                ) {
                    IconButton(onClick = {
                        onRestore(picked)
                        selected = emptySet()
                    }) {
                        Icon(AppIcons.TrashRestore, contentDescription = "Restaurar")
                    }
                    IconButton(onClick = { confirm = picked }) {
                        Icon(AppIcons.TrashX, contentDescription = "Eliminar definitivamente")
                    }
                }
            } else {
                TopAppBar(
                    title = { Text("Papelera") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(AppIcons.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                        if (items.isNotEmpty()) {
                            IconButton(onClick = { confirm = items }) {
                                Icon(AppIcons.Trash, contentDescription = "Vaciar papelera")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            }
        },
    ) { padding ->
        val gridPadding = PaddingValues(
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding(),
        )
        when {
            !supported -> EmptyState(gridPadding, "La papelera requiere Android 11 o superior")
            items.isEmpty() -> EmptyState(gridPadding, "La papelera está vacía")
            else -> MediaGrid(
                items = items,
                padding = gridPadding,
                settings = settings,
                selected = selected,
                header = {
                    Text(
                        "Los elementos de la papelera se eliminan definitivamente pasados 30 días.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                },
                onClick = { m, _ -> toggle(m) },
                onLongClick = { m -> toggle(m) },
            )
        }
    }

    val toDelete = confirm
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { confirm = null },
            title = { Text("Eliminar definitivamente") },
            text = {
                Text(
                    if (toDelete.size == 1) {
                        "Se eliminará 1 elemento. Esta acción no se puede deshacer."
                    } else {
                        "Se eliminarán ${toDelete.size} elementos. Esta acción no se puede deshacer."
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteForever(toDelete)
                    confirm = null
                    selected = emptySet()
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { confirm = null }) { Text("Cancelar") } },
        )
    }
}
