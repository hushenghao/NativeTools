package com.dede.nativetools.other

import androidx.appcompat.app.AppCompatDelegate
import com.dede.nativetools.util.get
import com.dede.nativetools.util.globalPreferences

object OtherPreferences {

    const val KEY_NIGHT_MODE_TOGGLE = "night_mode_toggle"
    const val KEY_IGNORE_BATTERY_OPTIMIZE = "ignore_battery_optimize"
    const val KEY_FULL_NET_USAGE = "full_net_usage"

    const val KEY_ABOUT = "about"
    const val KEY_DONATE = "donate"
    const val KEY_RATE = "rate"
    const val KEY_BETA = "beta"
    const val KEY_SHARE = "share"
    const val KEY_FEEDBACK = "feedback"
    const val KEY_OPEN_SOURCE = "open_source"
    const val KEY_GITHUB = "github"

    private const val DEFAULT_NIGHT_MODE = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

    val nightMode: Int
        get() = globalPreferences.get(KEY_NIGHT_MODE_TOGGLE, DEFAULT_NIGHT_MODE.toString())
            .toIntOrNull() ?: DEFAULT_NIGHT_MODE

}