package com.dede.nativetools.netspeed.service

import android.content.*
import android.os.IBinder
import android.os.RemoteException
import androidx.core.content.ContextCompat
import com.dede.nativetools.netspeed.INetSpeedInterface
import com.dede.nativetools.netspeed.NetSpeedConfiguration
import com.dede.nativetools.netspeed.NetSpeedPreferences
import com.dede.nativetools.util.Intent
import com.dede.nativetools.util.toast

class NetSpeedServiceController(val context: Context) : INetSpeedInterface.Default(),
    ServiceConnection {

    private var binder: INetSpeedInterface? = null

    var onCloseReceive: Function0<Unit>? = null

    private val closeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            stopService()
            onCloseReceive?.invoke()
        }
    }

    fun startService(bind: Boolean = false) {
        if (!NetSpeedPreferences.status) return
        val intent = NetSpeedService.createIntent(context)
        ContextCompat.startForegroundService(context, intent)
        if (bind) {
            bindService()
        }
    }

    fun bindService() {
        if (!NetSpeedPreferences.status) return
        val intent = NetSpeedService.createIntent(context)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        context.registerReceiver(closeReceiver, IntentFilter(NetSpeedService.ACTION_CLOSE))
    }

    fun stopService() {
        val intent = Intent<NetSpeedService>(context)
        unbindService()
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

    override fun updateConfiguration(configuration: NetSpeedConfiguration) {
        try {
            binder?.updateConfiguration(configuration)
        } catch (e: RemoteException) {
            context.toast("error")
        }
    }
}