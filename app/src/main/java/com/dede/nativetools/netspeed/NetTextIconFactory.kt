package com.dede.nativetools.netspeed

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import com.dede.nativetools.NativeToolsApp
import kotlin.math.abs
import kotlin.math.roundToInt


/**
 * Created by hsh on 2017/5/11 011 下午 05:17.
 * 通知栏网速图标工厂
 */
@SuppressLint("StaticFieldLeak")
object NetTextIconFactory {

    private val DEFAULT_CONFIG = Bitmap.Config.ARGB_8888
    private var cachedBitmap: Bitmap? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var ICON_SIZE = 72

    private lateinit var context: Context

    init {
        val context = NativeToolsApp.getInstance()
        init(context)
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.isFakeBoldText = true
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
        this.context = context
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
                text1Y = size * 0.475f
                text1Size = size * 0.50f

                text2Y = size * 0.87f
                text2Size = size * 0.37f
            }
        }

        class Pair(scale: Float, size: Int) : IconConfig(scale, size) {

            init {
                text1Y = size * 0.42f
                text1Size = size * 0.40f

                text2Y = size * 0.86f
                text2Size = text1Size
            }
        }
    }

    private val xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)

    fun createIconInternal(
        text1: String,
        text2: String,
        icon: IconConfig,
        fromCache: Boolean
    ): Bitmap {
        val bitmap = createBitmap(icon.size, fromCache)
        val canvas = Canvas(bitmap)

        val squirclePath = getSquirclePath(0, 0, icon.center.roundToInt())
        canvas.drawPath(squirclePath, paint)

        paint.xfermode = xfermode

        canvas.scale(icon.scale, icon.scale, icon.center, icon.center)

        paint.textSize = icon.text1Size
        canvas.drawText(text1, icon.center, icon.text1Y, paint)

        paint.textSize = icon.text2Size
        canvas.drawText(text2, icon.center, icon.text2Y, paint)
        paint.xfermode = null
        return bitmap
    }

    private fun getSquirclePath(left: Int, top: Int, radius: Int): Path {
        //Formula: (|x|)^3 + (|y|)^3 = radius^3
        val radiusToPow = (radius * radius * radius).toDouble()
        val path = Path()
        path.moveTo((-radius).toFloat(), 0f)
        for (x in -radius..radius)
            path.lineTo(
                x.toFloat(),
                Math.cbrt(radiusToPow - abs(x * x * x)).toFloat()
            )
        for (x in radius downTo -radius)
            path.lineTo(
                x.toFloat(),
                (-Math.cbrt(radiusToPow - abs(x * x * x))).toFloat()
            )
        path.close()
        val matrix = Matrix()
        matrix.postTranslate((left + radius).toFloat(), (top + radius).toFloat())
        path.transform(matrix)
        return path
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
