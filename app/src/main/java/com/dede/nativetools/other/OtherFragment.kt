package com.dede.nativetools.other

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
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
        if (requireContext().smallestScreenWidthDp < SW600DP) return
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
                setNightMode(OtherPreferences.isNightMode)
            }
        }
    }

}