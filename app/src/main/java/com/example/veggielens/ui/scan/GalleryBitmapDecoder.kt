package com.example.veggielens.ui.scan

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import okio.IOException
import kotlin.math.max
import kotlin.math.roundToInt

internal fun ContentResolver.decodeGalleryBitmap(
    uri: Uri,
    maxDimension: Int = 1600
): Bitmap {
    require(maxDimension > 0)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(this, uri)
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val width = info.size.width
            val height = info.size.height
            val largestSide = max(width, height)
            if (largestSide > maxDimension) {
                val scale = maxDimension.toFloat() / largestSide
                decoder.setTargetSize(
                    (width * scale).roundToInt().coerceAtLeast(1),
                    (height * scale).roundToInt().coerceAtLeast(1)
                )
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    } else {
        decodeLegacyBitmap(uri, maxDimension)
    }
}

private fun ContentResolver.decodeLegacyBitmap(
    uri: Uri,
    maxDimension: Int
): Bitmap {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        ?: throw IOException("无法打开所选图片")

    var sampleSize = 1
    while (max(bounds.outWidth, bounds.outHeight) / sampleSize > maxDimension * 2) {
        sampleSize *= 2
    }

    val option = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    return openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, option)
    } ?: throw IOException("无法解码所选图片")
}