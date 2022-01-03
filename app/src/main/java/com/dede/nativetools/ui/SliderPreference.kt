package com.dede.nativetools.ui

import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.dede.nativetools.R
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider
import kotlin.math.max
import kotlin.math.min

/**
 * Slider
 */
class SliderPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.preferenceStyle,
    defStyleRes: Int = 0
) : Preference(context, attrs, defStyleAttr, defStyleRes), Slider.OnChangeListener,
    Slider.OnSliderTouchListener {

    private val stepSize: Float
    private var value: Float = 0f
    private val valueFrom: Float
    private val valueTo: Float

    private var sliderValue: TextView? = null

    var sliderLabelFormatter: LabelFormatter? = null
        set(value) {
            field = value
            notifyChanged()
        }

    var onChangeListener: Slider.OnChangeListener? = null

    init {
        layoutResource = R.layout.preference_slider
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.SliderPreference,
            defStyleAttr,
            defStyleRes
        )
        stepSize = typedArray.getFloat(R.styleable.SliderPreference_android_stepSize, 0.01f)
        value = typedArray.getFloat(R.styleable.SliderPreference_android_value, value)
        valueFrom = typedArray.getFloat(R.styleable.SliderPreference_android_valueFrom, 0f)
        valueTo = typedArray.getFloat(R.styleable.SliderPreference_android_valueTo, 1f)
        typedArray.recycle()
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        var value = defaultValue as? Float
        if (value == null) value = 0f
        setValue(getPersistedFloat(value))
    }

    fun getValue(): Float {
        return value
    }

    fun setValue(value: Float) {
        setValueInternal(value, true)
    }

    private fun setValueInternal(value: Float, notify: Boolean) {
        if (value != this.value) {
            this.value = max(valueFrom, min(value, valueTo))
            persistFloat(this.value)
            if (notify) {
                notifyChanged()
            }
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val slider = holder.findViewById(R.id.slider) as? Slider
        if (slider != null) {
            slider.valueFrom = valueFrom
            slider.valueTo = valueTo
            slider.stepSize = stepSize
            slider.value = value
            slider.setLabelFormatter(sliderLabelFormatter)
            slider.tag = key

            slider.removeOnChangeListener(this)
            slider.removeOnSliderTouchListener(this)
            slider.addOnChangeListener(this)
            slider.addOnSliderTouchListener(this)
        }

        val sliderValue = holder.findViewById(R.id.slider_value) as? TextView
        if (sliderValue != null) {
            this.sliderValue = sliderValue
            updateLabelValue(value)
        }
    }

    override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
        if (fromUser) {
            slider.performHapticFeedback(
                HapticFeedbackConstants.CLOCK_TICK,
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
            )
        }
        updateLabelValue(value)

        @Suppress("RestrictedApi")
        onChangeListener?.onValueChange(slider, value, fromUser)
    }

    override fun onStartTrackingTouch(slider: Slider) {
    }

    override fun onStopTrackingTouch(slider: Slider) {
        setValueInternal(slider.value, true)
    }

    private fun updateLabelValue(value: Float) {
        val sliderValue = sliderValue
        if (sliderValue != null) {
            sliderValue.text = sliderLabelFormatter?.getFormattedValue(value) ?: value.toString()
        }
    }


}