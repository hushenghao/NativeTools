package com.dede.nativetools.ui

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreferenceCompat
import com.dede.nativetools.util.browse

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

class HideNotificationSwitchPreference(context: Context, attrs: AttributeSet?) :
    CustomWidgetLayoutSwitchPreference(context, attrs) {

    init {
        widgetLayoutResource =
            com.dede.nativetools.R.layout.override_preference_widget_switch_compat
        bindCustomWidget = {
            it.findViewById(com.dede.nativetools.R.id.iv_preference_help)?.setOnClickListener {
                context.browse("https://developer.android.google.cn/about/versions/12/behavior-changes-12#custom-notifications")
            }
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val titleView = holder.findViewById(android.R.id.title) as? TextView
        if (titleView != null) {
            titleView.paintFlags = titleView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }
    }
}