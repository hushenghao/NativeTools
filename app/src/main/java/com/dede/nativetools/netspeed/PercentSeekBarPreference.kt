package com.dede.nativetools.netspeed

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
        const val ICON_SIZE = 48f
    }

    private var seekBarValueTextView: TextView? = null
    private var copySeekBarChangeListener: SeekBar.OnSeekBarChangeListener? = null

    override fun onBindViewHolder(view: PreferenceViewHolder) {
        super.onBindViewHolder(view)

        val imageView = view.findViewById(android.R.id.icon) as ImageView
        val size = ICON_SIZE.dp
        imageView.layoutParams = imageView.layoutParams?.apply {
            height = size
            width = size
        }
        val seekBar = view.findViewById(R.id.seekbar) as SeekBar
        seekBar.layoutParams = seekBar.layoutParams?.apply {
            height = 30.dp
        }
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
        seekBarValueTextView?.text = "$value %"
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        copySeekBarChangeListener?.onProgressChanged(seekBar, progress, fromUser)
        seekBarValueTextView?.text = "${progress + min} %"
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        copySeekBarChangeListener?.onStartTrackingTouch(seekBar)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        copySeekBarChangeListener?.onStopTrackingTouch(seekBar)
    }


}