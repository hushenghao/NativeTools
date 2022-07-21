package com.dede.nativetools.util

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.TrafficStats
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatDelegate
import com.dede.nativetools.BuildConfig
import com.dede.nativetools.NativeToolsApp
import com.dede.nativetools.R
import com.dede.nativetools.netspeed.NetSpeedPreferences
import com.dede.nativetools.netspeed.service.NetSpeedNotificationHelper
import com.dede.nativetools.netspeed.stats.NetStats
import com.dede.nativetools.netusage.utils.NetUsageUtils
import com.dede.nativetools.other.OtherPreferences
import com.google.android.material.internal.ManufacturerUtils
import com.google.firebase.analytics.FirebaseAnalytics
import java.text.SimpleDateFormat
import java.util.*

fun isNightMode(): Boolean {
    when (OtherPreferences.nightMode) {
        AppCompatDelegate.MODE_NIGHT_YES -> {
            return true
        }
        AppCompatDelegate.MODE_NIGHT_UNSPECIFIED,
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY, -> {
            val configuration = UI.resources.configuration
            return configuration.isNightMode
        }
    }
    return false
}

fun setNightMode(mode: Int) {
    AppCompatDelegate.setDefaultNightMode(mode)
}

val Context.isIgnoringBatteryOptimizations
    get(): Boolean {
        val powerManager = this.requireSystemService<PowerManager>()
        return powerManager.isIgnoringBatteryOptimizations(this.packageName)
    }

fun Context.getVersionSummary(): String {
    val versionName =
        if (BuildConfig.DEBUG || BuildConfig.BUILD_TYPE == "beta") {
            val timestamp = BuildConfig.BUILD_TIMESTAMP
            val time = SimpleDateFormat("yyMMdd.HHmm", Locale.getDefault()).format(Date(timestamp))
            BuildConfig.VERSION_NAME + "-" + time
        } else {
            BuildConfig.VERSION_NAME
        }
    return getString(R.string.summary_about_version, versionName, BuildConfig.VERSION_CODE)
}

object Logic {

    fun shareApp(context: Context) {
        val appName = context.getString(R.string.app_name)
        val url =
            if (isSimplifiedChinese(context)) context.getString(R.string.url_cool_apk)
            else context.getString(R.string.url_play_store)
        context.share(context.getString(R.string.share_text, appName, url))
        event(FirebaseAnalytics.Event.SHARE)
    }

    fun isXiaomi(): Boolean {
        return Build.MANUFACTURER.lowercase(Locale.ENGLISH) == "xiaomi"
    }

    fun isMeizu(): Boolean {
        return ManufacturerUtils.isMeizuDevice()
    }

    private fun isSimplifiedChinese(context: Context): Boolean {
        val local = getLocale(context)
        return local.language == Locale.SIMPLIFIED_CHINESE.language &&
            local.country == Locale.SIMPLIFIED_CHINESE.country
    }

    private fun getLocale(context: Context): Locale {
        val configuration = context.resources.configuration
        var local =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.locales[0]
            } else {
                @Suppress("DEPRECATION") configuration.locale
            }
        if (local == null) {
            local = Locale.getDefault()
        }
        return local
    }

    fun checkAppOps(context: Context): Boolean {
        val appOpsManager = context.requireSystemService<AppOpsManager>()
        val result =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                appOpsManager.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
            } else {
                @Suppress("DEPRECATION")
                appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
            }
        return result == AppOpsManager.MODE_ALLOWED
    }

    fun requestOpsPermission(
        context: Context,
        activityResultLauncher: ActivityResultLauncherCompat<Intent, ActivityResult>,
        granted: () -> Unit,
        denied: (() -> Unit)? = null,
    ) {
        if (checkAppOps(context)) {
            granted.invoke()
            return
        }
        context.alert(R.string.usage_states_title, R.string.usage_stats_msg) {
            positiveButton(R.string.access) {
                val intent =
                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS, "package:${context.packageName}")
                // https://developer.android.google.cn/training/package-visibility/automatic
                // adb shell dumpsys package queries
                // 由于settings自动可见，不需要声明<queries>
                if (!intent.queryImplicitActivity(context)) {
                    intent.data = null
                }
                activityResultLauncher.launch(intent) {
                    if (checkAppOps(context)) {
                        granted.invoke()
                    } else {
                        denied?.invoke()
                    }
                }
            }
            negativeButton(android.R.string.cancel) { denied?.invoke() }
        }
    }

    fun collectionDiagnosis(): String {
        val app = NativeToolsApp.getInstance()
        val sb =
            StringBuilder()
                .appendPart("Manufacturer", Build.MANUFACTURER)
                .appendPart("Brand", Build.BRAND)
                .appendPart("Model", Build.MODEL)
                .appendPart("Product", Build.PRODUCT)
                .appendPart("Display", Build.DISPLAY)
                .appendPart("Android version", Build.VERSION.RELEASE)
                .appendPart("Sdk", Build.VERSION.SDK_INT)
                .appendPart("Locale", getLocale(app))
                .appendPart("Screen width", UI.displayMetrics().widthPixels)
                .appendPart("Screen height", UI.displayMetrics().heightPixels)
                .appendPart("Density dpi", UI.displayMetrics().densityDpi)
                .appendPart(
                    "SmallestScreenWidthDp", UI.resources.configuration.smallestScreenWidthDp)

        sb.appendLine()
            .appendPart("Package name", app.packageName)
            .appendPart("Build type", BuildConfig.BUILD_TYPE)
            .appendPart("App version", app.getVersionSummary())
            .appendPart("Target sdk", app.applicationInfo.targetSdkVersion)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            sb.appendPart("Compile sdk", app.applicationInfo.compileSdkVersion)
        }
        sb.appendPart("Process", app.processName())
            .appendPart("Thread", Thread.currentThread().name)
            .appendPart("Status", NetSpeedPreferences.status)
            .appendPart("Free reflection", app.unseal)
            .appendPart("Notification", NetSpeedNotificationHelper.areNotificationEnabled(app))
            .appendPart("Stats", NetStats.getInstance().name)
            .appendPart("TotalTxBytes", TrafficStats.getTotalTxBytes())
            .appendPart("TotalRxBytes", TrafficStats.getTotalRxBytes())
            .appendPart("AppOps", checkAppOps(app))
            .appendPart("Today usage", NetUsageUtils.networkUsageDiagnosis(app)) // work thread

        return sb.toString()
    }

    private fun StringBuilder.appendPart(first: String, second: Any?): StringBuilder {
        return this.append(first).append(": ").appendLine(second)
    }
}
