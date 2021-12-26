package com.dede.nativetools.main

import android.content.Intent
import android.graphics.Color
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.R
import com.dede.nativetools.databinding.ActivityMainBinding
import com.dede.nativetools.netspeed.NetSpeedPreferences
import com.dede.nativetools.netspeed.service.NetSpeedService
import com.dede.nativetools.util.extra
import com.dede.nativetools.util.isNightMode
import com.dede.nativetools.util.navController
import com.dede.nativetools.util.setNightMode
import com.google.android.material.color.MaterialColors
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView


/**
 * Main
 */
@StatusBarInsets
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
        val preferencesManager = WindowPreferencesManager(this)
        preferencesManager.applyEdgeToEdgePreference(window)

        val isToggle = intent.extra(EXTRA_TOGGLE, false)
        if (isToggle) {
            NetSpeedService.toggle(this)
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        setNightMode(NetSpeedPreferences.isNightMode)
        setSupportActionBar(binding.toolbar)

        applyBarsInsets(binding.root, binding.toolbar, this) {
            // fix statusBar blocking toolbar
            binding.root.requestLayout()
        }

        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            val color = MaterialColors.getColor(
                this,
                android.R.attr.colorBackground,
                if (isNightMode()) Color.BLACK else Color.WHITE
            )
            binding.navHostFragment.setBackgroundColor(color)
            window.setBackgroundDrawable(null)
            // Remove the default background, make the 'android:windowBackgroundFallback' effect, to split screen mode.
        }

        NavFragmentAssistant(supportFragmentManager)
            .setupWithNavFragment(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration.Builder(*topLevelDestinationIds).build()
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        // sw320dp
        binding.bottomNavigationView?.let {
            NavigationUI.setupWithNavController(it, navController)
            it.setOnItemSelectedListener(this)
        }
        // sw600dp
        binding.navigationRailView?.let {
            NavigationUI.setupWithNavController(it, navController)
            it.setOnItemSelectedListener(this)
        }
        //sw720dp
        binding.navigationView?.let {
            NavigationUI.setupWithNavController(it, navController)
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
        return NavigationUI.onNavDestinationSelected(item, navController)
    }

}
