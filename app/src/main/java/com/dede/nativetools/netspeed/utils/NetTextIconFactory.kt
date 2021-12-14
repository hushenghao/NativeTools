package com.dede.nativetools.netspeed.utils

import android.content.res.Resources
import android.graphics.*
import android.util.DisplayMetrics
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toXfermode
import com.dede.nativetools.R
import com.dede.nativetools.netspeed.NetSpeedConfiguration
import com.dede.nativetools.util.globalContext
import com.dede.nativetools.util.splicing


/**
 * Created by hsh on 2017/5/11 011 下午 05:17.
 * 通知栏网速图标工厂
 */
object NetTextIconFactory {

    private val DEFAULT_CONFIG = Bitmap.Config.ARGB_8888

    private const val DEBUG_MODE = false

    // 888M 931135488L
    private const val DEBUG_MODE_ALL_BYTES = (2 shl 19) * 888L

    // 88.8M 93113549L
    private const val DEBUG_MODE_SINGLE_BYTES = ((2 shl 19) * 88.8F).toLong()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        //typeface = Typeface.DEFAULT_BOLD
        //isFakeBoldText = true
        typeface = ResourcesCompat.getFont(globalContext, R.font.oswald_bold)
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
    }

    private val iconSize: Int

    init {
        // android.R.dimen.status_bar_icon_size
        val resources = Resources.getSystem()
        val id = resources.getIdentifier("status_bar_icon_size", "dimen", "android")
        val statusBarIconSize = id.runCatching(resources::getDimensionPixelSize)
            .onFailure(Throwable::printStackTrace)
            .getOrElse {
                val dpi = resources.displayMetrics.densityDpi
                when {
                    dpi <= DisplayMetrics.DENSITY_MEDIUM -> 24 // mdpi
                    dpi <= DisplayMetrics.DENSITY_HIGH -> 36 // hdpi
                    dpi <= DisplayMetrics.DENSITY_XHIGH -> 48 // xhdpi
                    dpi <= DisplayMetrics.DENSITY_XXHIGH -> 72 // xxhdpi
                    dpi <= DisplayMetrics.DENSITY_XXXHIGH -> 96 // xxxhdpi
                    else -> 96
                }
            }
        Log.i("NetTextIconFactory", "status_bar_icon_size: $statusBarIconSize")
        iconSize = statusBarIconSize
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

        var text1Y: Float = 0f
        var text1Size: Float = 0f

        var text2Y: Float = 0f
        var text2Size: Float = 0f

        class Single(size: Int) : IconConfig(size) {

            init {
                text1Y = size * 0.51f
                text1Size = size * 0.57f

                text2Y = size * 0.965f
                text2Size = size * 0.39f
            }
        }

        class Pair(size: Int) : IconConfig(size) {

            init {
                text1Y = size * 0.44f
                text1Size = size * 0.46f

                text2Y = size * 0.95f
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
        var rxByte = rxSpeed
        var txByte = txSpeed
        if (DEBUG_MODE) {
            // Check that the text is displayed completely
            if (configuration.mode == NetSpeedConfiguration.MODE_ALL) {
                rxByte = DEBUG_MODE_ALL_BYTES
                txByte = DEBUG_MODE_ALL_BYTES
            } else {
                rxByte = DEBUG_MODE_SINGLE_BYTES
                txByte = DEBUG_MODE_SINGLE_BYTES
            }
        }

        val text1: String
        val text2: String
        when (configuration.mode) {
            NetSpeedConfiguration.MODE_ALL -> {
                text1 = NetFormatter.format(
                    txByte,
                    NetFormatter.FLAG_NULL,
                    NetFormatter.ACCURACY_EQUAL_WIDTH
                ).splicing()
                text2 = NetFormatter.format(
                    rxByte,
                    NetFormatter.FLAG_NULL,
                    NetFormatter.ACCURACY_EQUAL_WIDTH
                ).splicing()
            }
            NetSpeedConfiguration.MODE_UP -> {
                val upSplit = NetFormatter.format(
                    txByte,
                    NetFormatter.FLAG_FULL,
                    NetFormatter.ACCURACY_EQUAL_WIDTH_EXACT
                )
                text1 = upSplit.first
                text2 = upSplit.second
            }
            else -> {
                val downSplit = NetFormatter.format(
                    rxByte,
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

        return createIconInternal(text1, text2, iconConfig, configuration.cachedBitmap).apply {
            configuration.cachedBitmap = this
        }
    }

    private fun createIconInternal(
        text1: String,
        text2: String,
        icon: IconConfig,
        cache: Bitmap? = null
    ): Bitmap {
        val bitmap = createBitmapInternal(icon.size, cache)
        val canvas = Canvas(bitmap)

        if (DEBUG_MODE) {
            canvas.drawRoundRect(
                0f,
                0f,
                icon.size.toFloat(),
                icon.size.toFloat(),
                icon.size * 0.15f,
                icon.size * 0.15f,
                paint
            )
            paint.xfermode = PorterDuff.Mode.DST_OUT.toXfermode()
        }

        paint.textSize = icon.text1Size
        canvas.drawText(text1, icon.center, icon.text1Y, paint)

        paint.textSize = icon.text2Size
        canvas.drawText(text2, icon.center, icon.text2Y, paint)

        if (DEBUG_MODE) {
            paint.xfermode = null
        }
        return bitmap
    }

}
