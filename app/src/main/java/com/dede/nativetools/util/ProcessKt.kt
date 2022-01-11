@file:JvmName("ProcessKt")

package com.dede.nativetools.util

import android.app.ActivityManager
import android.content.ComponentCallbacks2
import android.content.Context
import android.os.Build
import android.os.Process
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

private fun Context.runningProcesses(): List<ActivityManager.RunningAppProcessInfo> {
    val activityManager = requireSystemService<ActivityManager>()
    return activityManager.runningAppProcesses ?: emptyList()
}

fun Context.currentProcess(): ActivityManager.RunningAppProcessInfo? {
    val list = this.runningProcesses()
    if (list.isEmpty()) {
        return null
    }
    val pid = Process.myPid()
    for (info in list) {
        if (info.pid == pid) {
            return info
        }
    }
    return null
}

fun Context.mainProcess(): ActivityManager.RunningAppProcessInfo? {
    val list = this.runningProcesses()
    if (list.isEmpty()) {
        return null
    }
    val packageName = this.packageName
    for (info in list) {
        if (info.processName == packageName) {
            return info
        }
    }
    return null
}

fun Context.isMainProcess(): Boolean {
    return currentProcess().isMainProcess(this)
}

@OptIn(ExperimentalContracts::class)
fun ActivityManager.RunningAppProcessInfo?.isMainProcess(context: Context): Boolean {
    contract {
        returns(true) implies (this@isMainProcess != null)
    }
    val packageName = context.packageName
    if (this == null) return false
    return packageName.isNotEmpty() && this.processName == packageName
}

@OptIn(ExperimentalContracts::class)
fun ActivityManager.RunningAppProcessInfo?.isForeground(): Boolean {
    contract {
        returns(true) implies (this@isForeground != null)
    }
    if (this == null) return false
    val uiHidden = this.lastTrimLevel >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN
    val cached = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        this.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED
    } else {
        @Suppress("DEPRECATION")
        this.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND
    }
    return !uiHidden && !cached
}
