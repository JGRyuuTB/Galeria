package com.example.galeria

import android.Manifest
import android.app.Activity
import android.content.Intent
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
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DriveFileMove
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Settings
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

private val LightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val DarkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settings = AppSettings(applicationContext)
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

    CompositionLocalProvider(LocalGalleryLoader provides imageLoader) {
        if (!granted) {
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
            GalleryRoot(settings)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryRoot(settings: AppSettings) {
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
    // 0 = ninguna, 1 = ajustes, 2 = papelera
    var overlay by rememberSaveable { mutableIntStateOf(0) }
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
            toast("La papelera requiere Android 11 o superior")
            return
        }
        val uris = items.map { it.uri }
        launchRequest(MediaStore.createTrashRequest(context.contentResolver, uris, true).intentSender) {
            toast(if (items.size == 1) "Movido a la papelera" else "${items.size} elementos en la papelera")
            selected = emptySet()
            onDone()
            reload++
        }
    }

    fun restoreItems(items: List<MediaFile>) {
        if (items.isEmpty() || Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        val uris = items.map { it.uri }
        launchRequest(MediaStore.createTrashRequest(context.contentResolver, uris, false).intentSender) {
            toast("Restaurado")
            reload++
        }
    }

    fun deleteForever(items: List<MediaFile>) {
        if (items.isEmpty() || Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        val uris = items.map { it.uri }
        launchRequest(MediaStore.createDeleteRequest(context.contentResolver, uris).intentSender) {
            toast("Eliminado definitivamente")
            reload++
        }
    }

    fun moveItemsTo(items: List<MediaFile>, relativePath: String) {
        if (items.isEmpty()) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            toast("Mover entre álbumes requiere Android 11 o superior")
            return
        }
        val uris = items.map { it.uri }
        launchRequest(MediaStore.createWriteRequest(context.contentResolver, uris).intentSender) {
            scope.launch {
                val moved = withContext(Dispatchers.IO) { MediaRepo.move(context, items, relativePath) }
                toast(
                    if (moved == items.size) {
                        "Movido"
                    } else {
                        "$moved de ${items.size} movidos (esa carpeta no admite algunos archivos)"
                    },
                )
                selected = emptySet()
                reload++
            }
        }
    }

    fun open(items: List<MediaFile>, index: Int) {
        val request = ViewerRequest(items, index)
        lastViewer = request
        viewer = request
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
    val albums = remember(all) { MediaRepo.albums(all) }
    val openAlbum = albums.firstOrNull { it.id == openAlbumId }
    val selectedItems = remember(all, selected) { all.filter { it.uri in selected } }

    // El último BackHandler registrado tiene prioridad.
    BackHandler(enabled = openAlbum != null) {
        openAlbumId = null
        selected = emptySet()
    }
    BackHandler(enabled = selected.isNotEmpty() && overlay == 0) { selected = emptySet() }
    BackHandler(enabled = overlay != 0) { overlay = 0 }
    BackHandler(enabled = viewer != null) { viewer = null }

    Box(Modifier.fillMaxSize()) {
        when {
            media == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            openAlbum != null -> AlbumDetail(
                album = openAlbum,
                settings = settings,
                selected = selected,
                onBack = {
                    openAlbumId = null
                    selected = emptySet()
                },
                onClick = { m, index ->
                    if (selected.isNotEmpty()) toggle(m) else open(openAlbum.items, index)
                },
                onLongClick = { m -> toggle(m) },
                onClearSelection = { selected = emptySet() },
                onSelectAll = { selected = openAlbum.items.map { it.uri }.toSet() },
                onMove = { moveItems = selectedItems },
                onTrash = { trashItems(selectedItems) },
            )

            else -> {
                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
                val inSelection = selected.isNotEmpty() && tab == 0
                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        if (inSelection) {
                            SelectionTopBar(
                                count = selected.size,
                                onClose = { selected = emptySet() },
                                onSelectAll = { selected = filtered.map { it.uri }.toSet() },
                                scrollBehavior = scrollBehavior,
                            ) {
                                IconButton(onClick = { moveItems = selectedItems }) {
                                    Icon(Icons.Outlined.DriveFileMove, contentDescription = "Mover a álbum")
                                }
                                IconButton(onClick = { trashItems(selectedItems) }) {
                                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Mover a la papelera")
                                }
                            }
                        } else {
                            TopAppBar(
                                title = { Text(if (tab == 0) "Fotos" else "Álbumes") },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                ),
                                scrollBehavior = scrollBehavior,
                            )
                        }
                    },
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = tab == 0,
                                onClick = { tab = 0 },
                                icon = {
                                    Icon(
                                        if (tab == 0) Icons.Filled.PhotoLibrary else Icons.Outlined.PhotoLibrary,
                                        contentDescription = null,
                                    )
                                },
                                label = { Text("Fotos") },
                            )
                            NavigationBarItem(
                                selected = tab == 1,
                                onClick = {
                                    tab = 1
                                    selected = emptySet()
                                },
                                icon = {
                                    Icon(
                                        if (tab == 1) Icons.Filled.Collections else Icons.Outlined.Collections,
                                        contentDescription = null,
                                    )
                                },
                                label = { Text("Álbumes") },
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    selected = emptySet()
                                    overlay = 2
                                },
                                icon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null) },
                                label = { Text("Papelera") },
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    selected = emptySet()
                                    overlay = 1
                                },
                                icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                                label = { Text("Ajustes") },
                            )
                        }
                    },
                ) { padding ->
                    val gridPadding = PaddingValues(
                        top = padding.calculateTopPadding(),
                        bottom = padding.calculateBottomPadding(),
                    )
                    Crossfade(targetState = tab, label = "tabs") { current ->
                        if (current == 0) {
                            if (all.isEmpty()) {
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
                        } else {
                            AlbumsGrid(
                                albums = albums,
                                padding = gridPadding,
                                settings = settings,
                                onOpen = { openAlbumId = it.id },
                            )
                        }
                    }
                }
            }
        }

        // Ajustes y papelera se deslizan desde la derecha.
        AnimatedVisibility(
            visible = overlay == 1,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut(),
        ) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                SettingsScreen(settings = settings, onBack = { overlay = 0 })
            }
        }
        AnimatedVisibility(
            visible = overlay == 2,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut(),
        ) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                TrashScreen(
                    items = trash,
                    settings = settings,
                    onBack = { overlay = 0 },
                    onRestore = { restoreItems(it) },
                    onDeleteForever = { deleteForever(it) },
                )
            }
        }

        // Visor de fotos y videos.
        val shown = viewer ?: lastViewer
        AnimatedVisibility(
            visible = viewer != null,
            enter = fadeIn() + scaleIn(initialScale = 0.92f),
            exit = fadeOut() + scaleOut(targetScale = 0.92f),
        ) {
            shown?.let { request ->
                ViewerScreen(
                    request = request,
                    settings = settings,
                    onClose = { viewer = null },
                    onTrash = { m -> trashItems(listOf(m)) { viewer = null } },
                )
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
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (selected.isNotEmpty()) {
                SelectionTopBar(
                    count = selected.size,
                    onClose = onClearSelection,
                    onSelectAll = onSelectAll,
                    scrollBehavior = scrollBehavior,
                ) {
                    IconButton(onClick = onMove) {
                        Icon(Icons.Outlined.DriveFileMove, contentDescription = "Mover a álbum")
                    }
                    IconButton(onClick = onTrash) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = "Mover a la papelera")
                    }
                }
            } else {
                TopAppBar(
                    title = { Text(album.name) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                    scrollBehavior = scrollBehavior,
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
