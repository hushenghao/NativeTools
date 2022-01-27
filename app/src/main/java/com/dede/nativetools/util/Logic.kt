package com.dede.nativetools.util

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatDelegate
import com.dede.nativetools.R
import com.dede.nativetools.other.OtherPreferences
import java.util.*

fun isNightMode(): Boolean {
    when (OtherPreferences.nightMode) {
        AppCompatDelegate.MODE_NIGHT_YES -> {
            return true
        }
        AppCompatDelegate.MODE_NIGHT_UNSPECIFIED,
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY -> {
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

object Logic {

    fun isSimplifiedChinese(context: Context): Boolean {
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
        return local.language == Locale.SIMPLIFIED_CHINESE.language &&
                local.country == Locale.SIMPLIFIED_CHINESE.country
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
        denied: (() -> Unit)? = null
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
}