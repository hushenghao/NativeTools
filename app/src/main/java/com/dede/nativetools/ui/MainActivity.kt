package com.dede.nativetools.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.R
import com.dede.nativetools.databinding.ActivityMainBinding
import com.dede.nativetools.netspeed.NetSpeedPreferences
import com.dede.nativetools.netspeed.NetSpeedService
import com.dede.nativetools.util.setV28NightMode

/**
 * Main
 */
class MainActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityMainBinding::bind)
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.ThemeOverlay_Shapes_Rounded)
        setTheme(R.style.ThemeOverlay_ShapeSize_Large)
        super.onCreate(savedInstanceState)
        val isToggle = intent.getBooleanExtra("extra_toggle", false)
        if (isToggle) {
            NetSpeedService.toggle(this)
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        setV28NightMode(NetSpeedPreferences.v28NightMode)
        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(this, navController)

        navController.handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navController.handleDeepLink(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}
