package com.chac.data.photo.media

import android.content.Context
import android.media.ExifInterface
import androidx.core.net.toUri
import com.chac.domain.photo.media.MediaLocation
import java.io.InputStream

fun getMediaLocation(
    context: Context,
    uri: String,
): MediaLocation? =
    runCatching {
        context.contentResolver.openInputStream(uri.toUri())?.use { stream ->
            extractLocationFromStream(stream)
        }
    }.getOrNull()

private fun extractLocationFromStream(stream: InputStream): MediaLocation? =
    runCatching {
        val exif = ExifInterface(stream)
        val latLong = FloatArray(2)
        if (exif.getLatLong(latLong)) {
            MediaLocation(latLong[0].toDouble(), latLong[1].toDouble())
        } else {
            null
        }
    }.getOrNull()
