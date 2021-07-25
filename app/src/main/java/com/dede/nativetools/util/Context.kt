package com.dede.nativetools.util

import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import androidx.core.content.getSystemService
import com.dede.nativetools.R
import java.io.File


fun Context.safelyStartActivity(intent: Intent) {
    try {
        this.startActivity(intent)
    } catch (e: Throwable) {
    }
}

fun Context.checkAppOps(): Boolean {
    val appOpsManager = getSystemService<AppOpsManager>() ?: return true
    val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        appOpsManager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            this.packageName
        )
    } else {
        appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            this.packageName
        )
    }
    return result == AppOpsManager.MODE_ALLOWED
}

fun Context.isMainProcess(): Boolean {
    val mainProcessName = this.packageName
    val currentProcessName = getCurrentProcessName()
    return mainProcessName.isNotEmpty &&
            currentProcessName.isNotEmpty &&
            mainProcessName == currentProcessName
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

private var currentProcessName: String = ""

private fun Context.getCurrentProcessName(): String {
    if (currentProcessName.isNotEmpty) {
        return currentProcessName
    }

    val pid = Process.myPid()
    val processName = getProcessName(pid)
    if (processName != null && processName.isNotEmpty) {
        currentProcessName = processName
    } else {
        val activityManager = this.getSystemService<ActivityManager>() ?: return currentProcessName
        val list = activityManager.runningAppProcesses
        if (list != null && list.isNotEmpty()) {
            for (info in list) {
                if (info.pid == pid) {
                    currentProcessName = info.processName
                    break
                }
            }
        }
    }
    return currentProcessName
}

fun Context.browse(url: String) {
    val web = Intent(Intent.ACTION_VIEW)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .setData(Uri.parse(url))
    val chooserIntent = Intent.createChooser(web, getString(R.string.chooser_label_browse))
    startActivity(chooserIntent)
}

fun Context.market(packageName: String) {
    val market = Intent(Intent.ACTION_VIEW)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .setData(Uri.parse("market://details?id=$packageName"))
    val chooserIntent = Intent.createChooser(market, getString(R.string.chooser_label_market))
    startActivity(chooserIntent)
}

fun Context.share(textId: Int) {
    val intent = Intent(Intent.ACTION_SEND)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_TEXT, getString(textId))
    val chooserIntent = Intent.createChooser(intent, getString(R.string.action_share))
    startActivity(chooserIntent)
}
