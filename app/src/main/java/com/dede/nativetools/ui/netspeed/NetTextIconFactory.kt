package com.dede.nativetools.ui.netspeed

import android.content.Context
import android.graphics.*
import kotlin.math.abs


/**
 * Created by hsh on 2017/5/11 011 下午 05:17.
 * 通知栏网速图标工厂
 */
object NetTextIconFactory {
    var ICON_SIZE = 72
        private set

    fun init(context: Context) {
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
        size: Int = ICON_SIZE
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.WHITE
        val half = size / 2f

        paint.textSize = size * 0.51f
        var metrics = paint.fontMetrics
        val textY = abs(metrics.top) - metrics.descent
        canvas.scale(scale, scale, half, half)
        var offset = size * 0.06f
        canvas.drawText(text1, half, textY + offset, paint)

        paint.textSize = size * 0.39f
        metrics = paint.fontMetrics
        val text2Y = abs(metrics.top) - metrics.descent
        offset = size * 0.15f
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
        size: Int = ICON_SIZE
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = size * 0.42f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.WHITE
        val half = size / 2f

        var metrics = paint.fontMetrics
        val textY = abs(metrics.top) - metrics.descent
        canvas.scale(scale, scale, half, half)
        var offset = size * 0.05f
        canvas.drawText(text1, half, textY + offset, paint)

        metrics = paint.fontMetrics
        val text2Y = abs(metrics.top) - metrics.descent
        offset = size * 0.21f
        canvas.drawText(text2, half, textY + text2Y + offset, paint)
        return bitmap
    }
}
