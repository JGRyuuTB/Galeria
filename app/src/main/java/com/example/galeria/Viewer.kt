package com.example.galeria

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PhotoAlbum
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay

data class ViewerRequest(val items: List<MediaFile>, val start: Int)

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun share(context: Context, m: MediaFile) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = m.mime
        putExtra(Intent.EXTRA_STREAM, m.uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null))
}

// ------------------------------------------------------------------ Visor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(request: ViewerRequest, onClose: () -> Unit) {
    val context = LocalContext.current
    val items = request.items
    val pagerState = rememberPagerState(initialPage = request.start.coerceIn(0, items.lastIndex)) {
        items.size
    }
    var chrome by remember { mutableStateOf(true) }
    var zoomed by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    val current = items[pagerState.currentPage.coerceIn(0, items.lastIndex)]

    ImmersiveEffect(hideBars = !chrome)
    LaunchedEffect(pagerState.currentPage) { zoomed = false }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = !zoomed,
            modifier = Modifier.fillMaxSize(),
            key = { items[it].uri.toString() },
        ) { page ->
            val m = items[page]
            if (m.isVideo) {
                VideoPage(
                    m = m,
                    active = pagerState.settledPage == page,
                    chrome = chrome,
                    onTap = { chrome = !chrome },
                )
            } else {
                PhotoPage(
                    m = m,
                    onTap = { chrome = !chrome },
                    onZoomChange = { zoomed = it },
                )
            }
        }

        AnimatedVisibility(
            visible = chrome,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent)),
                    ),
            ) {
                Row(
                    modifier = Modifier.statusBarsPadding().padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val tint = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                    IconButton(onClick = onClose, colors = tint) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                    Column(Modifier.weight(1f).padding(start = 4.dp)) {
                        Text(
                            formatDate(current.dateMillis, "d MMM yyyy"),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            formatDate(current.dateMillis, "HH:mm"),
                            color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    IconButton(onClick = { share(context, current) }, colors = tint) {
                        Icon(Icons.Rounded.Share, contentDescription = "Compartir")
                    }
                    IconButton(onClick = { showInfo = true }, colors = tint) {
                        Icon(Icons.Rounded.Info, contentDescription = "Detalles")
                    }
                }
            }
        }
    }

    if (showInfo) {
        InfoSheet(current) { showInfo = false }
    }
}

@Composable
private fun ImmersiveEffect(hideBars: Boolean) {
    val context = LocalContext.current
    val view = LocalView.current
    val window = context.findActivity()?.window

    LaunchedEffect(hideBars, window) {
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (hideBars) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    DisposableEffect(window) {
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val prevStatus = controller?.isAppearanceLightStatusBars
        val prevNav = controller?.isAppearanceLightNavigationBars
        // Iconos claros sobre el fondo negro del visor.
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            if (controller != null) {
                controller.show(WindowInsetsCompat.Type.systemBars())
                if (prevStatus != null) controller.isAppearanceLightStatusBars = prevStatus
                if (prevNav != null) controller.isAppearanceLightNavigationBars = prevNav
            }
        }
    }
}

// ------------------------------------------------------------------ Foto con zoom

@Composable
private fun PhotoPage(m: MediaFile, onTap: () -> Unit, onZoomChange: (Boolean) -> Unit) {
    val context = LocalContext.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var box by remember { mutableStateOf(IntSize.Zero) }
    val zoomed = scale > 1.01f
    LaunchedEffect(zoomed) { onZoomChange(zoomed) }

    fun clampOffset(o: Offset, s: Float): Offset {
        val maxX = box.width * (s - 1f) / 2f
        val maxY = box.height * (s - 1f) / 2f
        return Offset(o.x.coerceIn(-maxX, maxX), o.y.coerceIn(-maxY, maxY))
    }

    val request = remember(m.uri) { ImageRequest.Builder(context).data(m.uri).build() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { box = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = {
                        if (scale > 1.01f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                        }
                    },
                )
            }
            .pointerInput(Unit) {
                // Pellizcar para ampliar; arrastrar solo cuando hay zoom (así no bloquea el deslizado entre fotos).
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val consumed = event.changes.any { it.isConsumed }
                        if (!consumed) {
                            val multi = event.changes.count { it.pressed } > 1
                            if (multi || scale > 1.01f) {
                                val zoom = event.calculateZoom()
                                val pan = event.calculatePan()
                                if (zoom != 1f || pan != Offset.Zero) {
                                    val newScale = (scale * zoom).coerceIn(1f, 6f)
                                    scale = newScale
                                    offset = if (newScale <= 1.01f) {
                                        Offset.Zero
                                    } else {
                                        clampOffset(offset + pan, newScale)
                                    }
                                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                                }
                            }
                        }
                    } while (!consumed && event.changes.any { it.pressed })
                }
            },
    ) {
        AsyncImage(
            model = request,
            imageLoader = LocalGalleryLoader.current,
            contentDescription = m.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
        )
    }
}

// ------------------------------------------------------------------ Video

@Composable
private fun VideoPage(m: MediaFile, active: Boolean, chrome: Boolean, onTap: () -> Unit) {
    if (active) {
        VideoPlayerContent(m, chrome, onTap)
    } else {
        // Mientras se desliza entre páginas solo se muestra la portada; el reproductor vive en la página activa.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onTap,
                ),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = thumbRequest(m.uri, 1080),
                imageLoader = LocalGalleryLoader.current,
                contentDescription = m.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier.size(64.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.PlayArrow, null, Modifier.size(40.dp), tint = Color.White)
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoPlayerContent(m: MediaFile, chrome: Boolean, onTap: () -> Unit) {
    val context = LocalContext.current
    val exo = remember(m.uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(m.uri))
            prepare()
            playWhenReady = true
        }
    }
    var isPlaying by remember { mutableStateOf(true) }
    var ended by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(m.durationMs) }
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    var speed by remember { mutableFloatStateOf(1f) }
    var muted by remember { mutableStateOf(false) }
    var speedMenu by remember { mutableStateOf(false) }

    DisposableEffect(exo) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                ended = state == Player.STATE_ENDED
                if (state == Player.STATE_READY && exo.duration > 0) duration = exo.duration
            }

            override fun onPlayerError(error: PlaybackException) {
                failed = true
            }
        }
        exo.addListener(listener)
        onDispose {
            exo.removeListener(listener)
            exo.release()
        }
    }

    // Pausa el video si la app pasa a segundo plano.
    val owner = remember { context.findActivity() as? LifecycleOwner }
    DisposableEffect(owner, exo) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) exo.pause()
        }
        owner?.lifecycle?.addObserver(observer)
        onDispose { owner?.lifecycle?.removeObserver(observer) }
    }

    LaunchedEffect(exo) {
        while (true) {
            if (dragFraction == null) position = exo.currentPosition.coerceAtLeast(0L)
            delay(200)
        }
    }

    fun togglePlay() {
        if (exo.isPlaying) {
            exo.pause()
        } else {
            if (ended || exo.playbackState == Player.STATE_ENDED) exo.seekTo(0)
            exo.play()
        }
    }

    fun seekBy(deltaMs: Long) {
        val target = exo.currentPosition + deltaMs
        val max = if (duration > 0) duration else Long.MAX_VALUE
        exo.seekTo(target.coerceIn(0L, max))
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    keepScreenOn = true
                    setPlayer(exo)
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        // Toque simple: mostrar/ocultar controles. Doble toque: ±10 s en los lados, pausa en el centro.
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onTap() },
                        onDoubleTap = { pos ->
                            val w = size.width
                            when {
                                pos.x < w / 3f -> seekBy(-10_000)
                                pos.x > w * 2f / 3f -> seekBy(10_000)
                                else -> togglePlay()
                            }
                        },
                    )
                },
        )

        if (failed) {
            Text(
                "No se puede reproducir este video",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        AnimatedVisibility(
            visible = chrome,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
        ) {
            val shownPosition = dragFraction?.let { (it * duration).toLong() } ?: position
            val fraction = dragFraction
                ?: if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f

            Column(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))),
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Slider(
                    value = fraction,
                    onValueChange = { dragFraction = it },
                    onValueChangeFinished = {
                        dragFraction?.let { f -> exo.seekTo((f * duration).toLong()) }
                        dragFraction = null
                    },
                    valueRange = 0f..1f,
                )
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        formatDuration(shownPosition),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        formatDuration(duration),
                        color = Color.White.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box {
                        TextButton(
                            onClick = { speedMenu = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                        ) {
                            Icon(Icons.Rounded.Speed, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(speedLabel(speed))
                        }
                        DropdownMenu(expanded = speedMenu, onDismissRequest = { speedMenu = false }) {
                            listOf(0.5f, 1f, 1.5f, 2f).forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(speedLabel(s)) },
                                    onClick = {
                                        speed = s
                                        exo.setPlaybackSpeed(s)
                                        speedMenu = false
                                    },
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val tint = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                        IconButton(onClick = { seekBy(-10_000) }, colors = tint) {
                            Icon(Icons.Rounded.Replay10, contentDescription = "Retroceder 10 segundos")
                        }
                        PlayPauseButton(isPlaying = isPlaying, ended = ended, onClick = { togglePlay() })
                        IconButton(onClick = { seekBy(10_000) }, colors = tint) {
                            Icon(Icons.Rounded.Forward10, contentDescription = "Avanzar 10 segundos")
                        }
                    }

                    IconButton(
                        onClick = {
                            muted = !muted
                            exo.volume = if (muted) 0f else 1f
                        },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
                    ) {
                        Icon(
                            if (muted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
                            contentDescription = if (muted) "Activar sonido" else "Silenciar",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayPauseButton(isPlaying: Boolean, ended: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.85f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "playScale",
    )
    FilledIconButton(
        onClick = onClick,
        interactionSource = interaction,
        modifier = Modifier
            .size(64.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
    ) {
        val icon = when {
            ended -> Icons.Rounded.Replay
            isPlaying -> Icons.Rounded.Pause
            else -> Icons.Rounded.PlayArrow
        }
        Icon(icon, contentDescription = null, modifier = Modifier.size(36.dp))
    }
}

private fun speedLabel(s: Float): String =
    if (s % 1f == 0f) "${s.toInt()}x" else "${s}x"

// ------------------------------------------------------------------ Detalles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoSheet(m: MediaFile, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.navigationBarsPadding().padding(bottom = 16.dp)) {
            Text(
                "Detalles",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            InfoRow(
                icon = { Icon(Icons.Rounded.CalendarToday, contentDescription = null) },
                title = formatDate(m.dateMillis, "EEEE, d 'de' MMMM yyyy"),
                subtitle = formatDate(m.dateMillis, "HH:mm"),
            )
            InfoRow(
                icon = {
                    Icon(
                        if (m.isVideo) Icons.Rounded.Videocam else Icons.Rounded.Image,
                        contentDescription = null,
                    )
                },
                title = m.name,
                subtitle = "${formatSize(m.size)} · ${m.mime}",
            )
            if (m.width > 0 && m.height > 0) {
                val mp = m.width.toLong() * m.height / 1_000_000.0
                InfoRow(
                    icon = { Icon(Icons.Rounded.AspectRatio, contentDescription = null) },
                    title = "${m.width} × ${m.height}",
                    subtitle = "%.1f MP".format(mp),
                )
            }
            if (m.isVideo) {
                InfoRow(
                    icon = { Icon(Icons.Rounded.Timer, contentDescription = null) },
                    title = formatDuration(m.durationMs),
                    subtitle = "Duración",
                )
            }
            if (m.bucket.isNotBlank()) {
                InfoRow(
                    icon = { Icon(Icons.Rounded.PhotoAlbum, contentDescription = null) },
                    title = m.bucket,
                    subtitle = "Álbum",
                )
            }
        }
    }
}

@Composable
private fun InfoRow(icon: @Composable () -> Unit, title: String, subtitle: String) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = icon,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
