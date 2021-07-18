package com.dede.nativetools.util

import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Process
import android.text.Spanned
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.core.text.HtmlCompat
import com.dede.nativetools.NativeToolsApp
import java.io.File
import kotlin.math.roundToInt


fun String?.safeInt(default: Int): Int {
    if (this == null) return default
    return try {
        this.toInt()
    } catch (e: NumberFormatException) {
        default
    }
}

fun String?.fromHtml(): Spanned? {
    return HtmlCompat.fromHtml(this ?: return null, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

inline val String?.isEmpty: Boolean get() = TextUtils.isEmpty(this)

inline val String?.isNotEmpty: Boolean get() = !TextUtils.isEmpty(this)

private inline fun displayMetrics(): DisplayMetrics {
    return NativeToolsApp.getInstance().resources.displayMetrics
}

val Number.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        displayMetrics()
    ).roundToInt()

val Number.dpf: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        displayMetrics()
    )

fun Pair<String, String>.splicing(): String = this.first + this.second

private val regexTrimZero = Regex("0+?$")
private val regexTrimDot = Regex("[.]$")

fun String.trimZeroAndDot(): String {
    var s = this
    if (s.indexOf(".") > 0) {
        // 去掉多余的0
        s = s.replace(regexTrimZero, "")
        // 如最后一位是.则去掉
        s = s.replace(regexTrimDot, "")
    }
    return s
}

fun SharedPreferences.getStringNotNull(key: String, default: String): String {
    return this.getString(key, default) ?: default
}

fun SharedPreferences.putBoolean(key: String, value: Boolean) {
    this.edit().putBoolean(key, value).apply()
}

fun Context.safelyStartActivity(intent: Intent) {
    try {
        this.startActivity(intent)
    } catch (e: Throwable) {
    }
}

fun Context.checkAppOps(): Boolean {
    val appOpsManager =
        this.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
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
        val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
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