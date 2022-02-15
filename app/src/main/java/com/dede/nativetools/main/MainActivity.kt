package com.dede.nativetools.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.LayoutInflaterCompat
import androidx.core.view.doOnAttach
import androidx.core.view.isGone
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.R
import com.dede.nativetools.databinding.ActivityMainBinding
import com.dede.nativetools.netspeed.service.NetSpeedService
import com.dede.nativetools.other.OtherPreferences
import com.dede.nativetools.ui.NavigatePreference
import com.dede.nativetools.util.*

/**
 * Main
 */
class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener,
    NavigatePreference.OnNavigateHandler {

    companion object {
        const val EXTRA_TOGGLE = "extra_toggle"
    }

    private val binding by viewBinding {
        ActivityMainBinding.inflate(it.getLayoutInflater(!UI.isWideSize()))
    }
    private val navController by navController(R.id.nav_host_fragment)
    private val topLevelDestinationIds = intArrayOf(R.id.netSpeed, R.id.other)
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isToggle = intent.extra(EXTRA_TOGGLE, false)
        if (isToggle) {
            NetSpeedService.toggle(this)
            finish()
            return
        }

        val windowEdgeManager = WindowEdgeManager(this)
        windowEdgeManager.applyEdgeToEdge(window)
        setNightMode(OtherPreferences.nightMode)

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
                    duration = 1000
                    start()
                }
            }
        }

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        applyBarsInsets(
            root = binding.root,
            // top status bar, android:fitsSystemWindows="true"
            left = binding.toolbar,         // navigation bar, Insert padding only in the toolbar
            right = binding.motionLayout,   // navigation bar
            // Some devices have navigation bars on the side, when landscape.
        ) {
            val systemBar = it.systemBar()
            binding.navigationView.updatePadding(left = systemBar.left)
        }

        NavFragmentAssistant(supportFragmentManager)
            .setupWithNavFragment(R.id.nav_host_fragment)
        val appBarBuilder = AppBarConfiguration.Builder(*topLevelDestinationIds)
        if (UI.isWideSize()) {
            // bind drawer
            appBarBuilder.setOpenableLayout(binding.drawerLayout)
            val headerView =
                binding.navigationView.inflateHeaderView(R.layout.layout_navigation_header)
            headerView.findViewById<TextView>(R.id.tv_version).text = this.getVersionSummary()
            // hide bottomNavigationView
            binding.bottomNavigationView.isGone = true
        } else {
            // lock drawer
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            // hide navigationRailView
            binding.navigationRailView.isGone = true
            navController.addOnDestinationChangedListener(this)
        }
        appBarConfiguration = appBarBuilder.build()
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationBars.setupWithNavController(
            navController = navController,
            bottomNavigationView = binding.bottomNavigationView,
            navigationRailView = binding.navigationRailView,
            navigationView = binding.navigationView
        )

        navController.handleDeepLink(intent)
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?,
    ) {
        if (destination is DialogFragmentNavigator.Destination) {
            return
        }
        if (destination.matchDestinations(topLevelDestinationIds)) {
            binding.motionLayout.transitionToStart()
        } else {
            binding.motionLayout.transitionToEnd()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home && navController.navigateUp(appBarConfiguration)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navController.handleDeepLink(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }

    /**
     * NavigatePreference点击跳转
     * @see NavigatePreference.OnNavigateHandler
     */
    override fun handleNavigate(id: Int) {
        navController.navigate(id)
    }

    private fun getLayoutInflater(enableScene: Boolean): LayoutInflater {
        val layoutInflater = LayoutInflater.from(this)
        if (enableScene) {
            return layoutInflater
        }
        val cloneInflater = layoutInflater.cloneInContext(this)
        LayoutInflaterCompat.setFactory2(cloneInflater, MotionLayoutFactory())
        return cloneInflater
    }

    private class MotionLayoutFactory : LayoutInflater.Factory2 {
        override fun onCreateView(
            parent: View?,
            name: String,
            context: Context,
            attrs: AttributeSet,
        ): View? {
            if (name == MotionLayout::class.qualifiedName) {
                // remove app:layoutDescription="@xml/activity_main_scene", disable scene
                val template = MotionLayout(context, attrs)
                return MotionLayout(context).apply {
                    id = template.id
                }
            }
            return null
        }

        override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
            return null
        }
    }
}
