package com.GalleryYou.Pixel

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

data class NavDest(val label: String, val icon: ImageVector, val iconSelected: ImageVector)

val GalleryNavItems = listOf(
    NavDest("Fotos", Icons.Outlined.PhotoLibrary, Icons.Filled.PhotoLibrary),
    NavDest("Álbumes", Icons.Outlined.Collections, Icons.Filled.Collections),
    NavDest("Papelera", Icons.Outlined.DeleteOutline, Icons.Filled.Delete),
    NavDest("Ajustes", Icons.Outlined.Settings, Icons.Filled.Settings),
)

/**
 * Barra de navegación "líquida": una burbuja sube sobre el destino activo, la barra se hunde
 * alrededor de ella con una curva suave y, al cambiar de destino, la burbuja se desliza con rebote.
 */
@Composable
fun LiquidNavBar(
    items: List<NavDest>,
    selected: Int,
    animations: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: BackdropState? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val barColor = scheme.surfaceContainerHighest
    val bubbleColor = scheme.primary
    val onBubble = scheme.onPrimary
    val barH = 66.dp
    val r = 26.dp
    val overflow = 30.dp

    var barPos by remember { mutableStateOf(Offset.Zero) }
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(barH + overflow)
            .onGloballyPositioned { barPos = it.positionInRoot() },
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val itemW = widthPx / items.size
        val overflowPx = with(density) { overflow.toPx() }
        val rPx = with(density) { r.toPx() }
        val dropPx = with(density) { 20.dp.toPx() }

        val cx = remember { Animatable(0f) }
        val lift = remember { Animatable(1f) }
        var placed by remember { mutableStateOf(false) }

        LaunchedEffect(selected, itemW, animations) {
            val target = itemW * (selected + 0.5f)
            if (!placed || !animations) {
                cx.snapTo(target)
                lift.snapTo(1f)
                placed = true
            } else {
                coroutineScope {
                    launch {
                        cx.animateTo(
                            target,
                            spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessLow),
                        )
                    }
                    launch {
                        lift.animateTo(0f, tween(120))
                        lift.animateTo(
                            1f,
                            spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow,
                            ),
                        )
                    }
                }
            }
        }

        // Fondo: barra con la muesca curva y la burbuja. Los valores animados se leen al dibujar.
        Canvas(Modifier.fillMaxSize()) {
            val top = overflowPx
            val h = barH.toPx()
            val x = cx.value
            val l = lift.value
            val halfW = rPx + 14.dp.toPx()
            val depth = (rPx + 6.dp.toPx()) * (0.55f + 0.45f * l)
            val up = 40.dp.toPx()

            val bar = Path().apply {
                addRoundRect(RoundRect(0f, top, size.width, top + h, CornerRadius(28.dp.toPx())))
            }
            val notch = Path().apply {
                moveTo(x - halfW, top - up)
                lineTo(x - halfW, top)
                cubicTo(x - halfW * 0.45f, top, x - rPx * 0.95f, top + depth, x, top + depth)
                cubicTo(x + rPx * 0.95f, top + depth, x + halfW * 0.45f, top, x + halfW, top)
                lineTo(x + halfW, top - up)
                close()
            }
            val shape = Path.combine(PathOperation.Difference, bar, notch)
            if (backdrop != null && backdrop.enabled) {
                clipPath(shape) {
                    translate(backdrop.origin.x - barPos.x, backdrop.origin.y - barPos.y) {
                        drawLayer(backdrop.blurred)
                    }
                }
                drawPath(shape, barColor.copy(alpha = 0.55f))
            } else {
                drawPath(shape, barColor)
            }
            drawCircle(bubbleColor, radius = rPx, center = Offset(x, top + (1f - l) * dropPx))
        }

        // Iconos de los destinos inactivos y etiqueta del activo.
        Row(Modifier.fillMaxSize().padding(top = overflow)) {
            items.forEachIndexed { i, d ->
                val isSel = i == selected
                val iconAlpha by animateFloatAsState(
                    targetValue = if (isSel) 0f else 1f,
                    animationSpec = if (animations) tween(180) else snap(),
                    label = "navIcon",
                )
                val labelAlpha by animateFloatAsState(
                    targetValue = if (isSel) 1f else 0f,
                    animationSpec = if (animations) tween(220, delayMillis = 120) else snap(),
                    label = "navLabel",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onSelect(i) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        d.icon,
                        contentDescription = d.label,
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp).graphicsLayer { alpha = iconAlpha },
                    )
                    Text(
                        d.label,
                        color = scheme.onSurface,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                            .graphicsLayer { alpha = labelAlpha },
                    )
                }
            }
        }

        // Icono dentro de la burbuja, siguiéndola mientras se desliza.
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (cx.value - rPx).roundToInt(),
                        (overflowPx + (1f - lift.value) * dropPx - rPx).roundToInt(),
                    )
                }
                .size(r * 2),
            contentAlignment = Alignment.Center,
        ) {
            Crossfade(
                targetState = selected,
                animationSpec = if (animations) tween(200) else snap(),
                label = "bubbleIcon",
            ) { idx ->
                Icon(
                    items[idx].iconSelected,
                    contentDescription = null,
                    tint = onBubble,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
    }
}
