package com.dede.nativetools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.dede.nativetools.netspeed.NetSpeedConfiguration
import com.dede.nativetools.netspeed.NetSpeedService
import com.dede.nativetools.netspeed.NetSpeedConfiguration.Companion.defaultSharedPreferences

class LauncherReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_LAUNCHER = "com.dede.netavetools.LAUNCHER"
        private val actions = arrayOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            ACTION_LAUNCHER
        )

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
        if (!actions.contains(action)) return

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            // 开机自启
            val autoBoot = defaultSharedPreferences.getBoolean(
                NetSpeedConfiguration.KEY_NET_SPEED_AUTO_START,
                false
            )
            if (!autoBoot) {
                return
            }
        }

        launcher(context)
    }
}