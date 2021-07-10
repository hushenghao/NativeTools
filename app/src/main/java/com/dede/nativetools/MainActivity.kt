package com.dede.nativetools

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val supportedModes = window.windowManager.defaultDisplay.supportedModes
//        supportedModes.sortBy { it.refreshRate }
//        window.attributes.preferredDisplayModeId = supportedModes[0].modeId
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        setContentView(R.layout.main_activity)
    }

}
