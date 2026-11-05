package com.GalleryYou.Pixel

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Fondo desenfocado "de cristal": el contenido marcado con [backdropSource] se copia a una capa,
 * y las barras que usan [backdropBlur] dibujan esa copia borrosa detrás de sí mismas.
 * Solo funciona en Android 12 (API 31) o superior.
 */
class BackdropState(val source: GraphicsLayer, val blurred: GraphicsLayer) {
    var level by mutableIntStateOf(0)
    var radius by mutableFloatStateOf(0f)
    var origin by mutableStateOf(Offset.Zero)
    var appliedRadius: Float = -1f
    val enabled: Boolean get() = level > 0
}

@Composable
fun rememberBackdropState(settings: AppSettings): BackdropState {
    val source = rememberGraphicsLayer()
    val blurred = rememberGraphicsLayer()
    val density = LocalDensity.current
    val state = remember(source, blurred) { BackdropState(source, blurred) }
    val level = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) settings.blur else 0
    state.level = level
    state.radius = with(density) { (if (level >= 2) 30.dp else 14.dp).toPx() }
    return state
}

/** Marca el contenido que quedará detrás de las barras translúcidas. */
fun Modifier.backdropSource(state: BackdropState): Modifier = this
    .onGloballyPositioned { state.origin = it.positionInRoot() }
    .drawWithContent {
        if (state.enabled) {
            state.source.record { this@drawWithContent.drawContent() }
            drawLayer(state.source)
            if (state.appliedRadius != state.radius) {
                state.blurred.renderEffect = BlurEffect(state.radius, state.radius, TileMode.Clamp)
                // Un poco más de saturación: el fondo detrás del cristal se ve más vivo.
                state.blurred.colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(1.45f) })
                state.appliedRadius = state.radius
            }
            state.blurred.record { drawLayer(state.source) }
        } else {
            drawContent()
        }
    }

/** Pinta detrás del elemento el fondo desenfocado y un velo de color. Sin efecto si está desactivado. */
@Composable
fun Modifier.backdropBlur(state: BackdropState?, tint: Color, corner: Dp = 0.dp): Modifier {
    if (state == null || !state.enabled) return this
    var pos by remember { mutableStateOf(Offset.Zero) }
    return this
        .onGloballyPositioned { pos = it.positionInRoot() }
        .drawBehind {
            val path = Path().apply {
                addRoundRect(RoundRect(0f, 0f, size.width, size.height, CornerRadius(corner.toPx())))
            }
            clipPath(path) {
                translate(left = state.origin.x - pos.x, top = state.origin.y - pos.y) {
                    drawLayer(state.blurred)
                }
                drawRect(tint)
            }
        }
}

/**
 * Superficie de "cristal líquido": fondo desenfocado y saturado, velo de color, borde con brillo
 * especular y un reflejo suave arriba. Sin desenfoque activado usa [fallback], casi opaco, con
 * el mismo borde y reflejo para conservar el aspecto.
 */
@Composable
fun Modifier.glassSurface(
    state: BackdropState?,
    tint: Color,
    fallback: Color,
    corner: Dp,
    border: Boolean = true,
    extendBottom: Boolean = false,
): Modifier {
    val blurOn = state != null && state.enabled
    var pos by remember { mutableStateOf(Offset.Zero) }
    return this
        .onGloballyPositioned { pos = it.positionInRoot() }
        .drawBehind {
            val r = corner.toPx()
            val h = if (extendBottom) size.height + 48.dp.toPx() else size.height
            val path = Path().apply {
                addRoundRect(RoundRect(0f, 0f, size.width, h, CornerRadius(r)))
            }
            if (blurOn && state != null) {
                clipPath(path) {
                    translate(left = state.origin.x - pos.x, top = state.origin.y - pos.y) {
                        drawLayer(state.blurred)
                    }
                    drawRect(tint, size = androidx.compose.ui.geometry.Size(size.width, h))
                }
            } else {
                drawPath(path, fallback)
            }
            // Reflejo suave en la mitad superior.
            clipPath(path) {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                        startY = 0f,
                        endY = h * 0.55f,
                    ),
                    size = androidx.compose.ui.geometry.Size(size.width, h),
                )
            }
            if (border) {
                val sw = 1.3.dp.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.60f),
                            Color.White.copy(alpha = 0.06f),
                            Color.White.copy(alpha = 0.32f),
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width, h),
                    ),
                    topLeft = Offset(sw / 2f, sw / 2f),
                    size = androidx.compose.ui.geometry.Size(size.width - sw, h - sw),
                    cornerRadius = CornerRadius(r - sw / 2f),
                    style = Stroke(width = sw),
                )
            }
        }
}
