package com.example.galeria

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
