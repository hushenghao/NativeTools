package com.dede.nativetools

import android.app.Application
import com.dede.nativetools.ui.LauncherReceiver

class NativeToolsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        LauncherReceiver.launcher(this)
    }
}