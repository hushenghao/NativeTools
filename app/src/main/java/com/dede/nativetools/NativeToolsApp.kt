package com.dede.nativetools

import android.app.Application
import android.content.Context
import com.dede.nativetools.ui.LauncherReceiver
import me.weishu.reflection.Reflection

class NativeToolsApp : Application() {

    companion object {
        private var instance: NativeToolsApp? = null
        fun getInstance(): NativeToolsApp {
            return checkNotNull(instance) {}
        }
    }

    override fun attachBaseContext(base: Context?) {
        instance = this
        super.attachBaseContext(base)
        Reflection.unseal(base)
    }

    override fun onCreate() {
        super.onCreate()
        LauncherReceiver.launcher(this)
    }
}