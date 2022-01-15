package com.dede.nativetools.other

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.dede.nativetools.R
import com.dede.nativetools.main.*
import com.dede.nativetools.ui.NightModeDropDownPreference
import com.dede.nativetools.util.*

class OtherFragment : PreferenceFragmentCompat() {

    private val activityResultLauncherCompat =
        ActivityResultLauncherCompat(this, ActivityResultContracts.StartActivityForResult())

    private val mainViewModel by activityViewModels<MainViewModel>()

    private lateinit var preferenceIgnoreBatteryOptimize: SwitchPreferenceCompat

    private val delayChangeNightMode = Runnable {
        setNightMode(OtherPreferences.nightMode)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isAtLast(SW600DP) || isLandscape) {
            applyBottomBarsInsets(listView)
        }
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

        requirePreference<NightModeDropDownPreference>(OtherPreferences.KEY_NIGHT_MODE_TOGGLE).also {
            it.onNightModeSelected = { rect ->
                val decorView = requireActivity().window.decorView
                mainViewModel.setCircularReveal(decorView, rect)
            }
            it.setOnPreferenceChangeListener { _, _ ->
                // Wait for Popup to dismiss
                uiHandler.postDelayed(delayChangeNightMode, 300)
                return@setOnPreferenceChangeListener true
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
        preferenceIgnoreBatteryOptimize.isChecked = requireContext().isIgnoringBatteryOptimizations
    }

    override fun onStart() {
        super.onStart()
        checkIgnoreBatteryOptimize()
    }

    override fun onDestroyView() {
        uiHandler.removeCallbacks(delayChangeNightMode)
        super.onDestroyView()
    }

}