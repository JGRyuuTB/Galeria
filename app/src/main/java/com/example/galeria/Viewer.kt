package com.example.galeria

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Delete
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
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
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay

data class ViewerRequest(val items: List<MediaFile>, val start: Int)

private fun share(context: Context, m: MediaFile) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = m.mime
        putExtra(Intent.EXTRA_STREAM, m.uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null))
}

private fun initialAspect(m: MediaFile): Float =
    if (m.width > 0 && m.height > 0) m.width.toFloat() / m.height else 16f / 9f

// ------------------------------------------------------------------ Visor

@Composable
fun ViewerScreen(
    request: ViewerRequest,
    settings: AppSettings,
    onClose: () -> Unit,
    onTrash: (MediaFile) -> Unit,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val items = request.items
    val pagerState = rememberPagerState(initialPage = request.start.coerceIn(0, items.lastIndex)) {
        items.size
    }
    var chrome by remember { mutableStateOf(true) }
    var zoomed by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    val current = items[pagerState.currentPage.coerceIn(0, items.lastIndex)]
    val darkIcons = colors.surface.luminance() > 0.5f

    ImmersiveEffect(hideBars = !chrome, darkIcons = darkIcons)
    LaunchedEffect(pagerState.currentPage) { zoomed = false }

    CompositionLocalProvider(LocalContentColor provides colors.onSurface) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(colors.surfaceContainerHigh, colors.surface))),
        ) {
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
                        settings = settings,
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
                enter = fadeIn() + slideInVertically { -it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                    Column(Modifier.weight(1f).padding(start = 4.dp)) {
                        Text(
                            formatDate(current.dateMillis, "d MMM yyyy"),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            formatDate(current.dateMillis, "HH:mm"),
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    IconButton(onClick = { share(context, current) }) {
                        Icon(Icons.Rounded.Share, contentDescription = "Compartir")
                    }
                    IconButton(onClick = { onTrash(current) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Mover a la papelera")
                    }
                    IconButton(onClick = { showInfo = true }) {
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
private fun ImmersiveEffect(hideBars: Boolean, darkIcons: Boolean) {
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

    DisposableEffect(window, darkIcons) {
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val prevStatus = controller?.isAppearanceLightStatusBars
        val prevNav = controller?.isAppearanceLightNavigationBars
        // Iconos de las barras que contrastan con el fondo del visor.
        controller?.isAppearanceLightStatusBars = darkIcons
        controller?.isAppearanceLightNavigationBars = darkIcons
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

// ------------------------------------------------------------------ Marco del reproductor

/** Marco redondeado con sombra y borde (del color del tema, fijo o animado) alrededor del video. */
@Composable
private fun PlayerFrame(
    width: Dp,
    height: Dp,
    mode: Int,
    animate: Boolean,
    scale: Float,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(28.dp)
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val shift: State<Float>? = if (mode == 2 && animate) {
        rememberInfiniteTransition(label = "frameBorder").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing), RepeatMode.Reverse),
            label = "frameShift",
        )
    } else {
        null
    }

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = ((scale - 0.85f) / 0.15f).coerceIn(0f, 1f)
            }
            .size(width, height)
            .shadow(16.dp, shape)
            .drawWithContent {
                drawContent()
                if (mode != 0) {
                    // El valor animado se lee al dibujar, sin recomponer la pantalla en cada fotograma.
                    val s = shift?.value ?: 0f
                    val brush = Brush.linearGradient(
                        listOf(lerp(primary, tertiary, s), lerp(tertiary, primary, s)),
                    )
                    val stroke = 3.dp.toPx()
                    drawRoundRect(
                        brush = brush,
                        topLeft = Offset(stroke / 2f, stroke / 2f),
                        size = Size(size.width - stroke, size.height - stroke),
                        cornerRadius = CornerRadius(28.dp.toPx() - stroke / 2f),
                        style = Stroke(width = stroke),
                    )
                }
            },
    ) {
        content()
    }
}

/** Reserva espacio para la barra superior y el panel de controles y ajusta el marco al video. */
@Composable
private fun VideoStage(
    aspect: Float,
    chrome: Boolean,
    settings: AppSettings,
    borderActive: Boolean,
    content: @Composable () -> Unit,
) {
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val topPad by animateDpAsState(
        targetValue = if (chrome) statusTop + 72.dp else 24.dp,
        animationSpec = motion(settings.animations),
        label = "stageTop",
    )
    val bottomPad by animateDpAsState(
        targetValue = if (chrome) navBottom + 184.dp else 24.dp,
        animationSpec = motion(settings.animations),
        label = "stageBottom",
    )
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val appear by animateFloatAsState(
        targetValue = if (shown || !settings.animations) 1f else 0.9f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "stageAppear",
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = topPad, bottom = bottomPad),
        contentAlignment = Alignment.Center,
    ) {
        if (maxWidth.value > 0f && maxHeight.value > 0f && aspect > 0f) {
            val boxAspect = maxWidth.value / maxHeight.value
            val w: Dp
            val h: Dp
            if (aspect >= boxAspect) {
                w = maxWidth
                h = maxWidth / aspect
            } else {
                h = maxHeight
                w = maxHeight * aspect
            }
            PlayerFrame(
                width = w,
                height = h,
                mode = settings.playerBorder,
                animate = settings.animations && borderActive,
                scale = appear,
                content = content,
            )
        }
    }
}

// ------------------------------------------------------------------ Video

@Composable
private fun VideoPage(
    m: MediaFile,
    active: Boolean,
    chrome: Boolean,
    settings: AppSettings,
    onTap: () -> Unit,
) {
    if (active) {
        VideoPlayerContent(m, chrome, settings, onTap)
    } else {
        // Mientras se desliza entre páginas solo se muestra la portada; el reproductor vive en la página activa.
        Box(Modifier.fillMaxSize()) {
            VideoStage(
                aspect = initialAspect(m),
                chrome = chrome,
                settings = settings,
                borderActive = false,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
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
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onTap,
                    ),
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoPlayerContent(m: MediaFile, chrome: Boolean, settings: AppSettings, onTap: () -> Unit) {
    val context = LocalContext.current
    val exo = remember(m.uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(m.uri))
            prepare()
            playWhenReady = settings.autoplay
        }
    }
    var isPlaying by remember { mutableStateOf(false) }
    var ended by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(m.durationMs) }
    var aspect by remember { mutableFloatStateOf(initialAspect(m)) }
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    var speed by remember { mutableFloatStateOf(1f) }
    var muted by remember { mutableStateOf(false) }
    var speedMenu by remember { mutableStateOf(false) }
    var flashDir by remember { mutableIntStateOf(0) }
    var lastDir by remember { mutableIntStateOf(1) }
    var flashTick by remember { mutableIntStateOf(0) }

    DisposableEffect(exo) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                ended = state == Player.STATE_ENDED
                if (state == Player.STATE_READY && exo.duration > 0) duration = exo.duration
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    aspect = videoSize.width * videoSize.pixelWidthHeightRatio / videoSize.height
                }
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

    LaunchedEffect(flashTick) {
        if (flashDir != 0) {
            delay(550)
            flashDir = 0
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
        VideoStage(
            aspect = aspect,
            chrome = chrome,
            settings = settings,
            borderActive = isPlaying,
        ) {
            AndroidView(
                factory = { ctx ->
                    // PlayerView con TextureView (ver res/layout/view_player.xml) para poder recortar las esquinas.
                    (LayoutInflater.from(ctx).inflate(R.layout.view_player, null, false) as PlayerView).apply {
                        setPlayer(exo)
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

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
                                pos.x < w / 3f -> {
                                    seekBy(-10_000)
                                    lastDir = -1
                                    flashDir = -1
                                    flashTick++
                                }

                                pos.x > w * 2f / 3f -> {
                                    seekBy(10_000)
                                    lastDir = 1
                                    flashDir = 1
                                    flashTick++
                                }

                                else -> togglePlay()
                            }
                        },
                    )
                },
        )

        AnimatedVisibility(
            visible = flashDir != 0,
            modifier = Modifier
                .align(if (lastDir < 0) Alignment.CenterStart else Alignment.CenterEnd)
                .padding(horizontal = 40.dp),
            enter = fadeIn() + scaleIn(initialScale = 0.6f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
        ) {
            Box(
                Modifier.size(72.dp).background(Color.Black.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (lastDir < 0) Icons.Rounded.Replay10 else Icons.Rounded.Forward10,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.White,
                )
            }
        }

        if (failed) {
            Text(
                "No se puede reproducir este video",
                modifier = Modifier.align(Alignment.Center),
            )
        }

        AnimatedVisibility(
            visible = chrome,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
        ) {
            val shownPosition = dragFraction?.let { (it * duration).toLong() } ?: position
            val fraction = dragFraction
                ?: if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f

            Surface(
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
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
                        Text(formatDuration(shownPosition), style = MaterialTheme.typography.labelMedium)
                        Text(
                            formatDuration(duration),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box {
                            TextButton(onClick = { speedMenu = true }) {
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
                            IconButton(onClick = { seekBy(-10_000) }) {
                                Icon(Icons.Rounded.Replay10, contentDescription = "Retroceder 10 segundos")
                            }
                            PlayPauseButton(
                                isPlaying = isPlaying,
                                ended = ended,
                                animations = settings.animations,
                                onClick = { togglePlay() },
                            )
                            IconButton(onClick = { seekBy(10_000) }) {
                                Icon(Icons.Rounded.Forward10, contentDescription = "Avanzar 10 segundos")
                            }
                        }

                        IconButton(
                            onClick = {
                                muted = !muted
                                exo.volume = if (muted) 0f else 1f
                            },
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
}

@Composable
private fun PlayPauseButton(isPlaying: Boolean, ended: Boolean, animations: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.85f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "playScale",
    )
    val state = when {
        ended -> 2
        isPlaying -> 1
        else -> 0
    }
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
        // El icono cambia con una pequeña animación de escala y desvanecido.
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                if (animations) {
                    (fadeIn(tween(160)) + scaleIn(initialScale = 0.6f)) togetherWith
                        (fadeOut(tween(100)) + scaleOut(targetScale = 0.6f))
                } else {
                    fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                }
            },
            label = "playIcon",
        ) { s ->
            Icon(
                when (s) {
                    2 -> Icons.Rounded.Replay
                    1 -> Icons.Rounded.Pause
                    else -> Icons.Rounded.PlayArrow
                },
                contentDescription = null,
                modifier = Modifier.size(36.dp),
            )
        }
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
