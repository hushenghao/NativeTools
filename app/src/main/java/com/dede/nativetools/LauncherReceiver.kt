package com.dede.nativetools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.dede.nativetools.netspeed.NetSpeedFragment

class LauncherReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_LAUNCHER = "com.dede.netavetools.LAUNCHER"
        private val actions = arrayOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            ACTION_LAUNCHER
        )

        fun launcher(
            context: Context,
            preference: SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context)
        ) {
            if (preference.getBoolean(NetSpeedFragment.KEY_NET_SPEED_STATUS, false)) {
                val intent = NetSpeedFragment.createServiceIntent(context, preference)
                ContextCompat.startForegroundService(context, intent)
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        Log.i("LauncherReceiver", "onReceive: $action")
        if (context == null || !actions.contains(action)) return

        val preference = PreferenceManager.getDefaultSharedPreferences(context)
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            // 开机自启
            if (!preference.getBoolean(NetSpeedFragment.KEY_NET_SPEED_AUTO_START, false)) {
                return
            }
        }

        launcher(context, preference)
    }
}