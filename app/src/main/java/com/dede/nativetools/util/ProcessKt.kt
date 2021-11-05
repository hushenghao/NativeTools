@file:JvmName("ProcessKt")
package com.dede.nativetools.util

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import java.io.File


private var currentProcessName: String = ""

fun Context.getProcessName(): String {
    if (currentProcessName.isNotEmpty) {
        return currentProcessName
    }

    val pid = Process.myPid()
    val processName = getProcessName(pid)
    if (processName != null && processName.isNotEmpty) {
        currentProcessName = processName
    } else {
        val activityManager = this.requireSystemService<ActivityManager>()
        val list = activityManager.runningAppProcesses
        if (list == null || list.isEmpty()) {
            return currentProcessName
        }
        for (info in list) {
            if (info.pid == pid) {
                currentProcessName = info.processName
                break
            }
        }
    }
    return currentProcessName
}

private fun getProcessName(pid: Int): String? {
    val file = File("/proc/$pid/cmdline")
    if (!file.exists()) return null

    file.bufferedReader().use { reader ->
        var str = reader.readLine()
        if (str.isNotEmpty) {
            str = str.trim { it <= ' ' }
        }
        return str
    }
}

fun Context.isMainProcess(): Boolean {
    val mainProcessName = this.packageName
    val currentProcessName = getProcessName()
    return mainProcessName.isNotEmpty &&
            currentProcessName.isNotEmpty &&
            mainProcessName == currentProcessName
}
