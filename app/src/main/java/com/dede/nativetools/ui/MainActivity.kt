package com.dede.nativetools.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
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
import com.google.android.material.transition.MaterialFadeThrough

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
        binding.bottomNavigationView.setOnItemSelectedListener {
            if (it.itemId == navController.currentDestination?.id) {
                return@setOnItemSelectedListener true
            }
            NavigationUI.onNavDestinationSelected(it, navController)
            return@setOnItemSelectedListener true
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination is DialogFragmentNavigator.Destination) {
                return@addOnDestinationChangedListener
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

        navController.handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navController.handleDeepLink(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    class NavHostFragment : androidx.navigation.fragment.NavHostFragment() {

        private val materialFadeThrough = MaterialFadeThrough()

        private val transitionLifecycleCallbacks =
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentViewCreated(
                    fm: FragmentManager,
                    f: Fragment,
                    v: View,
                    savedInstanceState: Bundle?
                ) {
                    if (f is DialogFragment) {
                        return
                    }
                    f.enterTransition = materialFadeThrough.addTarget(v)
                }

                override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                    if (f is DialogFragment) {
                        return
                    }
                    f.exitTransition = materialFadeThrough
                    materialFadeThrough.removeTarget(f.requireView())
                }
            }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            childFragmentManager.registerFragmentLifecycleCallbacks(
                transitionLifecycleCallbacks,
                true
            )
        }

        override fun onDestroy() {
            childFragmentManager.unregisterFragmentLifecycleCallbacks(transitionLifecycleCallbacks)
            super.onDestroy()
        }
    }

}
