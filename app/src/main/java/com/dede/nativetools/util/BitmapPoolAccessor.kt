package com.dede.nativetools.util

import android.graphics.Bitmap
import com.bumptech.glide.Glide

/**
 * Created by shhu on 2022/7/20 11:49.
 *
 * @since 2022/7/20
 */
object BitmapPoolAccessor {

    private val bitmapPool by lazy { Glide.get(globalContext).bitmapPool }

    fun get(width: Int, height: Int, config: Bitmap.Config): Bitmap {
        return bitmapPool.get(width, height, config)
    }

    fun recycle(bitmap: Bitmap?) {
        if (bitmap == null) return
        bitmapPool.put(bitmap)
    }
}