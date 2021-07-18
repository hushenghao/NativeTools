package com.dede.nativetools.netspeed

import android.content.Context
import android.graphics.*
import com.dede.nativetools.NativeToolsApp
import kotlin.math.abs


/**
 * Created by hsh on 2017/5/11 011 下午 05:17.
 * 通知栏网速图标工厂
 */
object NetTextIconFactory {

    private val DEFAULT_CONFIG = Bitmap.Config.ARGB_8888
    private var cachedBitmap: Bitmap? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var ICON_SIZE = 72

    init {
        val context = NativeToolsApp.getInstance()
        init(context)
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.WHITE
    }

    private fun createBitmap(size: Int, useCache: Boolean): Bitmap {
        if (!useCache) {
            return Bitmap.createBitmap(size, size, DEFAULT_CONFIG)
        }
        var bitmap = this.cachedBitmap
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(size, size, DEFAULT_CONFIG)
            this.cachedBitmap = bitmap
            return bitmap
        }

        if (bitmap.config != DEFAULT_CONFIG || bitmap.width != size || bitmap.height != size) {
            bitmap.reconfigure(size, size, DEFAULT_CONFIG)
        }

        // Bitmaps in the pool contain random data that in some cases must be cleared for an image
        // to be rendered correctly. we shouldn't force all consumers to independently erase the
        // contents individually, so we do so here.
        bitmap.eraseColor(Color.TRANSPARENT)
        return bitmap
    }


    private fun init(context: Context) {
        val dpi = context.resources.displayMetrics.densityDpi
        when {
            dpi <= 160 -> {// mdpi
                ICON_SIZE = 24
            }
            dpi <= 240 -> {// hdpi
                ICON_SIZE = 36
            }
            dpi <= 320 -> {// xhdpi
                ICON_SIZE = 48
            }
            dpi <= 480 -> {// xxhdpi
                ICON_SIZE = 72
            }
            dpi <= 640 -> {// xxxhdpi
                ICON_SIZE = 96
            }
            else -> {
                ICON_SIZE = 96
            }
        }
    }

    /**
     * 创建下载图标
     *
     * @param text1 下载速度
     * @param text2 单位
     * @param size bitmap大小
     * @return bitmap
     */
    fun createSingleIcon(
        text1: String,
        text2: String,
        scale: Float = 1f,
        size: Int = ICON_SIZE,
        fromCache: Boolean = false
    ): Bitmap {
        val bitmap = createBitmap(size, fromCache)
        val canvas = Canvas(bitmap)
        val half = size / 2f
        canvas.scale(scale, scale, half, half)

        paint.textSize = size * 0.51f
        var metrics = paint.fontMetrics
        val textY = abs(metrics.top) - metrics.descent
        var offset = size * 0.06f
        canvas.drawText(text1, half, textY + offset, paint)

        paint.textSize = size * 0.37f
        metrics = paint.fontMetrics
        val text2Y = abs(metrics.top) - metrics.descent
        offset = size * 0.16f
        canvas.drawText(text2, half, textY + text2Y + offset, paint)
        return bitmap
    }

    /**
     * 创建上传下载的图标
     *
     * @param text1 上行网速
     * @param text2 下行网速
     * @param size bitmap大小
     * @return bitmap
     */
    fun createDoubleIcon(
        text1: String,
        text2: String,
        scale: Float = 1f,
        size: Int = ICON_SIZE,
        fromCache: Boolean = false
    ): Bitmap {
        val bitmap = createBitmap(size, fromCache)
        val canvas = Canvas(bitmap)
        val half = size / 2f
        canvas.scale(scale, scale, half, half)

        paint.textSize = size * 0.42f
        var metrics = paint.fontMetrics
        val textY = abs(metrics.top) - metrics.descent
        var offset = size * 0.05f
        canvas.drawText(text1, half, textY + offset, paint)

        metrics = paint.fontMetrics
        val text2Y = abs(metrics.top) - metrics.descent
        offset = size * 0.21f
        canvas.drawText(text2, half, textY + text2Y + offset, paint)
        return bitmap
    }
}
