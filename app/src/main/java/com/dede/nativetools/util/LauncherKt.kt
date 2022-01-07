@file:JvmName("LauncherKt")

package com.dede.nativetools.util

import android.content.ComponentName
import android.content.pm.PackageManager
import com.dede.nativetools.other.OtherPreferences

private val packageName = globalContext.packageName
private val componentDay = ComponentName(globalContext, "$packageName.main.Day")
private val componentNight = ComponentName(globalContext, "$packageName.main.Night")

fun tryApplyLauncherIcon() {
    applyLauncherIcon(isNightMode())
}

fun applyLauncherIcon(nightMode: Boolean) {
    if (!OtherPreferences.autoUpdateLauncherIcon)
        return
    val pm = globalContext.packageManager
    if (nightMode) {
        componentNight.enable(pm)
        componentDay.disable(pm)
    } else {
        componentDay.enable(pm)
        componentNight.disable(pm)
    }
}

fun ComponentName.enable(pm: PackageManager) {
    val enable = PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    if (pm.getComponentEnabledSetting(this) == enable) return
    pm.setComponentEnabledSetting(this, enable, PackageManager.DONT_KILL_APP)
}

fun ComponentName.disable(pm: PackageManager) {
    val disabled = PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    if (pm.getComponentEnabledSetting(this) == disabled) return
    pm.setComponentEnabledSetting(this, disabled, PackageManager.DONT_KILL_APP)
}

fun ComponentName.isEnable(pm: PackageManager): Boolean {
    val setting = pm.getComponentEnabledSetting(this)
    return setting == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
            setting == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
}