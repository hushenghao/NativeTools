package com.dede.nativetools.other

import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dede.nativetools.util.get
import com.dede.nativetools.util.globalDataStore

object OtherPreferences {

    const val KEY_NIGHT_MODE_TOGGLE = "night_mode_toggle"
    const val KEY_IGNORE_BATTERY_OPTIMIZE = "ignore_battery_optimize"

    const val KEY_ABOUT = "about"
    const val KEY_RATE = "rate"
    const val KEY_SHARE = "share"

    private const val DEFAULT_NIGHT_MODE = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

    val nightMode: Int
        get() = globalDataStore.get(
            stringPreferencesKey(KEY_NIGHT_MODE_TOGGLE),
            DEFAULT_NIGHT_MODE.toString()
        ).toIntOrNull() ?: DEFAULT_NIGHT_MODE

}