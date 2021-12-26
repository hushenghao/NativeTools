package com.dede.nativetools

import android.app.Application
import android.content.Context
import android.util.Log
import com.dede.nativetools.netspeed.service.NetSpeedNotificationHelper
import com.google.android.material.color.DynamicColors
import leakcanary.LeakCanary
import me.weishu.reflection.Reflection
import shark.AndroidReferenceMatchers

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
        LeakCanary.config = LeakCanary.config.copy(
            referenceMatchers = AndroidReferenceMatchers.appDefaults +
                    AndroidReferenceMatchers.instanceFieldLeak(
                        className = "android.graphics.animation.RenderNodeAnimator",
                        fieldName = "mTarget",
                        description = "DayNightSwitcher"
                    )
        )
        DynamicColors.applyToActivitiesIfAvailable(this, R.style.AppTheme)
        NetSpeedNotificationHelper.checkNotificationChannelAndUpgrade(this)
    }
}