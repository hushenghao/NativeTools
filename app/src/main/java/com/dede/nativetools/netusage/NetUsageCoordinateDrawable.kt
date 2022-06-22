package com.dede.nativetools.netusage

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.TypedValue
import com.dede.nativetools.netspeed.utils.NetFormatter
import com.dede.nativetools.util.dpf
import com.dede.nativetools.util.splicing
import com.google.android.material.R
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * 网络使用柱状图坐标系Drawable
 */
class NetUsageCoordinateDrawable(val context: Context, max: Long) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val xLineArr: Array<Line>
    private val textWidth: Float

    private val padding: RectF = RectF()

    private val arrowSize: Float
    private val labelPadding: Float

    val paddingLeft get() = padding.left.roundToInt()
    val paddingTop get() = padding.top.roundToInt()
    val paddingRight get() = padding.right.roundToInt()
    val paddingBottom get() = padding.bottom.roundToInt()

    init {
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.RIGHT

        arrowSize = 6.dpf
        labelPadding = 4.dpf

        val value = TypedValue()
        context.theme.resolveAttribute(R.attr.textAppearanceBodySmall, value, true)
        val arr = intArrayOf(android.R.attr.textSize, android.R.attr.textColor)
        val typeArray = context.obtainStyledAttributes(value.resourceId, arr)
        paint.textSize = typeArray.getDimension(0, 0f)
        paint.color = @Suppress("ResourceType") typeArray.getColor(1, Color.WHITE)
        typeArray.recycle()

        val xLineArr = arrayOfNulls<Line>(5)
        xLineArr[0] = Line(0L, 1f, true)
        val split = xLineArr.size - 1
        val div = max.toDouble().div(split)
        var textWidth = 0f
        for (i in 1 until xLineArr.size) {
            val line = Line((div * i).roundToLong(), 1f - i.toFloat() / split)
            xLineArr[i] = line
            textWidth = max(paint.measureText(line.label), textWidth)
        }
        this.textWidth = textWidth
        val fontMetrics = paint.fontMetrics
        padding.set(
            textWidth + labelPadding,// y轴字体宽度 + 文字边距
            -fontMetrics.top + arrowSize,// y轴文字高度 + y轴箭头
            arrowSize,// x轴箭头
            fontMetrics.bottom - fontMetrics.top + labelPadding// x轴文字高度 + 文字边距
        )
        this.xLineArr = xLineArr.requireNoNulls()
    }

    private class Line(
        bytes: Long,
        val scale: Float,
        val isFull: Boolean = false,
    ) {
        val label = NetFormatter.format(bytes,
            NetFormatter.FLAG_NULL,
            NetFormatter.ACCURACY_SHORTER).splicing()
    }

    override fun onBoundsChange(bounds: Rect) {
        invalidateSelf()
    }

    override fun getIntrinsicWidth(): Int {
        return bounds.width()
    }

    override fun getIntrinsicHeight(): Int {
        return bounds.height()
    }

    private val pathEffect = DashPathEffect(floatArrayOf(3.dpf, 3.dpf), 0f)
    private val path = Path()

    override fun draw(canvas: Canvas) {
        val w: Float = intrinsicWidth - padding.left - padding.right
        val h: Float = intrinsicHeight - padding.top - padding.bottom

        // 画y轴
        paint.strokeWidth = 1.5.dpf
        paint.pathEffect = null
        canvas.drawLine(padding.left, arrowSize, padding.left, h + padding.top, paint)

        // 画x轴，y轴刻度
        for (line in xLineArr) {
            if (line.isFull) {
                // x轴
                paint.strokeWidth = 1.5.dpf
                paint.pathEffect = null
            } else {
                paint.strokeWidth = 0.5.dpf
                paint.pathEffect = pathEffect
            }
            // 画水平线
            val y = h * line.scale + padding.top
            canvas.drawLine(padding.left, y, w + padding.left, y, paint)
            // 画y轴刻度
            canvas.drawText(line.label, textWidth, y, paint)
        }

        // 画圆点
        canvas.drawCircle(padding.left, h + padding.top, 1.5.dpf, paint)

        // y轴箭头
        path.reset()
        path.moveTo(padding.left, 0f)
        path.lineTo(padding.left - arrowSize / 2, arrowSize)
        path.lineTo(padding.left + arrowSize / 2, arrowSize)
        path.close()
        canvas.drawPath(path, paint)

        // x轴箭头
        path.reset()
        path.moveTo(intrinsicWidth.toFloat(), h + padding.top)
        path.lineTo(intrinsicWidth - arrowSize, h + padding.top - arrowSize / 2)
        path.lineTo(intrinsicWidth - arrowSize, h + padding.top + arrowSize / 2)
        path.close()
        canvas.drawPath(path, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }
}