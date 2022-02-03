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
        val sb = StringBuilder("Manufacturer: ")
            .append(Build.MANUFACTURER)
            .append("\nBrand: ")
            .append(Build.BRAND)
            .append("\nModel: ")
            .append(Build.MODEL)
            .append("\nProduct: ")
            .append(Build.PRODUCT)
            .append("\nDisplay: ")
            .append(Build.DISPLAY)
            .append("\nAndroid version: ")
            .append(Build.VERSION.RELEASE)
            .append("\nSdk: ")
            .append(Build.VERSION.SDK_INT)
            .append("\nScreen width: ")
            .append(UI.displayMetrics().widthPixels)
            .append("\nScreen height: ")
            .append(UI.displayMetrics().heightPixels)
            .append("\ndensityDpi: ")
            .append(UI.displayMetrics().densityDpi)
            .append("\nsmallestScreenWidthDp: ")
            .append(UI.resources.configuration.smallestScreenWidthDp)

        sb.append("\n---------------")
            .append("\nPackage name: ")
            .append(app.packageName)
            .append("\nBuild type: ")
            .append(BuildConfig.BUILD_TYPE)
            .append("\nApp version: ")
            .append(app.getVersionSummary())
            .append("\nTarget sdk: ")
            .append(app.applicationInfo.targetSdkVersion)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            sb.append("\nCompile sdk: ")
                .append(app.applicationInfo.compileSdkVersion)
        }
        sb.append("\nStatus: ")
            .append(NetSpeedPreferences.status)
            .append("\nFree reflection: ")
            .append(app.unseal)
            .append("\nProcess: ")
            .append(app.processName())
            .append("\nLocale: ")
            .append(getLocale(app))
            .append("\nNotification: ")
            .append(NetSpeedNotificationHelper.areNotificationEnabled(app))
            .append("\nStats: ")
            .append(NetStats.getInstance().name)
            .append("\nTotalTxBytes: ")
            .append(TrafficStats.getTotalTxBytes())
            .append("\nTotalRxBytes: ")
            .append(TrafficStats.getTotalRxBytes())
            .append("\nAppOps: ")
            .append(checkAppOps(app))
            .append("\nToday usage: ")
            .append(NetworkUsageUtil.networkUsageDiagnosis(app))

        return sb.toString()
    }
}