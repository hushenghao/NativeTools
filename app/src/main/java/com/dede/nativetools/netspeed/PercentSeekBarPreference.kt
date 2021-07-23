package com.dede.nativetools.netspeed

import android.content.Context
import android.util.AttributeSet
import androidx.preference.R
import androidx.preference.SeekBarPreference

class PercentSeekBarPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.seekBarPreferenceStyle,
    defStyleRes: Int = 0
) : SeekBarPreference(context, attrs, defStyleAttr, defStyleRes) {

    init {
        // override default layout
        layoutResource = com.dede.nativetools.R.layout.override_preference_widget_seekbar
    }

}