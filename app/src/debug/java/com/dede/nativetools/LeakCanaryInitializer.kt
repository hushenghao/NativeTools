package com.dede.nativetools

import android.content.Context
import androidx.startup.Initializer
import leakcanary.LeakCanary
import shark.AndroidReferenceMatchers

/**
 * LeakCanary config for debug
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
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}