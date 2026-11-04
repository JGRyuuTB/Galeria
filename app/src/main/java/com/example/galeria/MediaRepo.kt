package com.example.galeria

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MediaFile(
    val id: Long,
    val uri: Uri,
    val isVideo: Boolean,
    val name: String,
    val dateMillis: Long,
    val durationMs: Long,
    val size: Long,
    val width: Int,
    val height: Int,
    val mime: String,
    val bucketId: Long,
    val bucket: String,
)

data class Album(
    val id: Long,
    val name: String,
    val cover: MediaFile,
    val count: Int,
    val items: List<MediaFile>,
)

object MediaRepo {

    fun load(context: Context): List<MediaFile> {
        val result = ArrayList<MediaFile>()
        result += query(context, video = false)
        result += query(context, video = true)
        result.sortByDescending { it.dateMillis }
        return result
    }

    fun albums(all: List<MediaFile>): List<Album> =
        all.groupBy { it.bucketId }
            .map { (id, list) ->
                Album(
                    id = id,
                    name = list.first().bucket.ifBlank { "Sin nombre" },
                    cover = list.first(),
                    count = list.size,
                    items = list,
                )
            }
            .sortedByDescending { it.cover.dateMillis }

    private fun query(context: Context, video: Boolean): List<MediaFile> {
        val base: Uri =
            if (video) MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val colId = MediaStore.MediaColumns._ID
        val colName = MediaStore.MediaColumns.DISPLAY_NAME
        val colAdded = MediaStore.MediaColumns.DATE_ADDED
        val colSize = MediaStore.MediaColumns.SIZE
        val colMime = MediaStore.MediaColumns.MIME_TYPE
        val colWidth = MediaStore.MediaColumns.WIDTH
        val colHeight = MediaStore.MediaColumns.HEIGHT
        val colTaken =
            if (video) MediaStore.Video.Media.DATE_TAKEN else MediaStore.Images.Media.DATE_TAKEN
        val colBucketId =
            if (video) MediaStore.Video.Media.BUCKET_ID else MediaStore.Images.Media.BUCKET_ID
        val colBucket =
            if (video) MediaStore.Video.Media.BUCKET_DISPLAY_NAME
            else MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        val colDuration = MediaStore.Video.Media.DURATION

        val projection = mutableListOf(
            colId, colName, colAdded, colSize, colMime, colWidth, colHeight,
            colTaken, colBucketId, colBucket,
        )
        if (video) projection += colDuration

        val out = ArrayList<MediaFile>()
        try {
            context.contentResolver.query(base, projection.toTypedArray(), null, null, null)?.use { c ->
                val iId = c.getColumnIndexOrThrow(colId)
                val iName = c.getColumnIndexOrThrow(colName)
                val iAdded = c.getColumnIndexOrThrow(colAdded)
                val iSize = c.getColumnIndexOrThrow(colSize)
                val iMime = c.getColumnIndexOrThrow(colMime)
                val iWidth = c.getColumnIndexOrThrow(colWidth)
                val iHeight = c.getColumnIndexOrThrow(colHeight)
                val iTaken = c.getColumnIndexOrThrow(colTaken)
                val iBucketId = c.getColumnIndexOrThrow(colBucketId)
                val iBucket = c.getColumnIndexOrThrow(colBucket)
                val iDuration = if (video) c.getColumnIndexOrThrow(colDuration) else -1

                while (c.moveToNext()) {
                    val id = c.getLong(iId)
                    val taken = c.getLong(iTaken)
                    val date = if (taken > 0) taken else c.getLong(iAdded) * 1000L
                    out += MediaFile(
                        id = id,
                        uri = ContentUris.withAppendedId(base, id),
                        isVideo = video,
                        name = c.getString(iName) ?: "",
                        dateMillis = date,
                        durationMs = if (video) c.getLong(iDuration) else 0L,
                        size = c.getLong(iSize),
                        width = c.getInt(iWidth),
                        height = c.getInt(iHeight),
                        mime = c.getString(iMime) ?: if (video) "video/*" else "image/*",
                        bucketId = c.getLong(iBucketId),
                        bucket = c.getString(iBucket) ?: "",
                    )
                }
            }
        } catch (e: Exception) {
            return emptyList()
        }
        return out
    }
}

// ------------------------------------------------------------------ Formato

fun formatDuration(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

fun formatSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1 -> "%.2f GB".format(gb)
        mb >= 1 -> "%.1f MB".format(mb)
        else -> "%.0f KB".format(kb)
    }
}

fun dayKey(millis: Long): Int {
    val c = Calendar.getInstance().apply { timeInMillis = millis }
    return c.get(Calendar.YEAR) * 1000 + c.get(Calendar.DAY_OF_YEAR)
}

fun dayLabel(millis: Long): String {
    val now = System.currentTimeMillis()
    val today = dayKey(now)
    val yesterday = dayKey(now - 86_400_000L)
    val key = dayKey(millis)
    return when (key) {
        today -> "Hoy"
        yesterday -> "Ayer"
        else -> {
            val pattern = if (key / 1000 == today / 1000) "EEE, d MMM" else "EEE, d MMM yyyy"
            SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
                .replaceFirstChar { it.uppercase() }
        }
    }
}

fun formatDate(millis: Long, pattern: String): String =
    SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
