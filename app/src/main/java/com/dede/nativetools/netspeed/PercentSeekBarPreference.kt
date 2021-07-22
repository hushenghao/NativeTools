package com.dede.nativetools.netspeed

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.preference.PreferenceViewHolder
import androidx.preference.R
import androidx.preference.SeekBarPreference
import com.dede.nativetools.util.dp

class PercentSeekBarPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.seekBarPreferenceStyle,
    defStyleRes: Int = 0
) : SeekBarPreference(context, attrs, defStyleAttr, defStyleRes), SeekBar.OnSeekBarChangeListener {

    companion object {
        // androidx.preference.preference:1.1.0 res/layout/preference.xml
        // override_preference_widget_seekbar.xml
        const val ICON_SIZE = 56f
    }

    private var seekBarValueTextView: TextView? = null
    private var copySeekBarChangeListener: SeekBar.OnSeekBarChangeListener? = null

    init {
        // override default layout
        layoutResource = com.dede.nativetools.R.layout.override_preference_widget_seekbar
    }

    override fun onBindViewHolder(view: PreferenceViewHolder) {
        super.onBindViewHolder(view)

        val seekBar = view.findViewById(R.id.seekbar) as SeekBar
        if (copySeekBarChangeListener == null || copySeekBarChangeListener != this) {
            try {
                val field = SeekBar::class.java.getDeclaredField("mOnSeekBarChangeListener")
                field.isAccessible = true
                copySeekBarChangeListener = field.get(seekBar) as? SeekBar.OnSeekBarChangeListener
                seekBar.setOnSeekBarChangeListener(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        seekBarValueTextView = view.findViewById(R.id.seekbar_value) as TextView
        setValueStr(value)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        copySeekBarChangeListener?.onProgressChanged(seekBar, progress, fromUser)
        setValueStr(progress + min)
    }

    private fun setValueStr(value: Int) {
        seekBarValueTextView?.text =
            context.getString(com.dede.nativetools.R.string.percent_seek_bar_value, value)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        copySeekBarChangeListener?.onStartTrackingTouch(seekBar)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        copySeekBarChangeListener?.onStopTrackingTouch(seekBar)
    }


}