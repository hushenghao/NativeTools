package com.dede.nativetools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dede.nativetools.netspeed.NetSpeedPreferences
import com.dede.nativetools.netspeed.service.NetSpeedService

class LauncherReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.i("LauncherReceiver", "onReceive: $action")

        when (action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // 开机自启的设置状态
                val autoBoot = NetSpeedPreferences.autoStart
                if (autoBoot) {
                    NetSpeedService.launchForeground(context)
                }
            }
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                NetSpeedService.launchForeground(context)
            }
        }
    }
}