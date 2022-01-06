package com.dede.nativetools.other

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.dede.nativetools.R
import com.dede.nativetools.main.SW600DP
import com.dede.nativetools.main.applyRecyclerViewInsets
import com.dede.nativetools.util.*

class OtherFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val activityResultLauncherCompat =
        ActivityResultLauncherCompat(this, ActivityResultContracts.StartActivityForResult())

    private lateinit var preferenceIgnoreBatteryOptimize: SwitchPreferenceCompat

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (smallestScreenWidthDp < SW600DP) return
        applyRecyclerViewInsets(listView)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.other_preference)
        initOtherPreferenceGroup()
    }

    private fun initOtherPreferenceGroup() {
        requirePreference<Preference>(OtherPreferences.KEY_ABOUT).also {
            it.summary = requireContext().getVersionSummary()

            it.onPreferenceClickListener {
                findNavController().navigate(R.id.action_other_to_about)
            }
        }

        preferenceIgnoreBatteryOptimize =
            requirePreference<SwitchPreferenceCompat>(OtherPreferences.KEY_IGNORE_BATTERY_OPTIMIZE).apply {
                setOnPreferenceChangeListener { _, newValue ->
                    val ignoreBatteryOptimization = newValue as Boolean
                    if (ignoreBatteryOptimization) {
                        @SuppressLint("BatteryLife")
                        val intent = Intent(
                            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                            "package:${requireContext().packageName}"
                        )
                        activityResultLauncherCompat.launch(intent) { _ ->
                            checkIgnoreBatteryOptimize()
                        }
                    } else {
                        val intent =
                            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        startActivity(intent)
                        toast(getString(R.string.toast_open_battery_optimization))
                    }
                    return@setOnPreferenceChangeListener true
                }
            }
    }

    private fun checkIgnoreBatteryOptimize() {
        val context = requireContext()
        val powerManager = context.requireSystemService<PowerManager>()
        preferenceIgnoreBatteryOptimize.isChecked =
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    override fun onStart() {
        super.onStart()
        globalPreferences.registerOnSharedPreferenceChangeListener(this)

        checkIgnoreBatteryOptimize()
    }

    override fun onStop() {
        super.onStop()
        globalPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            OtherPreferences.KEY_NIGHT_MODE_TOGGLE -> {
                val mode = OtherPreferences.nightMode
                showToggleLauncherIconDialog(mode) {
                    setNightMode(mode)
                }
            }
        }
    }

    private fun showToggleLauncherIconDialog(mode: Int, finish: () -> Unit) {
        if (mode != AppCompatDelegate.MODE_NIGHT_NO && mode != AppCompatDelegate.MODE_NIGHT_YES) {
            finish.invoke()
            return
        }
        requireContext().alert(
            R.string.alert_title_toggle_launcher_icon,
            R.string.alert_msg_toggle_launcher_icon
        ) {
            positiveButton(android.R.string.ok) {
                finish.invoke()
                toggleLauncherIcon(mode)
            }
            negativeButton(android.R.string.cancel) {
                finish.invoke()
            }
        }
    }

    private fun toggleLauncherIcon(mode: Int) {
        if (mode != AppCompatDelegate.MODE_NIGHT_NO && mode != AppCompatDelegate.MODE_NIGHT_YES) {
            return
        }

        val packageName = globalContext.packageName
        val packageManager = globalContext.packageManager
        val componentDay = ComponentName(globalContext, "$packageName.main.Day")
        val componentNight = ComponentName(globalContext, "$packageName.main.Night")

        when (mode) {
            AppCompatDelegate.MODE_NIGHT_NO -> {
                componentDay.enable(packageManager)
                componentNight.disable(packageManager)
            }
            AppCompatDelegate.MODE_NIGHT_YES -> {
                componentNight.enable(packageManager)
                componentDay.disable(packageManager)
            }
        }
    }

}