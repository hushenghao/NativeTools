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
import com.dede.nativetools.netspeed.utils.NetworkUsageUtil
import com.dede.nativetools.other.OtherPreferences
import java.util.*

fun isNightMode(): Boolean {
    when (OtherPreferences.nightMode) {
        AppCompatDelegate.MODE_NIGHT_YES -> {
            return true
        }
        AppCompatDelegate.MODE_NIGHT_UNSPECIFIED,
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY,
        -> {
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
    get():Boolean {
        val powerManager = this.requireSystemService<PowerManager>()
        return powerManager.isIgnoringBatteryOptimizations(this.packageName)
    }

fun Context.getVersionSummary() =
    getString(R.string.summary_about_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

object Logic {

    fun isSimplifiedChinese(context: Context): Boolean {
        val local = getLocale(context)
        return local.language == Locale.SIMPLIFIED_CHINESE.language &&
                local.country == Locale.SIMPLIFIED_CHINESE.country
    }

    private fun getLocale(context: Context): Locale {
        val configuration = context.resources.configuration
        var local = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            configuration.locale
        }
        if (local == null) {
            local = Locale.getDefault()
        }
        return local
    }

    fun checkAppOps(context: Context): Boolean {
        val appOpsManager = context.requireSystemService<AppOpsManager>()
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            appOpsManager.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
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
                val intent = Intent(
                    Settings.ACTION_USAGE_ACCESS_SETTINGS, "package:${context.packageName}"
                )
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
            negativeButton(android.R.string.cancel) {
                denied?.invoke()
            }
        }
    }

    fun collectionDiagnosis(app: NativeToolsApp): String {
        val sb = StringBuilder()
            .appendPart("Manufacturer", Build.MANUFACTURER)
            .appendPart("Brand", Build.BRAND)
            .appendPart("Model", Build.MODEL)
            .appendPart("Product", Build.PRODUCT)
            .appendPart("Display", Build.DISPLAY)
            .appendPart("Android version", Build.VERSION.RELEASE)
            .appendPart("Sdk", Build.VERSION.SDK_INT)
            .appendPart("Screen width", UI.displayMetrics().widthPixels)
            .appendPart("Screen height", UI.displayMetrics().heightPixels)
            .appendPart("Density dpi", UI.displayMetrics().densityDpi)
            .appendPart("SmallestScreenWidthDp", UI.resources.configuration.smallestScreenWidthDp)

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
            .appendPart("Locale", getLocale(app))
            .appendPart("Status", NetSpeedPreferences.status)
            .appendPart("Free reflection", app.unseal)
            .appendPart("Notification", NetSpeedNotificationHelper.areNotificationEnabled(app))
            .appendPart("Stats", NetStats.getInstance().name)
            .appendPart("TotalTxBytes", TrafficStats.getTotalTxBytes())
            .appendPart("TotalRxBytes", TrafficStats.getTotalRxBytes())
            .appendPart("AppOps", checkAppOps(app))
            .appendPart("Today usage", NetworkUsageUtil.networkUsageDiagnosis(app))

        return sb.toString()
    }

    private fun StringBuilder.appendPart(first: String, second: Any?): StringBuilder {
        return this.append(first).append(": ").appendLine(second)
    }
}