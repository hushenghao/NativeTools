package com.dede.nativetools.netspeed

import android.content.Context
import android.graphics.*
import com.dede.nativetools.NativeToolsApp


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

    sealed class IconConfig(val scale: Float, val size: Int) {

        val center = size / 2f

        var text1Y: Float = 0f
        var text1Size: Float = 0f

        var text2Y: Float = 0f
        var text2Size: Float = 0f


        class Single(scale: Float, size: Int) : IconConfig(scale, size) {

            init {
                text1Y = size * 0.5f
                text1Size = size * 0.51f

                text2Y = size * 0.87f
                text2Size = size * 0.37f
            }
        }

        class Pair(scale: Float, size: Int) : IconConfig(scale, size) {

            init {
                text1Y = size * 0.42f
                text1Size = size * 0.41f

                text2Y = size * 0.86f
                text2Size = text1Size
            }
        }
    }

    fun createIconInternal(
        text1: String,
        text2: String,
        icon: IconConfig,
        fromCache: Boolean
    ): Bitmap {
        val bitmap = createBitmap(icon.size, fromCache)
        val canvas = Canvas(bitmap)

        canvas.scale(icon.scale, icon.scale, icon.center, icon.center)

        paint.textSize = icon.text1Size
        canvas.drawText(text1, icon.center, icon.text1Y, paint)

        paint.textSize = icon.text2Size
        canvas.drawText(text2, icon.center, icon.text2Y, paint)
        return bitmap
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
        return createIconInternal(text1, text2, IconConfig.Single(scale, size), fromCache)
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
        return createIconInternal(text1, text2, IconConfig.Pair(scale, size), fromCache)
    }
}
