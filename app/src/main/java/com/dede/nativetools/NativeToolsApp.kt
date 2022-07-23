package com.dede.nativetools

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.dede.nativetools.netspeed.NetSpeedPreferences
import com.dede.nativetools.other.OtherPreferences
import com.dede.nativetools.util.installShortcuts
import com.dede.nativetools.util.isMainProcess
import com.dede.nativetools.util.setNightMode
import com.google.android.material.R
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import me.weishu.reflection.Reflection

class NativeToolsApp : Application() {

    companion object {
        private var instance: NativeToolsApp? = null

        fun getInstance(): NativeToolsApp {
            return checkNotNull(instance)
        }

        private const val TAG = "NativeToolsApp"
    }

    var unseal: Int = -1
        private set

    override fun attachBaseContext(base: Context?) {
        instance = this
        super.attachBaseContext(base)
        val unseal = Reflection.unseal(base)
        Log.i(TAG, "unseal: $unseal")
        this.unseal = unseal
    }

    override fun onCreate() {
        super.onCreate()
        initFirebase()

        val options =
            DynamicColorsOptions.Builder()
                .setThemeOverlay(R.style.ThemeOverlay_Material3_DynamicColors_DayNight)
                .build()
        DynamicColors.applyToActivitiesIfAvailable(this, options)
        if (isMainProcess()) {
            installShortcuts()
            setNightMode(OtherPreferences.nightMode)
        }
    }

    private fun initFirebase() {
        if (FirebaseApp.initializeApp(this) == null) {
            Log.i(TAG, "FirebaseApp initialization unsuccessful")
        } else {
            Log.i(TAG, "FirebaseApp initialization successful")
        }
        if (NetSpeedPreferences.privacyAgreed) {
            Firebase.analytics.setAnalyticsCollectionEnabled(true)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        installShortcuts()
    }
}
