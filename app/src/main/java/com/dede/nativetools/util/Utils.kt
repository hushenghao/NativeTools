@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package com.dede.nativetools.util

import android.os.Build
import android.text.Spanned
import android.text.TextUtils
import android.util.Base64
import androidx.core.text.HtmlCompat
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


fun String?.fromHtml(): Spanned? {
    return HtmlCompat.fromHtml(this ?: return null, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

@OptIn(ExperimentalContracts::class)
@kotlin.internal.InlineOnly
inline fun String?.isEmpty(): Boolean {
    contract {
        returns(false) implies (this@isEmpty != null)
    }
    return TextUtils.isEmpty(this)
}

@OptIn(ExperimentalContracts::class)
@kotlin.internal.InlineOnly
inline fun String?.isNotEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNotEmpty != null)
    }
    return !TextUtils.isEmpty(this)
}

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

fun String.decodeBase64(): String? {
    return runCatching { String(Base64.decode(this, Base64.DEFAULT)) }.getOrNull()
}

fun getProp(key: String): String? {
    return Build::class.java.runCatching {
        declaredMethod("getString", String::class.java)
            .invoke(null, key) as? String
    }.getOrNull()
}