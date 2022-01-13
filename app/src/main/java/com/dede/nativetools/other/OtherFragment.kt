package com.dede.nativetools.other

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.dede.nativetools.R
import com.dede.nativetools.main.CircularReveal
import com.dede.nativetools.main.MainActivity
import com.dede.nativetools.main.SW600DP
import com.dede.nativetools.main.applyRecyclerViewInsets
import com.dede.nativetools.ui.NightModeDropDownPreference
import com.dede.nativetools.util.*
import kotlin.math.hypot
import kotlin.math.min

class OtherFragment : PreferenceFragmentCompat() {

    private val activityResultLauncherCompat =
        ActivityResultLauncherCompat(this, ActivityResultContracts.StartActivityForResult())

    private lateinit var preferenceIgnoreBatteryOptimize: SwitchPreferenceCompat

    private val delayChangeNightMode = Runnable {
        setNightMode(OtherPreferences.nightMode)
    }

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

        requirePreference<NightModeDropDownPreference>(OtherPreferences.KEY_NIGHT_MODE_TOGGLE).also {
            it.onNightModeSelected = { rect ->
                val activity = requireActivity()
                val decorView = activity.window.decorView
                (activity as MainActivity).circularReveal = CircularReveal(
                    rect.left + rect.width() / 2,
                    rect.top + rect.height() / 2,
                    min(rect.width() / 2f, rect.height() / 2f),
                    hypot(decorView.width.toFloat(), decorView.height.toFloat())
                )
            }
            it.setOnPreferenceChangeListener { _, _ ->
                // Wait for Popup to dismiss
                uiHandler.postDelayed(delayChangeNightMode, 200)
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