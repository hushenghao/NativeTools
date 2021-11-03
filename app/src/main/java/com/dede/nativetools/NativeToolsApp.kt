package com.dede.nativetools

import android.app.Application
import android.content.Context
import android.util.Log
import com.dede.nativetools.netspeed.NetSpeedNotificationHelper
import me.weishu.reflection.Reflection

class NativeToolsApp : Application() {

    companion object {
        private var instance: NativeToolsApp? = null

        fun getInstance(): NativeToolsApp {
            return checkNotNull(instance)
        }
    }

    override fun attachBaseContext(base: Context?) {
        instance = this
        super.attachBaseContext(base)
        val result = Reflection.unseal(base)
        Log.i("NativeToolsApp", "unseal: $result")
    }

    override fun onCreate() {
        super.onCreate()
        NetSpeedNotificationHelper.checkNotificationChannelAndUpgrade(this)
    }
}