package com.dede.nativetools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.dede.nativetools.netspeed.NetSpeedConfiguration
import com.dede.nativetools.netspeed.NetSpeedConfiguration.Companion.defaultSharedPreferences
import com.dede.nativetools.netspeed.NetSpeedService

class LauncherReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_LAUNCHER = "com.dede.netavetools.LAUNCHER"

        fun launcher(context: Context) {
            val status = defaultSharedPreferences.getBoolean(
                NetSpeedConfiguration.KEY_NET_SPEED_STATUS,
                false
            )
            if (status) {
                val intent = NetSpeedService.createServiceIntent(context)
                ContextCompat.startForegroundService(context, intent)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.i("LauncherReceiver", "onReceive: $action")

        when (action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // 开机自启的设置状态
                val autoBoot = defaultSharedPreferences.getBoolean(
                    NetSpeedConfiguration.KEY_NET_SPEED_AUTO_START,
                    false
                )
                if (!autoBoot) {
                    return
                }
            }
            Intent.ACTION_MY_PACKAGE_REPLACED, ACTION_LAUNCHER -> {
                launcher(context)
            }
        }
    }
}