package com.dede.nativetools.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

typealias OnReceiver = (action: String?, intent: Intent?) -> Unit

/**
 * BroadcastReceiver Helper
 *
 * @since 2022/7/20
 */
class BroadcastHelper(private vararg val actions: String) {

    private var broadcastReceiver: BroadcastReceiver? = null

    fun register(context: Context, onReceiver: OnReceiver) {
        unregister(context)

        val broadcastReceiver =
            object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    onReceiver.invoke(intent?.action, intent)
                }
            }
        val intentFilter = IntentFilter(*actions)
        context.registerReceiver(broadcastReceiver, intentFilter)
        this.broadcastReceiver = broadcastReceiver
    }

    fun unregister(context: Context) {
        if (broadcastReceiver == null) {
            return
        }
        context.unregisterReceiver(broadcastReceiver)
        broadcastReceiver = null
    }
}
