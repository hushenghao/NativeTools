package com.dede.nativetools.netspeed

import android.graphics.*
import android.util.DisplayMetrics
import androidx.core.graphics.toXfermode
import com.dede.nativetools.netspeed.utils.NetFormatter
import com.dede.nativetools.util.displayMetrics
import com.dede.nativetools.util.splicing
import kotlin.math.abs
import kotlin.math.roundToInt


/**
 * Created by hsh on 2017/5/11 011 下午 05:17.
 * 通知栏网速图标工厂
 */
object NetTextIconFactory {

    private val DEFAULT_CONFIG = Bitmap.Config.ARGB_8888

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.DEFAULT_BOLD
        //isFakeBoldText = true
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
    }

    private val iconSize: Int

    init {
        val dpi = displayMetrics().densityDpi
        this.iconSize = when {
            dpi <= DisplayMetrics.DENSITY_MEDIUM -> 24 // mdpi
            dpi <= DisplayMetrics.DENSITY_HIGH -> 36 // hdpi
            dpi <= DisplayMetrics.DENSITY_XHIGH -> 48 // xhdpi
            dpi <= DisplayMetrics.DENSITY_XXHIGH -> 72 // xxhdpi
            dpi <= DisplayMetrics.DENSITY_XXXHIGH -> 96 // xxxhdpi
            else -> 96
        }
    }

    private fun createBitmapInternal(size: Int, cache: Bitmap?): Bitmap {
        if (cache == null) {
            return Bitmap.createBitmap(size, size, DEFAULT_CONFIG)
        }
        if (cache.config != DEFAULT_CONFIG || cache.width != size || cache.height != size) {
            cache.reconfigure(size, size, DEFAULT_CONFIG)
        }

        // Bitmaps in the pool contain random data that in some cases must be cleared for an image
        // to be rendered correctly. we shouldn't force all consumers to independently erase the
        // contents individually, so we do so here.
        cache.eraseColor(Color.TRANSPARENT)
        return cache
    }

    private sealed class IconConfig(val size: Int) {

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

    /**
     * 创建网速图标
     *
     * @param rxSpeed 下行网速
     * @param txSpeed 上行网速
     * @param configuration 图标配置
     * @param size Bitmap大小
     */
    fun createIconBitmap(
        rxSpeed: Long,
        txSpeed: Long,
        configuration: NetSpeedConfiguration,
        size: Int = iconSize
    ): Bitmap {
        val text1: String
        val text2: String
        when (configuration.mode) {
            NetSpeedConfiguration.MODE_ALL -> {
                text1 = NetFormatter.format(
                    rxSpeed,
                    NetFormatter.FLAG_NULL,
                    NetFormatter.ACCURACY_EQUAL_WIDTH
                ).splicing()
                text2 = NetFormatter.format(
                    txSpeed,
                    NetFormatter.FLAG_NULL,
                    NetFormatter.ACCURACY_EQUAL_WIDTH
                ).splicing()
            }
            NetSpeedConfiguration.MODE_UP -> {
                val upSplit = NetFormatter.format(
                    txSpeed,
                    NetFormatter.FLAG_FULL,
                    NetFormatter.ACCURACY_EQUAL_WIDTH_EXACT
                )
                text1 = upSplit.first
                text2 = upSplit.second
            }
            else -> {
                val downSplit = NetFormatter.format(
                    rxSpeed,
                    NetFormatter.FLAG_FULL,
                    NetFormatter.ACCURACY_EQUAL_WIDTH_EXACT
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

        return createIconInternal(text1, text2, iconConfig, configuration.cachedBitmap)
    }

    private val DST_OUT_XFERMODE = PorterDuff.Mode.DST_OUT.toXfermode()

    private fun createIconInternal(
        text1: String,
        text2: String,
        icon: IconConfig,
        cache: Bitmap? = null
    ): Bitmap {
        val bitmap = createBitmapInternal(icon.size, cache)
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
