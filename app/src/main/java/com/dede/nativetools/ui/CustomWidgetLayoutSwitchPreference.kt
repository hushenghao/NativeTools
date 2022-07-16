package com.dede.nativetools.ui

import android.content.Context
import android.util.AttributeSet
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreferenceCompat

/**
 * 自定义widgetLayout的SwitchPreference
 *
 * @author hsh
 * @since 2021/10/9 1:40 下午
 */
open class CustomWidgetLayoutSwitchPreference(context: Context, attrs: AttributeSet?) :
    SwitchPreferenceCompat(context, attrs) {

    var bindCustomWidget: ((holder: PreferenceViewHolder) -> Unit)? = null
        set(value) {
            field = value
            notifyChanged()
        }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        bindCustomWidget?.invoke(holder)
    }
}
