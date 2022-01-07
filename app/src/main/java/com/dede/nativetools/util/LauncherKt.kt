@file:JvmName("LauncherKt")

package com.dede.nativetools.util

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.pm.PackageManager

private val packageName = globalContext.packageName
private val componentDay = ComponentName(globalContext, "$packageName.main.Day")
private val componentNight = ComponentName(globalContext, "$packageName.main.Night")

fun tryApplyLauncherIcon() {
    val context = globalContext
    val processInfo = context.currentProcess()
    if (processInfo.isMainProcess(context)) {
        // 主进程
        if (!processInfo.isRunning()) {
            applyLauncherIcon()
        }
        return
    }

    // 子进程
    val mainProcess = context.mainProcess()
    if (mainProcess != null && mainProcess.isRunning()) {
        // 主进程还在运行中
        return
    }

    runCatching {
        // reload from disk
        @SuppressLint("PrivateApi")
        val clazz = Class.forName("android.app.SharedPreferencesImpl")
        val method = clazz.declaredMethod("startReloadIfChangedUnexpectedly")
        method.invoke(globalPreferences)
    }.onSuccess {
        applyLauncherIcon()
    }.onFailure(Throwable::printStackTrace)
}

fun applyLauncherIcon() {
    val pm = globalContext.packageManager
    if (isNightMode()) {
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