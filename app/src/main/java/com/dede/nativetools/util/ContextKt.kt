@file:JvmName("ContextKt")

package com.dede.nativetools.util

import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.dede.nativetools.NativeToolsApp
import com.dede.nativetools.R
import java.io.File
import java.io.InputStream

val globalContext: Context
    get() = NativeToolsApp.getInstance()

fun Context.safelyStartActivity(intent: Intent) {
    safely { this.startActivity(intent) }
}

fun Context.startService(intent: Intent, foreground: Boolean) {
    if (foreground) {
        ContextCompat.startForegroundService(this, intent)
    } else {
        this.startService(intent)
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

fun Context.assets(fileName: String): InputStream {
    return assets.open(fileName)
}

fun Context.toast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun Context.toast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}

fun Context.browse(url: String) {
    val web = Intent(Intent.ACTION_VIEW)
        .setData(url)
        .newTask()
        .toChooser(R.string.chooser_label_browse)
    startActivity(web)
}

fun Context.market(packageName: String) {
    val market = Intent(Intent.ACTION_VIEW)
        .setData("market://details?id=$packageName")
        .newTask()
        .toChooser(R.string.chooser_label_market)
    startActivity(market)
}

fun Context.share(@StringRes textId: Int) {
    val intent = Intent(Intent.ACTION_SEND, Intent.EXTRA_TEXT to getString(textId))
        .setType("text/plain")
        .newTask()
        .toChooser(R.string.action_share)
    startActivity(intent)
}

fun Context.copy(text: String) {
    val clipboardManager = this.getSystemService<ClipboardManager>() ?: return
    clipboardManager.setPrimaryClip(ClipData.newPlainText("text", text))
}

fun Context.readClipboard(): String? {
    val clipboardManager = getSystemService<ClipboardManager>() ?: return null
    val primaryClip = clipboardManager.primaryClip ?: return null
    if (primaryClip.itemCount > 0) {
        return primaryClip.getItemAt(0)?.text?.toString()
    }
    return null
}
