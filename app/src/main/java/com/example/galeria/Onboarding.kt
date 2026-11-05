package com.example.galeria

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.BorderStyle
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PhotoAlbum
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.RoundedCorner
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val STEPS = 7

/** Introducción de primer uso: bienvenida, permiso, tema, colores, disposición y reproductor. */
@Composable
fun Onboarding(
    settings: AppSettings,
    granted: Boolean,
    onRequestPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onFinish: () -> Unit,
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    BackHandler(enabled = step > 0) { step -= 1 }
    val canNext = step != 1 || granted
    val animate = settings.animations

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(tween(320)) { w -> dir * w / 5 } + fadeIn(tween(260))) togetherWith
                        (slideOutHorizontally(tween(240)) { w -> -dir * w / 5 } + fadeOut(tween(140)))
                },
                label = "onboarding",
            ) { s ->
                when (s) {
                    0 -> WelcomeStep(animate)
                    1 -> PermissionStep(granted, animate, onRequestPermission, onOpenAppSettings)
                    2 -> ThemeStep(settings)
                    3 -> ColorsStep(settings)
                    4 -> LayoutStep(settings)
                    5 -> PlayerStep(settings)
                    else -> DoneStep(animate)
                }
            }
        }
        BottomBar(step = step, canNext = canNext) {
            if (step == STEPS - 1) onFinish() else step += 1
        }
    }
}

// ------------------------------------------------------------------ Barra inferior

@Composable
private fun BottomBar(step: Int, canNext: Boolean, onNext: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val last = step == STEPS - 1
    val fabColor by animateColorAsState(
        targetValue = if (canNext) scheme.primary else scheme.primary.copy(alpha = 0.16f),
        label = "fabColor",
    )
    val fabShape: Shape = when (step % 3) {
        0 -> CircleShape
        1 -> RoundedCornerShape(28.dp)
        else -> ScallopShape(8)
    }
    Surface(
        color = scheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(start = 28.dp, end = 20.dp, top = 24.dp, bottom = 24.dp)
                .heightIn(min = 72.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = when {
                    step == 0 -> "¡Vamos!"
                    last -> "Todo listo"
                    else -> "Paso $step de ${STEPS - 1}"
                },
                style = if (step == 0) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge,
                fontWeight = if (step == 0) FontWeight.Bold else FontWeight.Medium,
                color = if (step == 0) scheme.primary else scheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(fabShape)
                    .background(fabColor)
                    .clickable(enabled = canNext, onClick = onNext),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (last) Icons.Rounded.Check else Icons.Rounded.ArrowForward,
                    contentDescription = if (last) "Empezar" else "Siguiente",
                    tint = if (canNext) scheme.onPrimary else scheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

// ------------------------------------------------------------------ Pasos

@Composable
private fun WelcomeStep(animate: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 28.dp, vertical = 24.dp),
    ) {
        Text(
            "Bienvenido a",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold,
            color = scheme.onSurface,
        )
        Text(
            "Galería",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = scheme.primary,
        )
        Spacer(Modifier.weight(0.5f))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Surface(shape = CircleShape, color = scheme.surfaceContainerHigh) {
                Text(
                    "β  Beta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        HeroIllustration(animate, Modifier.fillMaxWidth().height(260.dp))
        Spacer(Modifier.weight(1f))
        Text(
            "Vamos a dejar todo listo para ti.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = scheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PermissionStep(
    granted: Boolean,
    animate: Boolean,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    StepFrame(
        title = "Permiso de medios",
        subtitle = "Galería necesita acceso a tus fotos y videos para mostrar tu colección.",
    ) {
        Spacer(Modifier.height(24.dp))
        ClusterIllustration(
            center = Icons.Rounded.Image,
            a = Icons.Rounded.Videocam,
            b = Icons.Rounded.Collections,
            c = Icons.Rounded.PhotoAlbum,
            d = Icons.Rounded.Share,
            animate = animate,
        )
        Spacer(Modifier.height(28.dp))
        if (granted) {
            Surface(shape = CircleShape, color = scheme.primaryContainer) {
                Row(
                    Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = scheme.onPrimaryContainer)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Permiso concedido",
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onPrimaryContainer,
                    )
                }
            }
        } else {
            Button(
                onClick = onRequest,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.primaryContainer,
                    contentColor = scheme.onPrimaryContainer,
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 32.dp, vertical = 18.dp),
            ) {
                Text("Conceder permiso de medios", style = MaterialTheme.typography.titleMedium)
            }
            TextButton(onClick = onOpenSettings) { Text("Abrir ajustes de la app") }
        }
    }
}

@Composable
private fun ThemeStep(settings: AppSettings) {
    StepFrame(title = "Tema de la app", subtitle = "Elige el aspecto antes de explorar tu galería.") {
        Spacer(Modifier.height(32.dp))
        OptionCard(
            title = "Oscuro",
            desc = "El aspecto oscuro Material 3, cómodo para los ojos.",
            icon = Icons.Rounded.DarkMode,
            selected = settings.themeMode == 2,
            badge = "Recomendado",
        ) { settings.updateThemeMode(2) }
        Spacer(Modifier.height(12.dp))
        OptionCard(
            title = "Claro",
            desc = "Un aspecto Material 3 más luminoso en toda la app.",
            icon = Icons.Rounded.LightMode,
            selected = settings.themeMode == 1,
        ) { settings.updateThemeMode(1) }
        Spacer(Modifier.height(12.dp))
        OptionCard(
            title = "Seguir el sistema",
            desc = "Coincidir con el tema del teléfono.",
            icon = Icons.Rounded.PhoneAndroid,
            selected = settings.themeMode == 0,
        ) { settings.updateThemeMode(0) }
        FootNote("Puedes cambiarlo después en Ajustes > Apariencia.")
    }
}

@Composable
private fun ColorsStep(settings: AppSettings) {
    val context = LocalContext.current
    val dynamicAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    StepFrame(title = "Colores", subtitle = "Haz que la galería combine contigo.") {
        Spacer(Modifier.height(32.dp))
        ToggleCard(
            title = "Colores dinámicos",
            desc = if (dynamicAvailable) {
                "Usa los colores de tu fondo de pantalla (Material You)."
            } else {
                "Disponible desde Android 12."
            },
            icon = Icons.Rounded.Palette,
            checked = settings.dynamicColor && dynamicAvailable,
            enabled = dynamicAvailable,
        ) { settings.updateDynamicColor(it) }
        Spacer(Modifier.height(12.dp))
        ToggleCard(
            title = "Icono Material You",
            desc = "El icono de la app toma los colores de tu fondo.",
            icon = Icons.Rounded.Apps,
            checked = settings.materialYouIcon,
        ) {
            settings.updateMaterialYouIcon(it)
            applyAppIcon(context, it)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Spacer(Modifier.height(12.dp))
            ToggleCard(
                title = "Desenfoque",
                desc = "Efecto de cristal en las barras. Puede usar más batería.",
                icon = Icons.Rounded.BlurOn,
                checked = settings.blur > 0,
            ) { settings.updateBlur(if (it) 1 else 0) }
        }
        FootNote("Puedes cambiarlo después en Ajustes > Apariencia.")
    }
}

@Composable
private fun LayoutStep(settings: AppSettings) {
    val scheme = MaterialTheme.colorScheme
    val cols = settings.photoColumns
    val corner = if (settings.roundedCorners) 12.dp else 3.dp
    StepFrame(title = "Disposición de la galería", subtitle = "Elige cómo quieres ver tus fotos.") {
        Spacer(Modifier.height(24.dp))
        // Vista previa en vivo.
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(listOf(scheme.primaryContainer, scheme.surfaceContainer)),
                )
                .padding(18.dp),
        ) {
            Column {
                Text(
                    "Galería",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onPrimaryContainer,
                )
                Spacer(Modifier.height(14.dp))
                val tileColors = listOf(
                    scheme.primary,
                    scheme.tertiary,
                    scheme.secondary,
                    scheme.primaryContainer,
                    scheme.tertiaryContainer,
                )
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(3) { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            repeat(cols) { col ->
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(corner))
                                        .background(tileColors[(row * cols + col) % tileColors.size].copy(alpha = 0.85f)),
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            for (n in 2..5) {
                val sel = n == cols
                val bg by animateColorAsState(
                    if (sel) scheme.primary else scheme.surfaceContainerHigh,
                    label = "colChip",
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(CircleShape)
                        .background(bg)
                        .clickable { settings.updatePhotoColumns(n) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "$n",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (sel) scheme.onPrimary else scheme.onSurface,
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Columnas",
            style = MaterialTheme.typography.labelLarge,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )
        Spacer(Modifier.height(14.dp))
        ToggleCard(
            title = "Esquinas redondeadas",
            desc = "Miniaturas con bordes suaves.",
            icon = Icons.Rounded.RoundedCorner,
            checked = settings.roundedCorners,
        ) { settings.updateRoundedCorners(it) }
        FootNote("Puedes cambiarlo después en Ajustes > Apariencia.")
    }
}

@Composable
private fun PlayerStep(settings: AppSettings) {
    StepFrame(title = "Reproductor", subtitle = "Elige cómo se ven y se reproducen tus videos.") {
        Spacer(Modifier.height(24.dp))
        ToggleCard(
            title = "Reproducción automática",
            desc = "Los videos empiezan al abrirlos.",
            icon = Icons.Rounded.PlayCircle,
            checked = settings.autoplay,
        ) { settings.updateAutoplay(it) }
        Spacer(Modifier.height(12.dp))
        ToggleCard(
            title = "Animaciones",
            desc = "Transiciones y efectos en toda la app.",
            icon = Icons.Rounded.Animation,
            checked = settings.animations,
        ) { settings.updateAnimations(it) }
        Spacer(Modifier.height(20.dp))
        Text(
            "Borde del panel de controles",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 10.dp),
        )
        OptionCard("Sin borde", "Un panel limpio y simple.", Icons.Rounded.BorderStyle, settings.playerBorder == 0) {
            settings.updatePlayerBorder(0)
        }
        Spacer(Modifier.height(10.dp))
        OptionCard("Color del tema", "Borde fijo con el color principal.", Icons.Rounded.BorderStyle, settings.playerBorder == 1) {
            settings.updatePlayerBorder(1)
        }
        Spacer(Modifier.height(10.dp))
        OptionCard("Animado", "El borde cambia de color al reproducir.", Icons.Rounded.BorderStyle, settings.playerBorder == 2, badge = "Recomendado") {
            settings.updatePlayerBorder(2)
        }
        FootNote("Puedes cambiarlo después en Ajustes > Reproductor.")
    }
}

@Composable
private fun DoneStep(animate: Boolean) {
    StepFrame(
        title = "¡Todo listo!",
        subtitle = "Tu galería está preparada. Puedes ajustar todo después en Ajustes.",
    ) {
        Spacer(Modifier.height(24.dp))
        ClusterIllustration(
            center = Icons.Rounded.CheckCircle,
            a = Icons.Rounded.Image,
            b = Icons.Rounded.Videocam,
            c = Icons.Rounded.Collections,
            d = Icons.Rounded.PhotoAlbum,
            animate = animate,
        )
    }
}

// ------------------------------------------------------------------ Piezas comunes

@Composable
private fun StepFrame(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@Composable
private fun FootNote(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp, start = 12.dp, end = 12.dp),
    )
}

@Composable
private fun OptionCard(
    title: String,
    desc: String,
    icon: ImageVector,
    selected: Boolean,
    badge: String? = null,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val bg by animateColorAsState(
        if (selected) scheme.primaryContainer else scheme.surfaceContainer,
        label = "optBg",
    )
    val iconBg by animateColorAsState(
        if (selected) scheme.primary else scheme.secondaryContainer,
        label = "optIconBg",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(52.dp).clip(RoundedCornerShape(18.dp)).background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                null,
                tint = if (selected) scheme.onPrimary else scheme.onSecondaryContainer,
                modifier = Modifier.size(26.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = if (selected) scheme.onPrimaryContainer else scheme.onSurface,
            )
            if (badge != null) {
                Spacer(Modifier.height(4.dp))
                Surface(shape = CircleShape, color = scheme.primary) {
                    Text(
                        badge,
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                desc,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (selected) scheme.primary else scheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(Icons.Rounded.Check, null, tint = scheme.onPrimary, modifier = Modifier.size(22.dp))
            } else {
                Box(Modifier.size(10.dp).clip(CircleShape).background(scheme.onSurfaceVariant.copy(alpha = 0.5f)))
            }
        }
    }
}

@Composable
private fun ToggleCard(
    title: String,
    desc: String,
    icon: ImageVector,
    checked: Boolean,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(scheme.surfaceContainer)
            .clickable(enabled = enabled) { onChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(scheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = scheme.onSecondaryContainer, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = scheme.onSurface)
            Text(desc, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
    }
}

// ------------------------------------------------------------------ Ilustraciones

@Composable
private fun floatingOffset(animate: Boolean): State<Float> {
    if (!animate) return remember { mutableStateOf(0f) }
    return rememberInfiniteTransition(label = "float").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "floatValue",
    )
}

/** Cuatro "satélites" alrededor de una pieza central inclinada. */
@Composable
private fun ClusterIllustration(
    center: ImageVector,
    a: ImageVector,
    b: ImageVector,
    c: ImageVector,
    d: ImageVector,
    animate: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    val bob = floatingOffset(animate)
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(Modifier.size(width = 320.dp, height = 260.dp)) {
            // Círculo arriba a la izquierda.
            Box(
                Modifier
                    .offset(x = 6.dp, y = 20.dp)
                    .graphicsLayer { translationY = -bob.value * 8.dp.toPx() }
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(scheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) { Icon(a, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(32.dp)) }
            // Flor abajo a la izquierda.
            Box(
                Modifier
                    .offset(x = 30.dp, y = 150.dp)
                    .graphicsLayer {
                        translationY = bob.value * 8.dp.toPx()
                        rotationZ = bob.value * 14f
                    }
                    .size(56.dp)
                    .clip(ScallopShape(8))
                    .background(scheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) { Icon(d, null, tint = scheme.onSecondaryContainer, modifier = Modifier.size(26.dp)) }
            // Pieza central inclinada.
            Box(
                Modifier
                    .offset(x = 84.dp, y = 50.dp)
                    .graphicsLayer {
                        rotationZ = -15f + bob.value * 4f
                        translationY = -bob.value * 4.dp.toPx()
                    }
                    .size(150.dp)
                    .clip(RoundedCornerShape(38.dp))
                    .background(scheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) { Icon(center, null, tint = scheme.primary, modifier = Modifier.size(76.dp)) }
            // Cuadrado inclinado arriba a la derecha.
            Box(
                Modifier
                    .offset(x = 214.dp, y = 30.dp)
                    .graphicsLayer {
                        rotationZ = 20f - bob.value * 6f
                        translationY = bob.value * 6.dp.toPx()
                    }
                    .size(96.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(scheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) { Icon(b, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(40.dp)) }
            // Círculo abajo a la derecha.
            Box(
                Modifier
                    .offset(x = 232.dp, y = 150.dp)
                    .graphicsLayer { translationY = -bob.value * 8.dp.toPx() }
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(scheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) { Icon(c, null, tint = scheme.tertiary, modifier = Modifier.size(32.dp)) }
        }
    }
}

/** Ilustración de bienvenida: una foto con montañas y sol sobre una ola. */
@Composable
private fun HeroIllustration(animate: Boolean, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val bob = floatingOffset(animate)
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val t = bob.value

        // Ola.
        val wave = Path().apply {
            val y = h * 0.94f
            moveTo(0f, y)
            val amp = 7.dp.toPx()
            val len = 56.dp.toPx()
            var x = 0f
            while (x < w) {
                cubicTo(x + len * 0.25f, y - amp * 2f, x + len * 0.75f, y + amp * 2f, x + len, y)
                x += len
            }
        }
        drawPath(wave, scheme.primary.copy(alpha = 0.85f), style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))

        val cw = minOf(w * 0.5f, 210.dp.toPx())
        val ch = cw * 1.12f
        val left = (w - cw) / 2f
        val top = h * 0.04f - t * 8.dp.toPx()
        val radius = CornerRadius(30.dp.toPx())

        // Foto de atrás.
        rotate(degrees = 11f, pivot = Offset(w / 2f, top + ch / 2f)) {
            drawRoundRect(
                color = scheme.secondaryContainer,
                topLeft = Offset(left + cw * 0.12f, top + 10.dp.toPx()),
                size = Size(cw, ch),
                cornerRadius = radius,
            )
        }
        // Foto de delante.
        rotate(degrees = -8f, pivot = Offset(w / 2f, top + ch / 2f)) {
            val card = Path().apply {
                addRoundRect(RoundRect(left, top, left + cw, top + ch, radius))
            }
            drawPath(card, scheme.primaryContainer)
            clipPath(card) {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(scheme.tertiaryContainer, scheme.primaryContainer),
                        startY = top,
                        endY = top + ch,
                    ),
                    topLeft = Offset(left, top),
                    size = Size(cw, ch),
                )
                drawCircle(scheme.tertiary, radius = cw * 0.10f, center = Offset(left + cw * 0.72f, top + ch * 0.24f))
                val m2 = Path().apply {
                    moveTo(left + cw * 0.30f, top + ch)
                    lineTo(left + cw * 0.62f, top + ch * 0.50f)
                    lineTo(left + cw * 1.05f, top + ch)
                    close()
                }
                drawPath(m2, scheme.secondary)
                val m1 = Path().apply {
                    moveTo(left - cw * 0.05f, top + ch)
                    lineTo(left + cw * 0.34f, top + ch * 0.42f)
                    lineTo(left + cw * 0.78f, top + ch)
                    close()
                }
                drawPath(m1, scheme.primary)
            }
        }
        // Destellos.
        drawCircle(scheme.tertiary, radius = 7.dp.toPx(), center = Offset(w * 0.80f, h * 0.22f + t * 6.dp.toPx()))
        drawCircle(scheme.primary, radius = 5.dp.toPx(), center = Offset(w * 0.20f, h * 0.30f - t * 6.dp.toPx()))
        drawCircle(scheme.secondary, radius = 9.dp.toPx(), center = Offset(w * 0.86f, h * 0.62f - t * 5.dp.toPx()))
    }
}

/** Círculo con lóbulos suaves, como una flor. */
private class ScallopShape(private val lobes: Int, private val depth: Float = 0.07f) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = minOf(cx, cy) / (1f + depth)
        val steps = 120
        for (i in 0..steps) {
            val angle = (2.0 * PI * i / steps).toFloat()
            val rr = r * (1f + depth * cos(lobes * angle))
            val x = cx + rr * cos(angle)
            val y = cy + rr * sin(angle)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}
