package com.dede.nativetools.netspeed.typeface

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import com.dede.nativetools.BuildConfig
import com.dede.nativetools.R
import com.dede.nativetools.ui.FreestyleDropDownPreference
import com.dede.nativetools.util.dp
import com.dede.nativetools.util.requireDrawable
import com.dede.nativetools.util.setCompoundDrawablesRelative
import com.dede.nativetools.util.toast

/**
 * 自定义字体
 */
class FontDropDownPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.dropdownPreferenceStyle,
    defStyleRes: Int = 0
) : FreestyleDropDownPreference(context, attrs, defStyleAttr, defStyleRes), Observer<WorkInfo> {

    override fun freestyle(position: Int, textView: TextView) {
        val fontKey = entryValues[position].toString()
        val getter = TypefaceGetter.create(context, fontKey)
        if (!getter.canApply()) {
            textView.compoundDrawablePadding = 6.dp
            textView.setCompoundDrawablesRelative(
                end = context.requireDrawable(R.drawable.ic_outline_file_download)
            )
            return
        }
        textView.setCompoundDrawablesRelative()
        if (fontKey == TypefaceGetter.FONT_DEBUG && getter is DebugTypeface) {
            // for debug
            textView.text = getter.fontName
        }
        textView.typeface = getter.get(Typeface.NORMAL)
    }

    init {
        super.setOnPreferenceChangeListener { _, newValue ->
            val fontKey = newValue.toString()
            val getter = TypefaceGetter.create(context, fontKey)
            val canApply = getter.canApply()
            if (!canApply) {
                // 下载字体
                downloadFont(fontKey)
                notifyChanged()// fix Spinner.OnItemSelectedListener cannot recall
            }
            return@setOnPreferenceChangeListener canApply// 下载过的字体才应用
        }
        if (BuildConfig.DEBUG) {
            // 添加debug字体
            entryValues.toMutableList().let {
                it.add(TypefaceGetter.FONT_DEBUG)
                entryValues = it.toTypedArray()
            }

            entries.toMutableList().let {
                it.add(TypefaceGetter.FONT_DEBUG)
                entries = it.toTypedArray()
            }
        }
    }

    private var downloadLiveData: LiveData<WorkInfo>? = null

    private fun downloadFont(fontKey: String) {
        downloadLiveData?.removeObserver(this)
        downloadLiveData = DownloadFontWork.downloadFont(context, fontKey)
            ?.also { it.observeForever(this) }
    }

    override fun onChanged(workInfo: WorkInfo) {
        when (workInfo.state) {
            WorkInfo.State.SUCCEEDED -> {
                context.toast(com.dede.nativetools.R.string.toast_download_font_succeeded)
                val fontKey = workInfo.outputData.getString(DownloadFontWork.EXTRA_FONT_KEY)
                value = fontKey// 更新下载
            }
            WorkInfo.State.FAILED -> {
                context.toast(com.dede.nativetools.R.string.toast_download_font_failed)
            }
            WorkInfo.State.RUNNING -> {
                context.toast(com.dede.nativetools.R.string.toast_download_font)
            }
            else -> {

            }
        }
    }

    override fun onDetached() {
        downloadLiveData?.removeObserver(this)
        super.onDetached()
    }

    override fun setOnPreferenceChangeListener(onPreferenceChangeListener: OnPreferenceChangeListener?) {
        throw IllegalStateException("setOnPreferenceChangeListener method cannot be override")
    }
}