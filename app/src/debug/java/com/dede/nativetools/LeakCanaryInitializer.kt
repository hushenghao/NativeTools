package com.dede.nativetools

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import leakcanary.AppWatcher
import leakcanary.LeakCanary
import shark.AndroidReferenceMatchers

/**
 * LeakCanary install for debug
 * [leakcanary.internal.AppWatcherStartupInitializer.create]
 */
class LeakCanaryInitializer : Initializer<LeakCanaryInitializer> {

    override fun create(context: Context) = apply {
        val application = context.applicationContext as Application
        AppWatcher.manualInstall(application)
        LeakCanary.config = LeakCanary.config.copy(
            referenceMatchers = AndroidReferenceMatchers.appDefaults +
//                    AndroidReferenceMatchers.instanceFieldLeak(
//                        className = "android.graphics.animation.RenderNodeAnimator",
//                        fieldName = "mTarget",
//                        description = "DayNightSwitcher"
//                    ) +
                    AndroidReferenceMatchers.instanceFieldLeak(
                        className = "leakcanary.ToastEventListener",
                        fieldName = "toastCurrentlyShown",
                        description = "Leakcanary toast"
                    )
        )
    }

    override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}