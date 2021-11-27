package com.dede.nativetools.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
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
import com.dede.nativetools.util.setupWithNavController
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView

/**
 * Main
 */
class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener,
    NavigationBarView.OnItemSelectedListener,
    NavigationView.OnNavigationItemSelectedListener {

    companion object {
        private const val EXTRA_TOGGLE = "extra_toggle"
    }

    private val binding by viewBinding(ActivityMainBinding::bind)
    private val navController by navController(R.id.nav_host_fragment)
    private val topLevelDestinationIds = intArrayOf(R.id.netSpeed, R.id.other, R.id.about)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val option =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        val decorView = window.decorView
        val visibility: Int = decorView.systemUiVisibility
        decorView.systemUiVisibility = visibility or option

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
        // sw320dp
        binding.bottomNavigationView?.let {
            setupWithNavController(it, navController)
            it.setOnItemSelectedListener(this)
        }
        // sw600dp
        binding.navigationRailView?.let {
            setupWithNavController(it, navController)
            it.setOnItemSelectedListener(this)
        }
        //sw720dp
        binding.navigationView?.let {
            setupWithNavController(it, navController)
            it.setNavigationItemSelectedListener(this)
        }

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
        val motionLayout = binding.root as? MotionLayout ?: return
        if (topLevelDestinationIds.contains(destination.id)) {
            if (motionLayout.progress != 0f) {
                motionLayout.transitionToStart()
            }
        } else {
            if (motionLayout.progress != 100f) {
                motionLayout.transitionToEnd()
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
