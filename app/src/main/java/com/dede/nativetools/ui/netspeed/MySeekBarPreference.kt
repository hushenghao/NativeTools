package com.dede.nativetools.ui.netspeed

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.preference.PreferenceViewHolder
import androidx.preference.R
import androidx.preference.SeekBarPreference
import com.dede.nativetools.util.dip

class MySeekBarPreference : SeekBarPreference, SeekBar.OnSeekBarChangeListener {
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context) : super(context)

    private var seekBarValueTextView: TextView? = null
    private var copySeekBarChangeListener: SeekBar.OnSeekBarChangeListener? = null

    override fun onBindViewHolder(view: PreferenceViewHolder) {
        super.onBindViewHolder(view)
        val context = view.itemView.context

        val imageView = view.findViewById(android.R.id.icon) as ImageView
        val size = context.dip(42f)
        imageView.layoutParams = imageView.layoutParams?.apply {
            height = size
            width = size
        }
        val seekBar = view.findViewById(R.id.seekbar) as SeekBar
        seekBar.layoutParams = seekBar.layoutParams?.apply {
            height = context.dip(30f)
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
        seekBarValueTextView?.textSize = 12f
        if (copySeekBarChangeListener != null) {
            seekBarValueTextView?.text = "$value %"
        }
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