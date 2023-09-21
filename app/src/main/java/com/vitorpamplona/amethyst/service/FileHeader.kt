package com.vitorpamplona.amethyst.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.vitorpamplona.amethyst.ui.actions.ImageDownloader
import com.vitorpamplona.quartz.crypto.CryptoUtils
import com.vitorpamplona.quartz.encoders.toHexKey
import io.trbl.blurhash.BlurHash
import kotlin.math.roundToInt

class FileHeader(
    val url: String,
    val mimeType: String?,
    val hash: String,
    val size: Int,
    val dim: String?,
    val blurHash: String?,
    val alt: String? = null,
    val sensitiveContent: Boolean = false
) {
    companion object {
        suspend fun prepare(fileUrl: String, mimeType: String?, alt: String?, sensitiveContent: Boolean, onReady: (FileHeader) -> Unit, onError: () -> Unit) {
            try {
                val imageData: ByteArray? = ImageDownloader().waitAndGetImage(fileUrl)

                if (imageData != null) {
                    prepare(imageData, fileUrl, mimeType, alt, sensitiveContent, onReady, onError)
                } else {
                    onError()
                }
            } catch (e: Exception) {
                Log.e("ImageDownload", "Couldn't download image from server: ${e.message}")
                onError()
            }
        }

        fun prepare(
            data: ByteArray,
            fileUrl: String,
            mimeType: String?,
            alt: String?,
            sensitiveContent: Boolean,
            onReady: (FileHeader) -> Unit,
            onError: () -> Unit
        ) {
            try {
                val hash = CryptoUtils.sha256(data).toHexKey()
                val size = data.size

                val (blurHash, dim) = if (mimeType?.startsWith("image/") == true) {
                    val opt = BitmapFactory.Options()
                    opt.inPreferredConfig = Bitmap.Config.ARGB_8888
                    val mBitmap = BitmapFactory.decodeByteArray(data, 0, data.size, opt)

                    val intArray = IntArray(mBitmap.width * mBitmap.height)
                    mBitmap.getPixels(
                        intArray,
                        0,
                        mBitmap.width,
                        0,
                        0,
                        mBitmap.width,
                        mBitmap.height
                    )

                    val dim = "${mBitmap.width}x${mBitmap.height}"

                    val aspectRatio = (mBitmap.width).toFloat() / (mBitmap.height).toFloat()

                    if (aspectRatio > 1) {
                        Pair(BlurHash.encode(intArray, mBitmap.width, mBitmap.height, 9, (9 * (1 / aspectRatio)).roundToInt()), dim)
                    } else if (aspectRatio < 1) {
                        Pair(BlurHash.encode(intArray, mBitmap.width, mBitmap.height, (9 * aspectRatio).roundToInt(), 9), dim)
                    } else {
                        Pair(BlurHash.encode(intArray, mBitmap.width, mBitmap.height, 4, 4), dim)
                    }
                } else {
                    Pair(null, null)
                }

                onReady(FileHeader(fileUrl, mimeType, hash, size, dim, blurHash, alt, sensitiveContent))
            } catch (e: Exception) {
                Log.e("ImageDownload", "Couldn't convert image in to File Header: ${e.message}")
                onError()
            }
        }
    }
}
