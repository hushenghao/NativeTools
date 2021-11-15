package com.dede.nativetools.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.R
import com.dede.nativetools.databinding.ActivityMainBinding
import com.dede.nativetools.netspeed.NetSpeedPreferences
import com.dede.nativetools.netspeed.NetSpeedService
import com.dede.nativetools.util.extra
import com.dede.nativetools.util.navController
import com.dede.nativetools.util.setNightMode

/**
 * Main
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_TOGGLE = "extra_toggle"
    }

    private val binding by viewBinding(ActivityMainBinding::bind)
    private val navController by navController(R.id.nav_host_fragment)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isToggle = intent.extra(EXTRA_TOGGLE, false)
        if (isToggle) {
            NetSpeedService.toggle(this)
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        setNightMode(NetSpeedPreferences.isNightMode)
        setSupportActionBar(binding.toolbar)

        val topLevelDestinationIds = intArrayOf(R.id.netSpeed, R.id.other, R.id.about)
        val appBarConfiguration = AppBarConfiguration.Builder(*topLevelDestinationIds)
            .build()
        setupActionBarWithNavController(this, navController, appBarConfiguration)
        setupWithNavController(binding.bottomNavigationView, navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (topLevelDestinationIds.contains(destination.id)) {
                if (binding.motionLayout.progress != 0f) {
                    binding.motionLayout.transitionToStart()
                }
            } else {
                if (binding.motionLayout.progress != 100f) {
                    binding.motionLayout.transitionToEnd()
                }
            }
        }

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
