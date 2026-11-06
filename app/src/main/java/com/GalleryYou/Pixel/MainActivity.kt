package com.GalleryYou.Pixel

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import androidx.compose.ui.geometry.Rect
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Pantalla visible: 0 = cargando, 1 = pestañas, 2 = álbum abierto (los ids de álbum pueden ser negativos). */
private data class ScreenKey(val kind: Int, val id: Long = 0L)

private val LightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val DarkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)

class MainActivity : ComponentActivity() {
    /** Aplica el idioma elegido en Ajustes (si no, se usa el del sistema). */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.withAppLocale())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settings = AppSettings(applicationContext)
        applyAppIcon(applicationContext, settings.materialYouIcon)
        setContent {
            val dark = isAppDark(settings)
            // Los iconos de las barras del sistema siguen el tema elegido en Ajustes.
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { dark },
                    navigationBarStyle = SystemBarStyle.auto(LightScrim, DarkScrim) { dark },
                )
                onDispose { }
            }
            GaleriaTheme(settings) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                    GaleriaApp(settings)
                }
            }
        }
    }
}

@Composable
fun GaleriaApp(settings: AppSettings) {
    val context = LocalContext.current
    val updater = remember { Updater(context.applicationContext) }
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(MediaThumbFetcher.Factory(context))
                add(VideoFrameDecoder.Factory())
            }
            .crossfade(false)
            .allowRgb565(true)
            .respectCacheHeaders(false)
            .memoryCache {
                MemoryCache.Builder(context).maxSizePercent(0.30).build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("thumbs"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .build()
    }
    val permissions = remember {
        if (Build.VERSION.SDK_INT >= 33) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    var granted by remember {
        mutableStateOf(
            permissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            },
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result -> granted = result.values.all { it } }

    // Búsqueda automática de actualizaciones: como mucho cada 12 horas y solo con la app lista.
    LaunchedEffect(settings.onboardingDone, granted) {
        if (settings.onboardingDone && granted && settings.autoUpdate) {
            val now = System.currentTimeMillis()
            if (now - settings.lastUpdateCheck > 12L * 60L * 60L * 1000L) {
                settings.markUpdateChecked()
                updater.check(manual = false)
            }
        }
    }

    CompositionLocalProvider(LocalGalleryLoader provides imageLoader) {
        if (!settings.onboardingDone) {
            Onboarding(
                settings = settings,
                granted = granted,
                onRequestPermission = { launcher.launch(permissions) },
                onOpenAppSettings = {
                    context.startActivity(
                        Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null),
                        ),
                    )
                },
                onFinish = { settings.completeOnboarding() },
            )
        } else if (!granted) {
            PermissionScreen(
                onRequest = { launcher.launch(permissions) },
                onSettings = {
                    context.startActivity(
                        Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null),
                        ),
                    )
                },
            )
        } else {
            GalleryRoot(settings, updater)
        }
        UpdateDialog(updater)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryRoot(settings: AppSettings, updater: Updater) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var media by remember { mutableStateOf<List<MediaFile>?>(null) }
    var trash by remember { mutableStateOf<List<MediaFile>>(emptyList()) }
    var reload by remember { mutableIntStateOf(0) }
    LaunchedEffect(reload) {
        val loaded = withContext(Dispatchers.IO) {
            MediaRepo.load(context) to MediaRepo.loadTrash(context)
        }
        media = loaded.first
        trash = loaded.second
    }

    // Al volver a la app (por ejemplo, tras tomar una foto) se actualiza la galería.
    val owner = remember { context.findActivity() as? LifecycleOwner }
    DisposableEffect(owner) {
        var first = true
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (first) first = false else reload++
            }
        }
        owner?.lifecycle?.addObserver(observer)
        onDispose { owner?.lifecycle?.removeObserver(observer) }
    }

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var filterMode by rememberSaveable { mutableIntStateOf(0) }
    var openAlbumId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selected by remember { mutableStateOf<Set<Uri>>(emptySet()) }
    var moveItems by remember { mutableStateOf<List<MediaFile>?>(null) }
    var viewer by remember { mutableStateOf<ViewerRequest?>(null) }
    var lastViewer by remember { mutableStateOf<ViewerRequest?>(null) }

    // ---- Acciones del sistema que piden confirmación al usuario (papelera, mover, borrar) ----
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    val intentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) pending?.invoke()
        pending = null
    }

    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun launchRequest(sender: IntentSender, onGranted: () -> Unit) {
        pending = onGranted
        intentLauncher.launch(IntentSenderRequest.Builder(sender).build())
    }

    fun trashItems(items: List<MediaFile>, onDone: () -> Unit = {}) {
        if (items.isEmpty()) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            toast(context.getString(R.string.trash_needs_android11))
            return
        }
        val uris = items.map { it.uri }
        launchRequest(MediaStore.createTrashRequest(context.contentResolver, uris, true).intentSender) {
            toast(context.resources.getQuantityString(R.plurals.toast_trashed, items.size, items.size))
            selected = emptySet()
            onDone()
            reload++
        }
    }

    fun restoreItems(items: List<MediaFile>) {
        if (items.isEmpty() || Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        val uris = items.map { it.uri }
        launchRequest(MediaStore.createTrashRequest(context.contentResolver, uris, false).intentSender) {
            toast(context.getString(R.string.toast_restored))
            reload++
        }
    }

    fun deleteForever(items: List<MediaFile>) {
        if (items.isEmpty() || Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        val uris = items.map { it.uri }
        launchRequest(MediaStore.createDeleteRequest(context.contentResolver, uris).intentSender) {
            toast(context.getString(R.string.toast_deleted_forever))
            reload++
        }
    }

    fun moveItemsTo(items: List<MediaFile>, relativePath: String) {
        if (items.isEmpty()) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            toast(context.getString(R.string.move_needs_android11))
            return
        }
        val uris = items.map { it.uri }
        launchRequest(MediaStore.createWriteRequest(context.contentResolver, uris).intentSender) {
            scope.launch {
                val moved = withContext(Dispatchers.IO) { MediaRepo.move(context, items, relativePath) }
                toast(
                    if (moved == items.size) {
                        context.getString(R.string.toast_moved)
                    } else {
                        context.getString(R.string.toast_moved_partial, moved, items.size)
                    },
                )
                selected = emptySet()
                reload++
            }
        }
    }

    // Transición del visor: crece desde la miniatura y vuelve a ella al cerrarse.
    val thumbRegistry = remember { ThumbRegistry() }
    val viewerProgress = remember { Animatable(0f) }
    var viewerVisible by remember { mutableStateOf(false) }
    var viewerOrigin by remember { mutableStateOf<Rect?>(null) }
    var viewerCurrent by remember { mutableStateOf<Uri?>(null) }
    var dragDismissed by remember { mutableStateOf(false) }

    fun open(items: List<MediaFile>, index: Int) {
        val request = ViewerRequest(items, index)
        lastViewer = request
        val first = items.getOrNull(index)?.uri
        viewerCurrent = first
        viewerOrigin = thumbRegistry.boundsOf(first)
        viewer = request
    }

    val viewerOpen = viewer != null
    LaunchedEffect(viewerOpen) {
        if (viewerOpen) {
            viewerVisible = true
            if (settings.animations) {
                viewerProgress.snapTo(0f)
                viewerProgress.animateTo(1f, tween(340, easing = FastOutSlowInEasing))
            } else {
                viewerProgress.snapTo(1f)
            }
        } else if (viewerVisible) {
            if (dragDismissed || !settings.animations) {
                viewerProgress.snapTo(0f)
            } else {
                // Vuelve a la miniatura de la foto que se estaba viendo (si sigue en pantalla).
                viewerOrigin = thumbRegistry.boundsOf(viewerCurrent)
                viewerProgress.animateTo(0f, tween(300, easing = FastOutSlowInEasing))
            }
            viewerVisible = false
            dragDismissed = false
        }
    }

    fun toggle(m: MediaFile) {
        selected = if (m.uri in selected) selected - m.uri else selected + m.uri
    }

    // ---- Datos derivados ----
    val all = media.orEmpty()
    val filtered = remember(all, filterMode) {
        when (filterMode) {
            1 -> all.filter { !it.isVideo }
            2 -> all.filter { it.isVideo }
            else -> all
        }
    }
    val unnamedAlbum = stringResource(R.string.album_unnamed)
    val albums = remember(all, unnamedAlbum) { MediaRepo.albums(all, unnamedAlbum) }
    val openAlbum = albums.firstOrNull { it.id == openAlbumId }
    val selectedItems = remember(all, selected) { all.filter { it.uri in selected } }

    // El último BackHandler registrado tiene prioridad.
    BackHandler(enabled = openAlbum != null) {
        openAlbumId = null
        selected = emptySet()
    }
    BackHandler(enabled = selected.isNotEmpty() && tab == 0) { selected = emptySet() }
    BackHandler(enabled = tab >= 2) { tab = 0 }
    BackHandler(enabled = viewer != null) { viewer = null }

    CompositionLocalProvider(LocalThumbRegistry provides thumbRegistry) {
    Box(Modifier.fillMaxSize()) {
        val screen = when {
            media == null -> ScreenKey(0)
            openAlbum != null -> ScreenKey(2, openAlbum.id)
            else -> ScreenKey(1)
        }
        AnimatedContent(
            targetState = screen,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                if (!settings.animations) {
                    EnterTransition.None togetherWith ExitTransition.None
                } else if (targetState.kind == 2 && initialState.kind != 2) {
                    (slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { w -> w / 3 } + fadeIn(tween(260))) togetherWith
                        (slideOutHorizontally(tween(320, easing = FastOutSlowInEasing)) { w -> -w / 6 } + fadeOut(tween(200)))
                } else if (initialState.kind == 2 && targetState.kind != 2) {
                    (slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { w -> -w / 6 } + fadeIn(tween(260))) togetherWith
                        (slideOutHorizontally(tween(320, easing = FastOutSlowInEasing)) { w -> w / 3 } + fadeOut(tween(200)))
                } else {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(120))
                }
            },
            label = "screens",
        ) { s ->
        when {
            s.kind == 0 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            s.kind == 2 -> albums.firstOrNull { it.id == s.id }?.let { album -> AlbumDetail(
                album = album,
                settings = settings,
                selected = selected,
                onBack = {
                    openAlbumId = null
                    selected = emptySet()
                },
                onClick = { m, index ->
                    if (selected.isNotEmpty()) toggle(m) else open(album.items, index)
                },
                onLongClick = { m -> toggle(m) },
                onClearSelection = { selected = emptySet() },
                onSelectAll = { selected = album.items.map { it.uri }.toSet() },
                onMove = { moveItems = selectedItems },
                onTrash = { trashItems(selectedItems) },
            ) }

            else -> {
                val inSelection = selected.isNotEmpty() && tab == 0
                val backdrop = rememberBackdropState(settings)
                Scaffold(
                    topBar = {
                        if (tab <= 1) {
                            AnimatedContent(
                                targetState = inSelection,
                                transitionSpec = {
                                    if (settings.animations) {
                                        (fadeIn(tween(220)) + slideInVertically(tween(220)) { h -> -h / 3 }) togetherWith
                                            fadeOut(tween(120))
                                    } else {
                                        EnterTransition.None togetherWith ExitTransition.None
                                    }
                                },
                                label = "topBar",
                            ) { sel ->
                            if (sel) {
                                SelectionTopBar(
                                    count = selected.size,
                                    backdrop = backdrop,
                                    onClose = { selected = emptySet() },
                                    onSelectAll = { selected = filtered.map { it.uri }.toSet() },
                                ) {
                                    IconButton(onClick = { moveItems = selectedItems }) {
                                        Icon(AppIcons.Folder, contentDescription = stringResource(R.string.move_to_album))
                                    }
                                    IconButton(onClick = { trashItems(selectedItems) }) {
                                        Icon(AppIcons.Trash, contentDescription = stringResource(R.string.move_to_trash))
                                    }
                                }
                            } else {
                                Box(
                                    Modifier.backdropBlur(
                                        backdrop,
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                    ),
                                ) {
                                    TopAppBar(
                                        title = { Text(stringResource(if (tab == 0) R.string.nav_photos else R.string.nav_albums)) },
                                        colors = TopAppBarDefaults.topAppBarColors(
                                            containerColor = if (backdrop.enabled) {
                                                androidx.compose.ui.graphics.Color.Transparent
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            },
                                        ),
                                    )
                                }
                            }
                            }
                        }
                    },
                    bottomBar = {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                        ) {
                            GlassNavBar(
                                items = GalleryNavItems,
                                selected = tab,
                                animations = settings.animations,
                                backdrop = backdrop,
                                onSelect = {
                                    selected = emptySet()
                                    tab = it
                                },
                            )
                        }
                    },
                ) { padding ->
                    val gridPadding = PaddingValues(
                        top = padding.calculateTopPadding(),
                        bottom = padding.calculateBottomPadding(),
                    )
                    val bottomOnly = Modifier
                        .fillMaxSize()
                        .padding(bottom = padding.calculateBottomPadding())
                    AnimatedContent(
                        modifier = Modifier.fillMaxSize().backdropSource(backdrop),
                        targetState = tab,
                        transitionSpec = {
                            if (settings.animations) {
                                val dir = if (targetState > initialState) 1 else -1
                                (slideInHorizontally(tween(280)) { w -> dir * w / 6 } + fadeIn(tween(220))) togetherWith
                                    (slideOutHorizontally(tween(220)) { w -> -dir * w / 6 } + fadeOut(tween(120)))
                            } else {
                                EnterTransition.None togetherWith ExitTransition.None
                            }
                        },
                        label = "tabs",
                    ) { current ->
                        when (current) {
                            0 -> if (all.isEmpty()) {
                                EmptyState(gridPadding)
                            } else {
                                MediaGrid(
                                    items = filtered,
                                    padding = gridPadding,
                                    settings = settings,
                                    selected = selected,
                                    header = {
                                        FilterRow(selected = filterMode, onSelect = { filterMode = it })
                                    },
                                    onClick = { m, index ->
                                        if (selected.isNotEmpty()) toggle(m) else open(filtered, index)
                                    },
                                    onLongClick = { m -> toggle(m) },
                                )
                            }

                            1 -> AlbumsGrid(
                                albums = albums,
                                padding = gridPadding,
                                settings = settings,
                                onOpen = { openAlbumId = it.id },
                            )

                            2 -> Box(bottomOnly) {
                                TrashScreen(
                                    items = trash,
                                    settings = settings,
                                    onBack = { tab = 0 },
                                    onRestore = { restoreItems(it) },
                                    onDeleteForever = { deleteForever(it) },
                                )
                            }

                            else -> Box(bottomOnly) {
                                SettingsScreen(settings = settings, updater = updater, onBack = { tab = 0 })
                            }
                        }
                    }
                }
            }
        }
        }

        // Visor de fotos y videos.
        val shown = viewer ?: lastViewer
        if (viewerVisible) {
            shown?.let { request ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val p = viewerProgress.value
                            val o = viewerOrigin
                            val w = size.width
                            val h = size.height
                            if (o != null && w > 0f && h > 0f) {
                                // Crece desde la miniatura: misma posición y tamaño al inicio.
                                val s0 = o.width / w
                                val s = lerp(s0, 1f, p)
                                scaleX = s
                                scaleY = s
                                translationX = lerp(o.center.x - w / 2f, 0f, p)
                                translationY = lerp(o.center.y - h / 2f, 0f, p)
                                alpha = (p * 4f).coerceIn(0f, 1f)
                                clip = true
                                shape = RevealShape(p, o.height / o.width, s)
                            } else {
                                val s = 0.92f + 0.08f * p
                                scaleX = s
                                scaleY = s
                                alpha = p.coerceIn(0f, 1f)
                            }
                        },
                ) {
                    ViewerScreen(
                        request = request,
                        settings = settings,
                        onClose = { viewer = null },
                        onTrash = { m -> trashItems(listOf(m)) { viewer = null } },
                        onPageChanged = { viewerCurrent = it },
                        onDragDismiss = {
                            dragDismissed = true
                            viewer = null
                        },
                    )
                }
            }
        }
    }
    }

    val toMove = moveItems
    if (toMove != null) {
        MoveSheet(
            albums = albums,
            currentBuckets = toMove.map { it.bucketId }.toSet(),
            onPick = { path ->
                moveItems = null
                moveItemsTo(toMove, path)
            },
            onDismiss = { moveItems = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetail(
    album: Album,
    settings: AppSettings,
    selected: Set<Uri>,
    onBack: () -> Unit,
    onClick: (MediaFile, Int) -> Unit,
    onLongClick: (MediaFile) -> Unit,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onMove: () -> Unit,
    onTrash: () -> Unit,
) {
    Scaffold(
        topBar = {
            if (selected.isNotEmpty()) {
                SelectionTopBar(
                    count = selected.size,
                    onClose = onClearSelection,
                    onSelectAll = onSelectAll,
                ) {
                    IconButton(onClick = onMove) {
                        Icon(AppIcons.Folder, contentDescription = stringResource(R.string.move_to_album))
                    }
                    IconButton(onClick = onTrash) {
                        Icon(AppIcons.Trash, contentDescription = stringResource(R.string.move_to_trash))
                    }
                }
            } else {
                TopAppBar(
                    title = { Text(album.name) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(AppIcons.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                )
            }
        },
    ) { padding ->
        MediaGrid(
            items = album.items,
            padding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding(),
            ),
            settings = settings,
            selected = selected,
            header = null,
            onClick = onClick,
            onLongClick = onLongClick,
        )
    }
}

