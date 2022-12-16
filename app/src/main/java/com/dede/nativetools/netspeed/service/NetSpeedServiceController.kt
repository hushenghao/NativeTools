package com.dede.nativetools.netspeed.service

import android.app.ActivityManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.work.*
import com.dede.nativetools.netspeed.INetSpeedInterface
import com.dede.nativetools.netspeed.NetSpeedConfiguration
import com.dede.nativetools.util.BroadcastHelper
import com.dede.nativetools.util.Intent
import com.dede.nativetools.util.toast
import java.util.concurrent.TimeUnit

class NetSpeedServiceController(context: Context) :
    INetSpeedInterface.Default(), ServiceConnection {

    private val appContext = context.applicationContext

    private var binder: INetSpeedInterface? = null

    private val broadcastHelper = BroadcastHelper(NetSpeedService.ACTION_CLOSE)

    fun startService(bind: Boolean = false) {
        val intent = NetSpeedService.createIntent(appContext)
        ContextCompat.startForegroundService(appContext, intent)
        if (bind) {
            bindService()
        }

        HeartbeatWork.daemon(appContext)
    }

    fun bindService() {
        val intent = NetSpeedService.createIntent(appContext)
        appContext.bindService(intent, this, Context.BIND_AUTO_CREATE)
        broadcastHelper.register(appContext) { _, _ -> unbindService() }
    }

    fun stopService() {
        val intent = Intent<NetSpeedService>(appContext)
        unbindService()
        appContext.stopService(intent)

        HeartbeatWork.stop(appContext)
    }

    fun unbindService() {
        broadcastHelper.unregister(appContext)
        if (binder == null) {
            return
        }
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
