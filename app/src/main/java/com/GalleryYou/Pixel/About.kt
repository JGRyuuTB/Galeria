package com.GalleryYou.Pixel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val GITHUB_URL = "https://github.com/JGRyuuTB"
private const val TELEGRAM_URL = "https://t.me/JG_ryuu"
private const val CHANNEL_URL = "https://t.me/dragonsbastion"

/** Pantalla "Acerca de": la app, quién la mantiene y dónde encontrar a la comunidad. */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {},
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
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Acerca de",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )

            // Tarjeta de la app.
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = scheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        AppLogo(60.dp)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                "Galería",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface,
                            )
                            Text(
                                "Galería de fotos y videos de código abierto, con el estilo de las apps de Pixel.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                    Surface(shape = CircleShape, color = scheme.tertiaryContainer) {
                        Text(
                            "Versión 1.0.0 · Estable",
                            style = MaterialTheme.typography.titleMedium,
                            color = scheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        InfoChip(AppIcons.Globe, "Código abierto")
                        InfoChip(AppIcons.Sparkle, "Sin anuncios")
                        InfoChip(AppIcons.Palette, "Material 3")
                    }
                }
            }

            SectionHeader("Mantenedor", "La persona detrás de Galería.")

            // Tarjeta del mantenedor.
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = scheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box64("JG")
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "JG Ryuu",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface,
                        )
                        Text(
                            "Creador y mantenedor",
                            style = MaterialTheme.typography.titleSmall,
                            color = scheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Construyendo Galería junto a la comunidad.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LinkButton(AppIcons.GitBranch, "GitHub") { openUrl(context, GITHUB_URL) }
                        LinkButton(AppIcons.Send, "Telegram") { openUrl(context, TELEGRAM_URL) }
                    }
                }
            }

            SectionHeader("Comunidad", "Novedades, versiones y soporte.")

            // Canal de Telegram.
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = scheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = scheme.secondaryContainer, modifier = Modifier.size(52.dp)) {
                            Column(
                                Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(
                                    AppIcons.Megaphone,
                                    contentDescription = null,
                                    tint = scheme.onSecondaryContainer,
                                    modifier = Modifier.size(26.dp),
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                "Dragons Bastion",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface,
                            )
                            Text(
                                "Canal de Telegram con novedades y nuevas versiones.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                    Button(
                        onClick = { openUrl(context, CHANNEL_URL) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(AppIcons.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Unirse al canal")
                    }
                }
            }

            // Licencia.
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = scheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        "Licencia MIT",
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onSurface,
                    )
                    Text(
                        "Hecha con Jetpack Compose, Material 3, Media3 y Coil, todos con licencia Apache 2.0. " +
                            "Paquete com.GalleryYou.Pixel.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(Modifier.padding(top = 8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InfoChip(icon: ImageVector, text: String) {
    val scheme = MaterialTheme.colorScheme
    Surface(shape = CircleShape, color = scheme.surfaceContainerHigh) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, color = scheme.onSurface)
        }
    }
}

/** Avatar con las iniciales (la app no usa internet, así que no descarga fotos de perfil). */
@Composable
private fun Box64(initials: String) {
    val scheme = MaterialTheme.colorScheme
    Surface(shape = CircleShape, color = scheme.primaryContainer, modifier = Modifier.size(64.dp)) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                initials,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = scheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun LinkButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(scheme.primary)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = description, tint = scheme.onPrimary, modifier = Modifier.size(22.dp))
    }
}

/** El mismo logo del icono de la app: cuatro pétalos de colores sobre un círculo blanco. */
@Composable
private fun AppLogo(logoSize: Dp) {
    Canvas(Modifier.size(logoSize)) {
        val s = size.minDimension
        val c = center
        drawCircle(Color.White)
        val r = s * 0.17f
        val off = s * 0.15f
        val colors = listOf(Color(0xEB4285F4), Color(0xEB34C9A0), Color(0xEB9C6EF0), Color(0xEBF25C8A))
        val centers = listOf(
            Offset(c.x, c.y - off),
            Offset(c.x + off, c.y),
            Offset(c.x, c.y + off),
            Offset(c.x - off, c.y),
        )
        centers.forEachIndexed { i, p -> drawCircle(colors[i], radius = r, center = p) }
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show()
    }
}
