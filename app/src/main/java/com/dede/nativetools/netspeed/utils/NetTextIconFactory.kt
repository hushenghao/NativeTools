package com.dede.nativetools.netspeed.utils

import android.content.res.Resources
import android.graphics.*
import android.util.DisplayMetrics
import android.util.Log
import com.dede.nativetools.netspeed.NetSpeedConfiguration
import com.dede.nativetools.netspeed.typeface.TypefaceGetter
import com.dede.nativetools.util.dpf
import com.dede.nativetools.util.globalContext
import com.dede.nativetools.util.saveToAlbum
import com.dede.nativetools.util.splicing
import kotlin.math.max
import kotlin.math.roundToInt


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
        //isFakeBoldText = true
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val iconSize: Int

    init {
        val resources = Resources.getSystem()
        val dpi = resources.displayMetrics.densityDpi
        val default = when {
            dpi <= DisplayMetrics.DENSITY_MEDIUM -> 24 // mdpi
            dpi <= DisplayMetrics.DENSITY_HIGH -> 36 // hdpi
            dpi <= DisplayMetrics.DENSITY_XHIGH -> 48 // xhdpi
            dpi <= DisplayMetrics.DENSITY_XXHIGH -> 72 // xxhdpi
            dpi <= DisplayMetrics.DENSITY_XXXHIGH -> 96 // xxxhdpi
            else -> 96
        }

        // android.R.dimen.status_bar_icon_size
        val id = resources.getIdentifier("status_bar_icon_size", "dimen", "android")
        val statusBarIconSize = id.runCatching(resources::getDimensionPixelSize)
            .onFailure(Throwable::printStackTrace)
            .getOrDefault(default)

        iconSize = max(statusBarIconSize, default)
        Log.i("NetTextIconFactory", "status_bar_icon_size: $iconSize")
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

    /**
     * 创建网速图标
     *
     * @param rxSpeed 下行网速
     * @param txSpeed 上行网速
     * @param configuration 图标配置
     * @param size Bitmap大小
     */
    fun create(
        rxSpeed: Long,
        txSpeed: Long,
        configuration: NetSpeedConfiguration,
        size: Int = iconSize,
        assistLine: Boolean = DEBUG_MODE
    ): Bitmap {
        var rxByte = rxSpeed
        var txByte = txSpeed
        if (assistLine) {
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

        return createIconInternal(
            text1,
            text2,
            size,
            configuration,
            assistLine
        ).apply {
            configuration.cachedBitmap = this
        }
    }

    private val pathEffect = DashPathEffect(floatArrayOf(2.dpf, 2.dpf), 0f)
    private val rect = Rect()

    private fun createIconInternal(
        text1: String,
        text2: String,
        size: Int,
        configuration: NetSpeedConfiguration,
        assistLine: Boolean = false
    ): Bitmap {
        val w = size
        val wf = w.toFloat()
        val wh = w / 2f
        val h = size
        val hf = h.toFloat()
        val hh = h / 2f

        val verticalOffset: Float = configuration.verticalOffset
        val horizontalOffset: Float = configuration.horizontalOffset
        val relativeRatio: Float = configuration.relativeRatio
        val relativeDistance: Float = configuration.relativeDistance
        val textScale: Float = configuration.textScale

        val bitmap = createBitmapInternal(size, configuration.cachedBitmap)
        val canvas = Canvas(bitmap)
        paint.typeface = TypefaceGetter.getOrDefault(configuration.font, configuration.textStyle)
        resetPaint()

        val yOffset = hf * verticalOffset
        val xOffset = wf * horizontalOffset
        val distance = hf * relativeDistance / 2f

        paint.textSize = w * relativeRatio * textScale// 缩放
        var textY = relativeRatio * hf - distance + yOffset
        val textX = wh + xOffset
        canvas.drawText(text1, textX, textY, paint)
        //if (assistLine) {
        //    drawTextRound(text1, textX, textY, canvas)
        //}

        paint.textSize = w * (1 - relativeRatio) * textScale// 缩放
        paint.getTextBounds(text2, 0, text2.length, rect)
        textY = hf * relativeRatio + rect.height() + distance + yOffset
        canvas.drawText(text2, textX, textY, paint)
        //if (assistLine) {
        //    drawTextRound(text2, textX, textY, canvas)
        //}

        if (assistLine) {
            // 居中辅助线
            paint.style = Paint.Style.STROKE
            paint.color = Color.YELLOW
            paint.strokeWidth = 1.5f.dpf
            paint.pathEffect = pathEffect
            canvas.drawLine(wh, 0f, wh, hf, paint)
            canvas.drawLine(0f, hh, wf, hh, paint)
            // 边框
            paint.pathEffect = null
            canvas.drawRect(0f, 0f, wf, hf, paint)
        }
        return bitmap
    }

    private fun resetPaint() {
        paint.pathEffect = null
        paint.strokeWidth = 0.5f.dpf
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
    }

    private fun drawTextRound(text: String, textX: Float, textY: Float, canvas: Canvas) {
        val rect = Rect()
        // 斜体字边框测量不准确???
        paint.getTextBounds(text, 0, text.length, rect)
        paint.style = Paint.Style.STROKE
        paint.color = Color.WHITE
        paint.strokeWidth = 0.5f.dpf
        paint.pathEffect = pathEffect
        // 画笔字体对齐为方式为center
        val offsetX = textX - rect.width() / 2f - rect.left / 2f// 字体左边会有边距
        rect.offset(offsetX.roundToInt(), textY.roundToInt())
        canvas.drawRect(rect, paint)

        resetPaint()
    }

    private fun createLauncherForeground() {
        val bitmap =
            createIconInternal("18.8", "KB/s", 512, NetSpeedConfiguration.defaultConfiguration)
        bitmap.saveToAlbum(globalContext, "ic_launcher_foreground.png", "Net Monitor")
    }

}
