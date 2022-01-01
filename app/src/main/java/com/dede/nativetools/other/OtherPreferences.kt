package com.dede.nativetools.other

import com.dede.nativetools.util.get
import com.dede.nativetools.util.globalPreferences

object OtherPreferences {

    const val KEY_NIGHT_MODE_TOGGLE = "v28_night_mode_toggle"
    const val KEY_IGNORE_BATTERY_OPTIMIZE = "ignore_battery_optimize"
    const val KEY_ABOUT = "about"

    val isNightMode: Boolean
        get() = globalPreferences.get(KEY_NIGHT_MODE_TOGGLE, false)
}