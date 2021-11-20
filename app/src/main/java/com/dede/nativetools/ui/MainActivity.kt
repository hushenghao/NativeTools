package com.dede.nativetools.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.*
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.R
import com.dede.nativetools.databinding.ActivityMainBinding
import com.dede.nativetools.netspeed.NetSpeedPreferences
import com.dede.nativetools.netspeed.NetSpeedService
import com.dede.nativetools.util.extra
import com.dede.nativetools.util.navController
import com.dede.nativetools.util.setNightMode
import com.google.android.material.navigation.NavigationBarView

/**
 * Main
 */
class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener,
    NavController.OnDestinationChangedListener {

    companion object {
        private const val EXTRA_TOGGLE = "extra_toggle"
    }

    private val binding by viewBinding(ActivityMainBinding::bind)
    private val navController by navController(R.id.nav_host_fragment)
    private val topLevelDestinationIds = intArrayOf(R.id.netSpeed, R.id.other, R.id.about)

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

        FragmentTransitionManager()
            .attach(supportFragmentManager.findFragmentById(R.id.nav_host_fragment))
        val appBarConfiguration = AppBarConfiguration.Builder(*topLevelDestinationIds).build()
        setupActionBarWithNavController(this, navController, appBarConfiguration)
        setupWithNavController(binding.bottomNavigationView, navController)
        binding.bottomNavigationView.setOnItemSelectedListener(this)
        navController.addOnDestinationChangedListener(this)

        navController.handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navController.handleDeepLink(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        if (destination is DialogFragmentNavigator.Destination) {
            return
        }
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == navController.currentDestination?.id) {
            return false
        }
        return onNavDestinationSelected(item, navController)
    }

}
