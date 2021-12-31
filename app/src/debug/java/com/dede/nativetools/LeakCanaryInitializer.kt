package com.dede.nativetools

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import leakcanary.AppWatcher
import leakcanary.LeakCanary
import shark.AndroidReferenceMatchers

/**
 * LeakCanary install for debug
 * [leakcanary.internal.AppWatcherInstaller.onCreate]
 */
class LeakCanaryInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        LeakCanary.config = LeakCanary.config.copy(
            referenceMatchers = AndroidReferenceMatchers.appDefaults +
                    AndroidReferenceMatchers.instanceFieldLeak(
                        className = "android.graphics.animation.RenderNodeAnimator",
                        fieldName = "mTarget",
                        description = "DayNightSwitcher"
                    )
        )
        val application = context.applicationContext as Application
        AppWatcher.manualInstall(application)
    }

    override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}