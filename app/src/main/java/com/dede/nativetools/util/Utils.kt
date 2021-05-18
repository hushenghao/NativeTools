package com.dede.nativetools.util

import android.util.DisplayMetrics
import android.util.TypedValue
import com.dede.nativetools.NativeToolsApp
import kotlin.math.roundToInt


fun String?.safeInt(default: Int): Int {
    if (this == null) return default
    return try {
        this.toInt()
    } catch (e: NumberFormatException) {
        default
    }
}

private inline fun displayMetrics(): DisplayMetrics {
    return NativeToolsApp.getInstance().resources.displayMetrics
}

val Number.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        displayMetrics()
    ).roundToInt()

val Number.dpf: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        displayMetrics()
    )
