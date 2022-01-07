package com.dede.nativetools.main

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
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
import com.google.android.material.color.MaterialColors


/**
 * Main
 */
@StatusBarInsets
class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener,
    NavigationBars.NavigationItemSelectedListener {

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
        setNightMode(OtherPreferences.nightMode)
        setSupportActionBar(binding.toolbar)

        applyBarsInsets(binding.root, binding.toolbar, this)

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
        NavigationBars.setupWithNavController(
            navController, this,
            binding.bottomNavigationView,   // default
            binding.navigationRailView,     // sw600dp
            binding.navigationView          // sw720dp
        )

        navController.addOnDestinationChangedListener(this)

        navController.handleDeepLink(intent)

        installShortcuts()
    }

    private fun createShortcutIcon(resId: Int): IconCompat {
        val context = this
        val bitmap = LayerDrawable(
            arrayOf(
                GradientDrawable().apply {
                    setColor(context.color(R.color.primaryColor))
                    shape = GradientDrawable.OVAL
                },
                InsetDrawable(this.requireDrawable<Drawable>(resId).apply {
                    setTint(Color.WHITE)
                }, 4.dp)
            )
        ).toBitmap(24.dp, 24.dp)
        return IconCompat.createWithBitmap(bitmap)
    }

    private fun installShortcuts() {
        val shortcuts = arrayListOf(
            ShortcutInfoCompat.Builder(this, "shortcut_about")
                .setIcon(createShortcutIcon(R.drawable.ic_outline_info))
                .setIntent(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://dede.nativetools/about"))
                        .setClass(this, MainActivity::class.java)
                )
                .setShortLabel(getString(R.string.label_about))
                .setLongLabel(getString(R.string.label_about))
                .build(),
            ShortcutInfoCompat.Builder(this, "shortcut_toggle")
                .setIcon(createShortcutIcon(R.drawable.ic_outline_toggle_on))
                .setIntent(
                    Intent(Intent.ACTION_VIEW, EXTRA_TOGGLE to true)
                        .setClass(this, MainActivity::class.java)
                )
                .setShortLabel(getString(R.string.label_net_speed_toggle))
                .setLongLabel(getString(R.string.label_net_speed_toggle))
                .build()
        )
        for (shortcut in shortcuts) {
            ShortcutManagerCompat.pushDynamicShortcut(this, shortcut)
        }
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
        return NavigationUI.onNavDestinationSelected(item, navController)
    }

}
