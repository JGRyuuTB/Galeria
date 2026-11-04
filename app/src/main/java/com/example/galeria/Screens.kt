package com.example.galeria

import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest

val LocalGalleryLoader = staticCompositionLocalOf<ImageLoader> {
    error("ImageLoader no disponible")
}

/** Resorte suave cuando las animaciones están activas; cambio instantáneo si el usuario las desactiva. */
fun <T> motion(enabled: Boolean): AnimationSpec<T> =
    if (enabled) spring(stiffness = Spring.StiffnessMediumLow) else snap()

@Composable
fun thumbRequest(uri: Uri, size: Int = 320): ImageRequest {
    val context = LocalContext.current
    return remember(uri, size) {
        ImageRequest.Builder(context).data(uri).size(size).build()
    }
}

// ------------------------------------------------------------------ Permiso

@Composable
fun PermissionScreen(onRequest: () -> Unit, onSettings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(Modifier.height(24.dp))
        Text("Tu galería", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Permite el acceso a tus fotos y videos para verlos y reproducirlos aquí.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRequest) { Text("Permitir acceso") }
        TextButton(onClick = onSettings) { Text("Abrir ajustes") }
    }
}

@Composable
fun EmptyState(padding: PaddingValues, text: String = "No hay fotos ni videos") {
    Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ------------------------------------------------------------------ Filtros y barra de selección

@Composable
fun FilterRow(selected: Int, onSelect: (Int) -> Unit) {
    val labels = listOf("Todo", "Fotos", "Videos")
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEachIndexed { index, label ->
            FilterChip(
                selected = selected == index,
                onClick = { onSelect(index) },
                label = { Text(label) },
                leadingIcon = if (selected == index) {
                    { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else {
                    null
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopBar(
    count: Int,
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable RowScope.() -> Unit,
) {
    TopAppBar(
        title = { Text(if (count == 1) "1 seleccionado" else "$count seleccionados") },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Rounded.Close, contentDescription = "Cancelar selección")
            }
        },
        actions = {
            IconButton(onClick = onSelectAll) {
                Icon(Icons.Rounded.SelectAll, contentDescription = "Seleccionar todo")
            }
            actions()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            scrolledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        scrollBehavior = scrollBehavior,
    )
}

// ------------------------------------------------------------------ Pellizco para cambiar columnas

/**
 * Pellizcar hacia afuera = miniaturas más grandes (menos columnas); hacia adentro = más pequeñas.
 * Solo observa el gesto con dos dedos: el desplazamiento normal con un dedo no se ve afectado.
 */
@Composable
fun Modifier.pinchColumns(columns: Int, onChange: (Int) -> Unit): Modifier {
    val currentColumns by rememberUpdatedState(columns)
    val currentOnChange by rememberUpdatedState(onChange)
    return pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            var cols = currentColumns
            var acc = 1f
            do {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.changes.count { it.pressed } >= 2) {
                    acc *= event.calculateZoom()
                    if (acc > 1.2f && cols > 2) {
                        cols -= 1
                        currentOnChange(cols)
                        acc = 1f
                    } else if (acc < 0.83f && cols < 5) {
                        cols += 1
                        currentOnChange(cols)
                        acc = 1f
                    }
                    event.changes.forEach { it.consume() }
                }
            } while (event.changes.any { it.pressed })
        }
    }
}

// ------------------------------------------------------------------ Cuadrícula de fotos

@Composable
fun MediaGrid(
    items: List<MediaFile>,
    padding: PaddingValues,
    settings: AppSettings,
    selected: Set<Uri>,
    header: (@Composable () -> Unit)?,
    onClick: (MediaFile, Int) -> Unit,
    onLongClick: (MediaFile) -> Unit,
) {
    val groups = remember(items) {
        items.withIndex().groupBy { dayKey(it.value.dateMillis) }.values.toList()
    }
    val selectionMode = selected.isNotEmpty()
    val gap = settings.gap.dp

    LazyVerticalGrid(
        columns = GridCells.Fixed(settings.photoColumns),
        modifier = Modifier
            .fillMaxSize()
            .pinchColumns(settings.photoColumns) { settings.setPhotoColumns(it) },
        contentPadding = padding,
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        if (header != null) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "header") { header() }
        }
        groups.forEach { group ->
            val first = group.first()
            item(span = { GridItemSpan(maxLineSpan) }, key = "day-${first.index}") {
                Text(
                    dayLabel(first.value.dateMillis),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                )
            }
            items(group, key = { it.index }) { indexed ->
                Thumb(
                    m = indexed.value,
                    selectionMode = selectionMode,
                    isSelected = indexed.value.uri in selected,
                    settings = settings,
                    onClick = { onClick(indexed.value, indexed.index) },
                    onLongClick = { onLongClick(indexed.value) },
                    // Las miniaturas se deslizan a su nueva posición al cambiar de columnas.
                    modifier = if (settings.animations) Modifier.animateItem() else Modifier,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Thumb(
    m: MediaFile,
    selectionMode: Boolean,
    isSelected: Boolean,
    settings: AppSettings,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val anim = settings.animations

    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = if (anim) spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium) else snap(),
        label = "thumbPress",
    )
    val selectScale by animateFloatAsState(
        targetValue = if (isSelected) 0.86f else 1f,
        animationSpec = if (anim) spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow) else snap(),
        label = "thumbSelect",
    )
    val baseCorner = if (settings.roundedCorners) 12.dp else 4.dp
    val corner by animateDpAsState(
        targetValue = if (isSelected || pressed) 20.dp else baseCorner,
        animationSpec = motion(anim),
        label = "thumbCorner",
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                val s = pressScale * selectScale
                scaleX = s
                scaleY = s
            }
            .clip(RoundedCornerShape(corner))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            ),
    ) {
        // Los elementos de la papelera solo se pueden leer con la URI marcada como "incluir papelera".
        val thumbUri =
            if (m.expiresMillis > 0L && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                m.uri.buildUpon().appendQueryParameter("includeTrashed", "1").build()
            } else {
                m.uri
            }
        AsyncImage(
            model = thumbRequest(thumbUri),
            imageLoader = LocalGalleryLoader.current,
            contentDescription = m.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        if (m.isVideo) {
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = CircleShape,
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.White,
                    )
                    Text(
                        formatDuration(m.durationMs),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }

        if (m.expiresMillis > 0L) {
            val days = ((m.expiresMillis - System.currentTimeMillis()) / 86_400_000L).coerceAtLeast(0)
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = CircleShape,
                modifier = Modifier.align(Alignment.BottomStart).padding(6.dp),
            ) {
                Text(
                    "$days d",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }

        AnimatedVisibility(
            visible = selectionMode,
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
            enter = fadeIn() + scaleIn(initialScale = 0.4f),
            exit = fadeOut() + scaleOut(targetScale = 0.4f),
        ) {
            val primary = MaterialTheme.colorScheme.primary
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) primary else Color.Black.copy(alpha = 0.35f))
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn() + scaleIn(initialScale = 0.3f),
                    exit = fadeOut() + scaleOut(targetScale = 0.3f),
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------ Álbumes

@Composable
fun AlbumsGrid(
    albums: List<Album>,
    padding: PaddingValues,
    settings: AppSettings,
    onOpen: (Album) -> Unit,
) {
    if (albums.isEmpty()) {
        EmptyState(padding)
        return
    }
    val columns = settings.albumColumns
    val spacing = if (columns <= 3) 16.dp else 10.dp
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .fillMaxSize()
            .pinchColumns(columns) { settings.setAlbumColumns(it) },
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = padding.calculateTopPadding() + 8.dp,
            bottom = padding.calculateBottomPadding() + 16.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) {
        items(albums, key = { it.id }) { album ->
            AlbumCard(
                album = album,
                columns = columns,
                anim = settings.animations,
                modifier = if (settings.animations) Modifier.animateItem() else Modifier,
                onClick = { onOpen(album) },
            )
        }
    }
}

@Composable
fun AlbumCard(
    album: Album,
    columns: Int,
    anim: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = if (anim) spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium) else snap(),
        label = "albumScale",
    )
    val corner = when (columns) {
        2 -> 24.dp
        3 -> 18.dp
        else -> 12.dp
    }
    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
    ) {
        AsyncImage(
            model = thumbRequest(album.cover.uri, 480),
            imageLoader = LocalGalleryLoader.current,
            contentDescription = album.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(corner))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        )
        Text(
            album.name,
            style = if (columns >= 4) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
            maxLines = 1,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp),
        )
        Text(
            if (columns >= 4) "${album.count}" else "${album.count} elementos",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

// ------------------------------------------------------------------ Mover a otro álbum

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveSheet(
    albums: List<Album>,
    currentBuckets: Set<Long>,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var newAlbum by remember { mutableStateOf(false) }
    val targets = remember(albums, currentBuckets) {
        albums.filter { it.relativePath.isNotBlank() && it.id !in currentBuckets }
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            "Mover a álbum",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        LazyColumn(modifier = Modifier.navigationBarsPadding()) {
            item {
                ListItem(
                    headlineContent = { Text("Álbum nuevo") },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { newAlbum = true },
                )
            }
            items(targets, key = { it.id }) { album ->
                ListItem(
                    headlineContent = { Text(album.name, maxLines = 1) },
                    supportingContent = { Text("${album.count} elementos") },
                    leadingContent = {
                        AsyncImage(
                            model = thumbRequest(album.cover.uri, 160),
                            imageLoader = LocalGalleryLoader.current,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { onPick(album.relativePath) },
                )
            }
        }
    }
    if (newAlbum) {
        NewAlbumDialog(
            onConfirm = { name -> onPick("Pictures/$name/") },
            onDismiss = { newAlbum = false },
        )
    }
}

@Composable
private fun NewAlbumDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    val clean = name.trim().replace(Regex("[\\\\/:*?\"<>|]"), "")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Álbum nuevo") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("Nombre") },
            )
        },
        confirmButton = {
            TextButton(enabled = clean.isNotEmpty(), onClick = { onConfirm(clean) }) { Text("Mover") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
