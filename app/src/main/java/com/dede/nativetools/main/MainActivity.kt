package com.dede.nativetools.main

import android.content.Intent
import android.graphics.Color
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.MenuItem
import android.view.ViewAnimationUtils
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
import com.dede.nativetools.netspeed.service.NetSpeedService
import com.dede.nativetools.other.OtherPreferences
import com.dede.nativetools.util.*
import kotlinx.parcelize.Parcelize

@Parcelize
data class CircularReveal(
    val centerX: Int,
    val centerY: Int,
    val startRadius: Float,
    val endRadius: Float
) : Parcelable

/**
 * Main
 */
@StatusBarInsets
class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener,
    NavigationBars.NavigationItemSelectedListener {

    companion object {
        const val EXTRA_TOGGLE = "extra_toggle"
        private const val STATE_CIRCULAR_REVEAL = "state_circular_reveal"
    }

    private val binding by viewBinding(ActivityMainBinding::bind)
    private val navController by navController(R.id.nav_host_fragment)
    private val topLevelDestinationIds = intArrayOf(R.id.netSpeed, R.id.other, R.id.about)

    var circularReveal: CircularReveal? = null

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

        val circularReveal =
            savedInstanceState?.getParcelable<CircularReveal>(STATE_CIRCULAR_REVEAL)
        if (circularReveal != null) {
            ViewAnimationUtils.createCircularReveal(
                window.decorView,
                circularReveal.centerX,
                circularReveal.centerY,
                circularReveal.startRadius,
                circularReveal.endRadius
            ).apply {
                duration = 800
                start()
            }
        }

        setContentView(R.layout.activity_main)
        setNightMode(OtherPreferences.nightMode)
        setSupportActionBar(binding.toolbar)

        applyBarsInsets(binding.root, binding.toolbar, this)

        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            val color = this.color(
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
        NavigationBars.setupWithNavController(
            navController, this,
            binding.bottomNavigationView,   // default
            binding.navigationRailView,     // sw600dp
            binding.navigationView          // sw720dp
        )

        navController.addOnDestinationChangedListener(this)

        navController.handleDeepLink(intent)
    }

    override fun onNightModeChanged(mode: Int) {
        super.onNightModeChanged(mode)
        Log.i("TAG", "onNightModeChanged: " + mode)
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
        return NavigationUI.onNavDestinationSelected(item, navController)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE_CIRCULAR_REVEAL, circularReveal)
        circularReveal = null
    }

}
