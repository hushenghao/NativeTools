package com.dede.nativetools.netspeed.service

import android.content.*
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.dede.nativetools.netspeed.INetSpeedInterface
import com.dede.nativetools.netspeed.NetSpeedPreferences

class NetSpeedServiceController(val context: Context) : ServiceConnection {

    private val intent = NetSpeedService.createIntent(context)

    var binder: INetSpeedInterface? = null
        private set

    var onCloseReceive: Function0<Unit>? = null

    private val closeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            stopService()
            onCloseReceive?.invoke()
        }
    }

    fun startService(bind: Boolean = false) {
        if (!NetSpeedPreferences.status) return
        ContextCompat.startForegroundService(context, intent)
        if (bind) {
            bindService()
        }
    }

    fun bindService() {
        if (!NetSpeedPreferences.status) return
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        context.registerReceiver(closeReceiver, IntentFilter(NetSpeedService.ACTION_CLOSE))
    }

    fun stopService() {
        context.unbindService(this)
        context.stopService(intent)
    }

    fun unbindService() {
        if (binder == null) {
            return
        }
        context.unregisterReceiver(closeReceiver)
        context.unbindService(this)
        binder = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        binder = INetSpeedInterface.Stub.asInterface(service)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        binder = null
    }
}