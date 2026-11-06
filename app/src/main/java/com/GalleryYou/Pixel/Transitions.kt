package com.GalleryYou.Pixel

import android.net.Uri
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * Recuerda dónde está cada miniatura en pantalla para que el visor pueda crecer desde ella
 * al abrirse y volver a ella al cerrarse. Guarda solo las últimas, para no acumular memoria.
 */
class ThumbRegistry {
    private val map = object : LinkedHashMap<Uri, LayoutCoordinates>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Uri, LayoutCoordinates>?): Boolean =
            size > 80
    }

    fun put(uri: Uri, coordinates: LayoutCoordinates) {
        map[uri] = coordinates
    }

    /** Rectángulo de la miniatura en pantalla, o null si ya no está visible. */
    fun boundsOf(uri: Uri?): Rect? {
        val coordinates = uri?.let { map[it] } ?: return null
        if (!coordinates.isAttached) return null
        val bounds = coordinates.boundsInRoot()
        return if (bounds.width > 1f && bounds.height > 1f) bounds else null
    }
}

val LocalThumbRegistry = staticCompositionLocalOf<ThumbRegistry?> { null }

/**
 * Recorte del visor durante la transición: empieza con la forma de la miniatura y se abre
 * hasta ocupar toda la pantalla. [aspect] es alto/ancho de la miniatura y [scale] la escala
 * actual del contenedor, para que las esquinas redondeadas midan lo mismo en pantalla.
 */
class RevealShape(
    private val progress: Float,
    private val aspect: Float,
    private val scale: Float,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        val startHeight = w * aspect
        val rh = startHeight + (h - startHeight) * progress
        val top = (h - rh) / 2f
        val screenRadius = 14f * density.density * (1f - progress)
        val radius = if (scale > 0.01f) screenRadius / scale else 0f
        return Outline.Rounded(RoundRect(0f, top, w, top + rh, CornerRadius(radius)))
    }
}
