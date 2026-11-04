package com.example.galeria

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.util.Size
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import coil.size.Dimension

/** Parámetro de la petición que activa las miniaturas rápidas del sistema. */
const val THUMB_PARAM = "galeria_thumb"

/**
 * Pide a MediaStore la miniatura ya generada (y cacheada por el sistema) en vez de decodificar
 * la foto o el video completos. Es lo que hace que el desplazamiento por la galería sea fluido.
 * Si el sistema no puede darla (Android 9, papelera, etc.) se usa el cargador normal de Coil.
 */
class MediaThumbFetcher(
    private val context: Context,
    private val uri: Uri,
    private val px: Int,
) : Fetcher {
    override suspend fun fetch(): FetchResult? {
        return try {
            val bitmap = context.contentResolver.loadThumbnail(uri, Size(px, px), null)
            DrawableResult(
                drawable = BitmapDrawable(context.resources, bitmap),
                isSampled = true,
                dataSource = DataSource.DISK,
            )
        } catch (e: Exception) {
            null
        }
    }

    class Factory(private val context: Context) : Fetcher.Factory<Uri> {
        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
            if (options.parameters.value(THUMB_PARAM) != true) return null
            if (data.authority != "media") return null
            if (data.getQueryParameter("includeTrashed") != null) return null
            val px = (options.size.width as? Dimension.Pixels)?.px ?: return null
            return MediaThumbFetcher(context.applicationContext, data, px)
        }
    }
}
