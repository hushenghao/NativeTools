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

typealias OnCloseCallback = () -> Unit

class NetSpeedServiceController(context: Context) : INetSpeedInterface.Default(),
    ServiceConnection {

    private val appContext = context.applicationContext

    private var binder: INetSpeedInterface? = null

    var onCloseCallback: OnCloseCallback? = null

    private val closeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val onCloseCallback = onCloseCallback
            unbindService()
            NetSpeedPreferences.status = false
            onCloseCallback?.invoke()
        }
    }

    val status: Boolean
        get() = NetSpeedPreferences.status

    fun startService(bind: Boolean = false) {
        if (!status) return
        val intent = NetSpeedService.createIntent(appContext)
        ContextCompat.startForegroundService(appContext, intent)
        if (bind) {
            bindService()
        }
    }

    fun bindService(onCloseCallback: OnCloseCallback? = null) {
        if (!status) return
        this.onCloseCallback = onCloseCallback
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
        onCloseCallback = null
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