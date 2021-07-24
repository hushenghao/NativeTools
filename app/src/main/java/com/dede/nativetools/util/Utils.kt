package com.dede.nativetools.util

import android.text.Spanned
import android.text.TextUtils
import androidx.core.text.HtmlCompat


fun String?.safeInt(default: Int): Int {
    if (this == null) return default
    return try {
        this.toInt()
    } catch (e: NumberFormatException) {
        default
    }
}

fun String?.fromHtml(): Spanned? {
    return HtmlCompat.fromHtml(this ?: return null, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

inline val String?.isEmpty: Boolean get() = TextUtils.isEmpty(this)

inline val String?.isNotEmpty: Boolean get() = !TextUtils.isEmpty(this)

fun Pair<String, String>.splicing(): String = this.first + this.second

private val regexTrimZero = Regex("0+?$")
private val regexTrimDot = Regex("[.]$")

fun String.trimZeroAndDot(): String {
    var s = this
    if (s.indexOf(".") > 0) {
        // 去掉多余的0
        s = s.replace(regexTrimZero, "")
        // 如最后一位是.则去掉
        s = s.replace(regexTrimDot, "")
    }
    return s
}
