package com.dede.nativetools.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.Keep
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.dede.nativetools.R
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Slider
 */
class SliderPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs),
    Slider.OnChangeListener, Slider.OnSliderTouchListener, View.OnKeyListener {

    private var stepSize: Float
    private var value: Float = 0f
    private var valueFrom: Float
    private var valueTo: Float

    private var slider: Slider? = null
    private var sliderValueText: TextView? = null

    var sliderLabelFormatter: LabelFormatter? = null
        set(value) {
            field = value
            notifyChanged()
        }

    var onChangeListener: Slider.OnChangeListener? = null

    init {
        layoutResource = R.layout.preference_slider
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SliderPreference)
        valueFrom = typedArray.getFloat(R.styleable.SliderPreference_android_valueFrom, 0f)
        valueTo = typedArray.getFloat(R.styleable.SliderPreference_android_valueTo, 1f)
        stepSize = typedArray.getFloat(R.styleable.SliderPreference_android_stepSize, 0.01f)
        typedArray.recycle()
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any? {
        return a?.getFloat(index, 0f)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        var value = defaultValue as? Float
        if (value == null) value = 0f
        value = formatValue(value)
        setValue(getPersistedFloat(value))
    }

    fun getValue(): Float {
        return value
    }

    fun setValue(value: Float) {
        setValueInternal(formatValue(value), true)
    }

    fun setStepSize(stepSize: Float) {
        var size = stepSize
        if (size <= 0.0f) {
            size = 0.01f
        }
        if (size != this.stepSize) {
            this.stepSize = size
            notifyChanged()
        }
    }

    fun setValueFrom(valueFrom: Float) {
        var from = valueFrom
        if (from < valueTo) {
            from = valueTo
        }
        if (from != this.valueFrom) {
            this.valueFrom = from
            notifyChanged()
        }
    }

    fun getValueFrom(): Float {
        return this.valueFrom
    }

    fun setValueTo(valueTo: Float) {
        var to = valueTo
        if (to > valueFrom) {
            to = valueFrom
        }
        if (to != this.valueTo) {
            this.valueTo = to
            notifyChanged()
        }
    }

    fun getValueTo(): Float {
        return this.valueTo
    }

    private fun setValueInternal(sliderValue: Float, notify: Boolean) {
        val value = max(valueFrom, min(sliderValue, valueTo))
        if (value != this.value) {
            this.value = value
            updateLabelValue(this.value)
            persistFloat(this.value)
            if (notify) {
                notifyChanged()
            }
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView.setOnKeyListener(this)
        val slider = holder.findViewById(R.id.slider) as Slider
        val sliderValueText = holder.findViewById(R.id.slider_value) as? TextView
        this.slider = slider
        this.sliderValueText = sliderValueText
        slider.clearOnChangeListeners()
        slider.clearOnSliderTouchListeners()
        slider.addOnChangeListener(this)
        slider.addOnSliderTouchListener(this)

        slider.setLabelFormatter(sliderLabelFormatter)
        slider.tag = key

        slider.valueFrom = valueFrom
        slider.valueTo = valueTo
        slider.stepSize = stepSize
        slider.value = value
        updateLabelValue(value)
        slider.isEnabled = isEnabled
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) {
            return false
        }

        // We don't want to propagate the click keys down to the Slider view since it will
        // create the ripple effect for the thumb.
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            return false
        }
        return slider?.onKeyDown(keyCode, event) ?: false
    }

    @SuppressLint("RestrictedApi")
    override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
        if (fromUser) {
            slider.performHapticFeedback(
                HapticFeedbackConstants.CLOCK_TICK,
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
            )
        }
        val format = formatValue(value)
        updateLabelValue(format)
        onChangeListener?.onValueChange(slider, format, fromUser)
    }

    private fun formatValue(value: Float): Float {
        val int = (value / stepSize).roundToInt()
        return int * stepSize
    }

    @SuppressLint("RestrictedApi")
    override fun onStartTrackingTouch(slider: Slider) {
    }

    @SuppressLint("RestrictedApi")
    override fun onStopTrackingTouch(slider: Slider) {
        val value = formatValue(slider.value)
        if (this.value != value) {
            if (callChangeListener(value)) {
                setValueInternal(value, false)
            } else {
                slider.value = value
                updateLabelValue(value)
            }
        }
    }

    private fun updateLabelValue(value: Float) {
        val textView = sliderValueText
        if (textView != null) {
            textView.text = sliderLabelFormatter?.getFormattedValue(value) ?: value.toString()
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            return superState
        }
        val savedState = SavedState(superState)
        savedState.stepSize = stepSize
        savedState.value = value
        savedState.valueFrom = valueFrom
        savedState.valueTo = valueTo
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        stepSize = state.stepSize
        value = state.value
        valueFrom = state.valueFrom
        valueTo = state.valueTo
        notifyChanged()
    }

    private class SavedState : BaseSavedState {

        companion object {
            @Keep
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState?> =
                object : Parcelable.Creator<SavedState?> {
                    override fun createFromParcel(`in`: Parcel): SavedState {
                        return SavedState(`in`)
                    }

                    override fun newArray(size: Int): Array<SavedState?> {
                        return arrayOfNulls(size)
                    }
                }
        }

        var stepSize: Float = 0.01f
        var value: Float = 0f
        var valueFrom: Float = 0f
        var valueTo: Float = 1f

        constructor(source: Parcel) : super(source) {
            stepSize = source.readFloat()
            value = source.readFloat()
            valueFrom = source.readFloat()
            valueTo = source.readFloat()
        }

        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeFloat(stepSize)
            dest.writeFloat(value)
            dest.writeFloat(valueFrom)
            dest.writeFloat(valueTo)
        }
    }

}