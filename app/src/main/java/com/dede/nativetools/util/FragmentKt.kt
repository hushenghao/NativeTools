@file:JvmName("FragmentKt")

package com.dede.nativetools.util

import android.content.pm.PackageManager
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

fun Fragment.toast(text: String) {
    requireContext().toast(text)
}

fun Fragment.toast(@StringRes resId: Int) {
    requireContext().toast(resId)
}

fun Fragment.checkPermissions(vararg permissions: String): Boolean {
    val context = requireContext()
    for (permission in permissions) {
        if (ContextCompat.checkSelfPermission(context, permission) !=
            PackageManager.PERMISSION_GRANTED) {
            return false
        }
    }
    return true
}
