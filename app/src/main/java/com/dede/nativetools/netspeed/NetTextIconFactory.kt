package com.dede.nativetools.netspeed

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import androidx.core.graphics.toXfermode
import com.dede.nativetools.NativeToolsApp
import com.dede.nativetools.util.splicing
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

    sealed class IconConfig(val size: Int) {

        val center = size / 2f

        var scale: Float = 1f

        var text1Y: Float = 0f
        var text1Size: Float = 0f

        var text2Y: Float = 0f
        var text2Size: Float = 0f

        var background: String = NetSpeedConfiguration.BACKGROUND_NONE


        class Single(size: Int) : IconConfig(size) {

            init {
                text1Y = size * 0.475f
                text1Size = size * 0.50f

                text2Y = size * 0.87f
                text2Size = size * 0.37f
            }
        }

        class Pair(size: Int) : IconConfig(size) {

            init {
                text1Y = size * 0.42f
                text1Size = size * 0.40f

                text2Y = size * 0.86f
                text2Size = text1Size
            }
        }
    }

    fun createIconBitmap(
        rxSpeed: Long,
        txSpeed: Long,
        configuration: NetSpeedConfiguration = NetSpeedConfiguration.initialize(),
        size: Int = ICON_SIZE,
        fromCache: Boolean = false
    ): Bitmap {
        val text1: String
        val text2: String
        when (configuration.mode) {
            NetSpeedConfiguration.MODE_ALL -> {
                val down =
                    NetUtil.formatBytes(rxSpeed, 0, NetUtil.ACCURACY_EQUAL_WIDTH).splicing()
                val up =
                    NetUtil.formatBytes(txSpeed, 0, NetUtil.ACCURACY_EQUAL_WIDTH).splicing()
                text1 = up
                text2 = down
            }
            NetSpeedConfiguration.MODE_UP -> {
                val upSplit = NetUtil.formatBytes(
                    txSpeed,
                    NetUtil.FLAG_FULL,
                    NetUtil.ACCURACY_EQUAL_WIDTH_EXACT
                )
                text1 = upSplit.first
                text2 = upSplit.second
            }
            else -> {
                val downSplit = NetUtil.formatBytes(
                    rxSpeed,
                    NetUtil.FLAG_FULL,
                    NetUtil.ACCURACY_EQUAL_WIDTH_EXACT
                )
                text1 = downSplit.first
                text2 = downSplit.second
            }
        }

        val iconConfig = when (configuration.mode) {
            NetSpeedConfiguration.MODE_ALL -> {
                IconConfig.Pair(size)
            }
            else -> {
                IconConfig.Single(size)
            }
        }
        iconConfig.scale = configuration.scale
        iconConfig.background = configuration.background

        return createIconInternal(text1, text2, iconConfig, fromCache)
    }

    private val DST_OUT_XFERMODE = PorterDuff.Mode.DST_OUT.toXfermode()

    private fun createIconInternal(
        text1: String,
        text2: String,
        icon: IconConfig,
        fromCache: Boolean = false
    ): Bitmap {
        val bitmap = createBitmap(icon.size, fromCache)
        val canvas = Canvas(bitmap)

        when (icon.background) {
            NetSpeedConfiguration.BACKGROUND_CIRCLE -> {
                canvas.drawOval(
                    0f,
                    0f,
                    icon.size.toFloat(),
                    icon.size.toFloat(),
                    paint
                )
            }
            NetSpeedConfiguration.BACKGROUND_ROUNDED_CORNERS -> {
                canvas.drawRoundRect(
                    0f,
                    0f,
                    icon.size.toFloat(),
                    icon.size.toFloat(),
                    icon.size * 0.15f,
                    icon.size * 0.15f,
                    paint
                )
            }
            NetSpeedConfiguration.BACKGROUND_SQUIRCLE -> {
                val squirclePath = getSquirclePath(0, 0, icon.center.roundToInt())
                canvas.drawPath(squirclePath, paint)
            }
        }
        if (icon.background != NetSpeedConfiguration.BACKGROUND_NONE) {
            paint.xfermode = DST_OUT_XFERMODE
        }

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

}
