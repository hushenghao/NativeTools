package com.dede.nativetools.ui

import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.widget.SeekBar
import androidx.preference.PreferenceViewHolder
import androidx.preference.SeekBarPreference
import com.dede.nativetools.R
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider

class SliderPreference @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    SeekBarPreference(context, attrs) {

    init {
        layoutResource = R.layout.override_preference_widget_seekbar
    }

    override fun onBindViewHolder(view: PreferenceViewHolder) {
        super.onBindViewHolder(view)
        val seekBar = view.findViewById(R.id.seekbar) as SeekBar
        val slider = view.findViewById(R.id.slider) as Slider

        val impl = Impl(seekBar)
        slider.apply {
            valueTo = (max - min).toFloat()
            valueFrom = 0f
            value = seekBar.progress.toFloat()
            stepSize = 1f

            setLabelFormatter(impl)
            clearOnChangeListeners()
            clearOnSliderTouchListeners()
            addOnChangeListener(impl)
            addOnSliderTouchListener(impl)
        }
    }

    private inner class Impl(val seekBar: SeekBar) :
        Slider.OnSliderTouchListener, Slider.OnChangeListener, LabelFormatter {

        override fun onStartTrackingTouch(slider: Slider) {
        }

        override fun onStopTrackingTouch(slider: Slider) {
            value = min + seekBar.progress
        }

        override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
            seekBar.progress = value.toInt()
            if (fromUser) {
                slider.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        }

        override fun getFormattedValue(value: Float): String {
            return (min + value).toInt().toString() + " %"
        }
    }
}