package com.dede.nativetools.ui

import android.content.Context
import android.util.AttributeSet
import androidx.preference.PreferenceViewHolder
import androidx.preference.R
import androidx.preference.SwitchPreferenceCompat

/**
 * 自定义widgetLayout的SwitchPreference
 *
 * @author hsh
 * @since 2021/10/9 1:40 下午
 */
class CustomWidgetLayoutSwitchPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.switchPreferenceCompatStyle,
    defStyleRes: Int = 0
) : SwitchPreferenceCompat(context, attrs, defStyleAttr, defStyleRes) {

    var bindCustomWidget: ((holder: PreferenceViewHolder) -> Unit)? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        bindCustomWidget?.invoke(holder ?: return)
    }
}