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

class NetSpeedServiceController(context: Context) : INetSpeedInterface.Default(),
    ServiceConnection {

    private val appContext = context.applicationContext

    private var binder: INetSpeedInterface? = null

    var onCloseCallback: Function0<Unit>? = null

    private val closeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val onCloseCallback = onCloseCallback
            stopService()
            NetSpeedPreferences.status = false
            onCloseCallback?.invoke()
        }
    }

    fun startService(bind: Boolean = false) {
        if (!NetSpeedPreferences.status) return
        val intent = NetSpeedService.createIntent(appContext)
        ContextCompat.startForegroundService(appContext, intent)
        if (bind) {
            bindService()
        }
    }

    fun bindService() {
        if (!NetSpeedPreferences.status) return
        val intent = NetSpeedService.createIntent(appContext)
        appContext.bindService(intent, this, Context.BIND_AUTO_CREATE)
        appContext.registerReceiver(closeReceiver, IntentFilter(NetSpeedService.ACTION_CLOSE))
    }

    fun stopService() {
        val intent = Intent<NetSpeedService>(appContext)
        unbindService()
        appContext.stopService(intent)
    }

    fun unbindService() {
        if (binder == null) {
            return
        }
        appContext.unregisterReceiver(closeReceiver)
        appContext.unbindService(this)
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
            appContext.toast("error")
        }
    }
}