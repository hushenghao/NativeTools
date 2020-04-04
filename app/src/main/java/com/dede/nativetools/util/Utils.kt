package com.dede.nativetools.util

import android.content.Context


fun String?.safeInt(default: Int): Int {
    if (this == null) return default
    return try {
        this.toInt()
    } catch (e: NumberFormatException) {
        default
    }
}

fun Context.dip(dp: Float): Int {
    return (resources.displayMetrics.density * dp + .5f).toInt()
}
