package com.dede.nativetools.main

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewAnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import androidx.core.view.doOnAttach
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.R
import com.dede.nativetools.databinding.ActivityMainBinding
import com.dede.nativetools.netspeed.service.NetSpeedService
import com.dede.nativetools.other.OtherPreferences
import com.dede.nativetools.util.*

/**
 * Main
 */
class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener,
    NavigationBars.NavigationItemSelectedListener {

    companion object {
        const val EXTRA_TOGGLE = "extra_toggle"
    }

    private val binding by viewBinding(ActivityMainBinding::bind)
    private val navController by navController(R.id.nav_host_fragment)
    private val topLevelDestinationIds = intArrayOf(R.id.netSpeed, R.id.other)

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val windowEdgeManager = WindowEdgeManager(this)
        windowEdgeManager.applyEdgeToEdge(window)

        val isToggle = intent.extra(EXTRA_TOGGLE, false)
        if (isToggle) {
            NetSpeedService.toggle(this)
            finish()
            return
        }

        val circularReveal = viewModel.getCircularRevealAndClean()
        if (circularReveal != null) {
            val decorView = window.decorView
            decorView.doOnAttach {
                ViewAnimationUtils.createCircularReveal(
                    decorView,
                    circularReveal.centerX,
                    circularReveal.centerY,
                    circularReveal.startRadius,
                    circularReveal.endRadius
                ).apply {
                    duration = 1200
                    start()
                }
            }
        }

        setContentView(R.layout.activity_main)
        setNightMode(OtherPreferences.nightMode)
        setSupportActionBar(binding.toolbar)

        applyBarsInsets(
            root = binding.root,
            top = binding.toolbar,  // status bar
            left = binding.toolbar, // navigation bar, Insert padding only in the toolbar
            right = binding.root,   // navigation bar
            // Some devices have navigation bars on the side, when landscape.
        )

        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            val color = this.color(
                android.R.attr.colorBackground,
                if (isNightMode()) Color.BLACK else Color.WHITE
            )
            binding.navHostFragment.setBackgroundColor(color)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // Remove the default background, make the 'android:windowBackgroundFallback' effect, to split screen mode.
        }

        NavFragmentAssistant(supportFragmentManager)
            .setupWithNavFragment(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration.Builder(*topLevelDestinationIds).build()
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationBars.setupWithNavController(
            navController, this,
            binding.bottomNavigationView,
            binding.navigationRailView
        )

        if (UI.isSmallestScreenWidthDpAtLast(UI.SW600DP) || UI.isLandscape) {
            binding.navigationRailView.isVisible = true
            binding.bottomNavigationView.isGone = true
        } else {
            binding.bottomNavigationView.isVisible = true
            binding.navigationRailView.isGone = true
            navController.addOnDestinationChangedListener(this)
        }

        navController.handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navController.handleDeepLink(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        if (destination is DialogFragmentNavigator.Destination) {
            return
        }
        val motionLayout = binding.motionLayout as? MotionLayout ?: return
        if (topLevelDestinationIds.contains(destination.id)) {
            motionLayout.transitionToStart()
        } else {
            motionLayout.transitionToEnd()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item, navController)
    }

}
