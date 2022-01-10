@file:JvmName("LogicKt")

package com.dede.nativetools.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.dede.nativetools.other.OtherPreferences
import java.util.*

fun isSimplifiedChinese(context: Context): Boolean {
    val configuration = context.resources.configuration
    var local = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        configuration.locales[0]
    } else {
        configuration.locale
    }
    if (local == null) {
        local = Locale.getDefault()
    }
    return local == Locale.SIMPLIFIED_CHINESE
}

fun isNightMode(): Boolean {
    when (OtherPreferences.nightMode) {
        AppCompatDelegate.MODE_NIGHT_YES -> {
            return true
        }
        AppCompatDelegate.MODE_NIGHT_UNSPECIFIED,
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY -> {
            val configuration = Resources.getSystem().configuration
            return configuration.isNightMode
        }
    }
    return false
}

fun setNightMode(mode: Int) {
    AppCompatDelegate.setDefaultNightMode(mode)
}

val Configuration.isNightMode: Boolean
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) this.isNightModeActive else
            this.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
