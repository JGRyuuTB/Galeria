package com.example.galeria

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PhotoAlbum
import androidx.compose.material.icons.rounded.Refresh
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GaleriaTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                    GaleriaApp()
                }
            }
        }
    }
}

@Composable
fun GaleriaApp() {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(VideoFrameDecoder.Factory()) }
            .crossfade(true)
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
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null),
                        ),
                    )
                },
            )
        } else {
            GalleryRoot()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryRoot() {
    val context = LocalContext.current
    var media by remember { mutableStateOf<List<MediaFile>?>(null) }
    var reload by remember { mutableIntStateOf(0) }
    LaunchedEffect(reload) {
        media = withContext(Dispatchers.IO) { MediaRepo.load(context) }
    }

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var openAlbumId by rememberSaveable { mutableStateOf<Long?>(null) }
    var viewer by remember { mutableStateOf<ViewerRequest?>(null) }
    var lastViewer by remember { mutableStateOf<ViewerRequest?>(null) }

    val all = media.orEmpty()
    val filtered = remember(all, filter) {
        when (filter) {
            1 -> all.filter { !it.isVideo }
            2 -> all.filter { it.isVideo }
            else -> all
        }
    }
    val albums = remember(all) { MediaRepo.albums(all) }
    val openAlbum = albums.firstOrNull { it.id == openAlbumId }

    fun open(items: List<MediaFile>, index: Int) {
        val request = ViewerRequest(items, index)
        lastViewer = request
        viewer = request
    }

    BackHandler(enabled = viewer != null) { viewer = null }
    BackHandler(enabled = viewer == null && openAlbum != null) { openAlbumId = null }

    Box(Modifier.fillMaxSize()) {
        when {
            media == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            openAlbum != null -> AlbumDetail(
                album = openAlbum,
                onBack = { openAlbumId = null },
                onOpen = { index -> open(openAlbum.items, index) },
            )

            else -> {
                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        TopAppBar(
                            title = { Text(if (tab == 0) "Fotos" else "Álbumes") },
                            actions = {
                                IconButton(onClick = { reload++ }) {
                                    Icon(Icons.Rounded.Refresh, contentDescription = "Actualizar")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                            scrollBehavior = scrollBehavior,
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = tab == 0,
                                onClick = { tab = 0 },
                                icon = { Icon(Icons.Rounded.Image, contentDescription = null) },
                                label = { Text("Fotos") },
                            )
                            NavigationBarItem(
                                selected = tab == 1,
                                onClick = { tab = 1 },
                                icon = { Icon(Icons.Rounded.PhotoAlbum, contentDescription = null) },
                                label = { Text("Álbumes") },
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
                            if (filtered.isEmpty() && all.isEmpty()) {
                                EmptyState(gridPadding)
                            } else {
                                MediaGrid(
                                    items = filtered,
                                    padding = gridPadding,
                                    header = {
                                        FilterRow(selected = filter, onSelect = { filter = it })
                                    },
                                    onOpen = { index -> open(filtered, index) },
                                )
                            }
                        } else {
                            AlbumsGrid(
                                albums = albums,
                                padding = gridPadding,
                                onOpen = { openAlbumId = it.id },
                            )
                        }
                    }
                }
            }
        }

        val shown = viewer ?: lastViewer
        AnimatedVisibility(
            visible = viewer != null,
            enter = fadeIn() + scaleIn(initialScale = 0.92f),
            exit = fadeOut() + scaleOut(targetScale = 0.92f),
        ) {
            shown?.let { ViewerScreen(request = it, onClose = { viewer = null }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetail(album: Album, onBack: () -> Unit, onOpen: (Int) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
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
        },
    ) { padding ->
        MediaGrid(
            items = album.items,
            padding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding(),
            ),
            header = null,
            onOpen = onOpen,
        )
    }
}
