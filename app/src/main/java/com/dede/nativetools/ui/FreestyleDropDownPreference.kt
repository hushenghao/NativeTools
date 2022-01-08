package com.dede.nativetools.ui

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.preference.DropDownPreference
import androidx.preference.PreferenceViewHolder

abstract class FreestyleDropDownPreference @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.dropdownPreferenceStyle,
    defStyleRes: Int = 0
) : DropDownPreference(context, attrs, defStyleAttr, defStyleRes) {

    abstract fun freestyle(position: Int, textView: TextView)

    override fun createAdapter(): ArrayAdapter<*> {
        return object : ArrayAdapter<Any>(context, android.R.layout.simple_spinner_dropdown_item) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val itemView = super.getView(position, convertView, parent)
                val textView = itemView.findViewById<TextView>(android.R.id.text1)
                setItemStyle(position, textView)
                return itemView
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val itemView = super.getDropDownView(position, convertView, parent)
                val textView = itemView.findViewById<TextView>(android.R.id.text1)
                setItemStyle(position, textView)
                return itemView
            }
        }
    }

    private fun setItemStyle(position: Int, textView: TextView) {
        if (position < 0 || position >= entryValues.size) {
            return
        }
        freestyle(position, textView)
    }

    override fun onBindViewHolder(view: PreferenceViewHolder) {
        super.onBindViewHolder(view)
        val summaryTextView = view.findViewById(android.R.id.summary) as? TextView
        val spinner = view.findViewById(androidx.preference.R.id.spinner) as? Spinner
        if (summaryTextView != null && spinner != null) {
            setItemStyle(spinner.selectedItemPosition, summaryTextView)
        }
    }
}

/**
 * 自定义字体样式
 */
class TypefaceDropDownPreference @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.dropdownPreferenceStyle,
    defStyleRes: Int = 0
) : FreestyleDropDownPreference(context, attrs, defStyleAttr, defStyleRes) {

    override fun freestyle(position: Int, textView: TextView) {
        val style = entryValues[position].toString().toIntOrNull() ?: Typeface.NORMAL
        textView.typeface = Typeface.create(Typeface.DEFAULT, style)
    }
}
