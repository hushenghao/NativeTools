package com.dede.nativetools.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.dede.nativetools.ui.netspeed.NetSpeedFragment

class LauncherReceiver : BroadcastReceiver() {

    companion object {
        const val LAUNCHER_ACTION = "com.dede.netavetools.LAUNCHER"
        private val actions = arrayOf(Intent.ACTION_BOOT_COMPLETED, LAUNCHER_ACTION)

        fun launcher(context: Context) {
            val preference = PreferenceManager.getDefaultSharedPreferences(context)

            if (preference.getBoolean(NetSpeedFragment.KEY_NET_SPEED_STATUS, false)) {
                val intent = NetSpeedFragment.createServiceIntent(context, preference)
                context.startForegroundService(intent)
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || actions.contains(intent?.action)) return

        launcher(context)
    }
}