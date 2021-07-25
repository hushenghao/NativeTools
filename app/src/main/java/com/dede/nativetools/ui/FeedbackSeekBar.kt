package com.dede.nativetools.ui

import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSeekBar

/**
 * 带震动反馈的SeekBar
 */
class FeedbackSeekBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    AppCompatSeekBar(context, attrs) {

    private val listener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            onSeekBarChangeListener?.onProgressChanged(seekBar, progress, fromUser)
            if (fromUser) {
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            onSeekBarChangeListener?.onStartTrackingTouch(seekBar)
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            onSeekBarChangeListener?.onStopTrackingTouch(seekBar)
        }
    }

    init {
        super.setOnSeekBarChangeListener(listener)
    }

    private var onSeekBarChangeListener: OnSeekBarChangeListener? = null

    override fun setOnSeekBarChangeListener(listener: OnSeekBarChangeListener?) {
        this.onSeekBarChangeListener = listener
    }
}