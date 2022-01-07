package com.dede.nativetools

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.dede.nativetools.util.applyLauncherIcon
import com.dede.nativetools.util.isMainProcess
import com.dede.nativetools.util.tryApplyLauncherIcon
import com.google.android.material.color.DynamicColors

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
    }

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this, R.style.AppTheme)
        if (isMainProcess()) {
            applyLauncherIcon()
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        tryApplyLauncherIcon()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        tryApplyLauncherIcon()
    }
}