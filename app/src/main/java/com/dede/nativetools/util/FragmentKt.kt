@file:JvmName("FragmentKt")

package com.dede.nativetools.util

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

fun Fragment.toast(text: String) {
    requireContext().toast(text)
}

fun Fragment.toast(@StringRes resId: Int) {
    requireContext().toast(resId)
}
