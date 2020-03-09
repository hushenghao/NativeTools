package com.dede.nativetools.ui.netspeed

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import com.dede.nativetools.NativeToolsApp


/**
 * Created by hsh on 2017/5/11 011 下午 05:17.
 * 通知栏网速图标工厂
 */
object NetTextIconFactory {

    private const val WIDTH = 100
    private const val HEIGHT = 100

    private val factoryTextView = object : AppCompatTextView(NativeToolsApp.getInstance()) {

        init {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, 55f)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            includeFontPadding = false
            layoutParams = ViewGroup.LayoutParams(WIDTH, HEIGHT)
            setTextColor(Color.WHITE)
            maxLines = 2

            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this,
                25, 55, 5, TypedValue.COMPLEX_UNIT_PX
            )
            TextViewCompat.setAutoSizeTextTypeWithDefaults(
                this,
                TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
            )
        }

        private val canvas = Canvas()

        fun drawBitmap(): Bitmap {
            val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
            canvas.setBitmap(bitmap)
            measure(
                View.MeasureSpec.makeMeasureSpec(WIDTH, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(WIDTH, View.MeasureSpec.AT_MOST)
            )
            layout(0, 0, WIDTH, HEIGHT)
            draw(canvas)
            return bitmap
        }
    }

    /**
     * 创建下载图标
     *
     * @param text1 下载速度
     * @param text2 单位
     * @return bitmap
     */
    fun createSingleIcon(text1: String, text2: String): Bitmap {
        factoryTextView.text = SpannableStringBuilder()
            .append(text1)
            .append("\n")
            .append(text2, RelativeSizeSpan(0.7f), Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        return factoryTextView.drawBitmap()
    }

    /**
     * 创建上传下载的图标
     *
     * @param text1 上行网速
     * @param text2 下行网速
     * @return bitmap
     */
    @SuppressLint("SetTextI18n")
    fun createDoubleIcon(text1: String, text2: String): Bitmap {
        factoryTextView.text = "${text1}\n${text2}"
        return factoryTextView.drawBitmap()
    }
}
