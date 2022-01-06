package com.dede.nativetools.other

import androidx.appcompat.app.AppCompatDelegate
import com.dede.nativetools.util.get
import com.dede.nativetools.util.globalPreferences
import com.dede.nativetools.util.set

object OtherPreferences {

    const val KEY_NIGHT_MODE_TOGGLE = "night_mode_toggle"
    const val KEY_IGNORE_BATTERY_OPTIMIZE = "ignore_battery_optimize"
    const val KEY_ABOUT = "about"

    private const val KEY_TOGGLE_LAUNCHER_ICON_DONT_ASK = "toggle_launcher_icon_dont_ask"

    private const val DEFAULT_NIGHT_MODE = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

    val nightMode: Int
        get() = globalPreferences.get(KEY_NIGHT_MODE_TOGGLE, DEFAULT_NIGHT_MODE.toString())
            .toIntOrNull() ?: DEFAULT_NIGHT_MODE

    var dontAskToggleLauncherIcon: Boolean
        get() = globalPreferences.get(KEY_TOGGLE_LAUNCHER_ICON_DONT_ASK, false)
        set(value) = globalPreferences.set(KEY_TOGGLE_LAUNCHER_ICON_DONT_ASK, value)
}