package com.dede.nativetools.other

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.postDelayed
import androidx.fragment.app.activityViewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.dede.nativetools.R
import com.dede.nativetools.main.MainViewModel
import com.dede.nativetools.main.applyBottomBarsInsets
import com.dede.nativetools.ui.NightModeDropDownPreference
import com.dede.nativetools.util.*

class OtherFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val activityResultLauncherCompat =
        ActivityResultLauncherCompat(this, ActivityResultContracts.StartActivityForResult())

    private val mainViewModel by activityViewModels<MainViewModel>()

    private lateinit var preferenceIgnoreBatteryOptimize: SwitchPreferenceCompat

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (UI.isWideSize()) {
            applyBottomBarsInsets(listView)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.other_preference)
        initOtherPreferenceGroup()
    }

    private fun initOtherPreferenceGroup() {
        requirePreference<Preference>(OtherPreferences.KEY_ABOUT)
            .summary = requireContext().getVersionSummary()

        requirePreference<NightModeDropDownPreference>(OtherPreferences.KEY_NIGHT_MODE_TOGGLE)
            .onNightModeSelected = { rect ->
            val decorView = requireActivity().window.decorView
            mainViewModel.setCircularReveal(decorView, rect)
        }

        preferenceIgnoreBatteryOptimize =
            requirePreference<SwitchPreferenceCompat>(OtherPreferences.KEY_IGNORE_BATTERY_OPTIMIZE).apply {
                onPreferenceChangeListener<Boolean> { _, ignoreBatteryOptimization ->
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
                    return@onPreferenceChangeListener true
                }
            }

        requirePreference<Preference>(OtherPreferences.KEY_RATE)
            .onPreferenceClickListener {
                requireContext().market(requireContext().packageName)
            }
        requirePreference<Preference>(OtherPreferences.KEY_SHARE)
            .onPreferenceClickListener {
                val appName = getString(R.string.app_name)
                val url = if (Logic.isSimplifiedChinese(requireContext()))
                    getString(R.string.url_cool_apk) else getString(R.string.url_play_store)
                requireContext().share(getString(R.string.share_text, appName, url))
            }
        requirePreference<Preference>(OtherPreferences.KEY_FEEDBACK)
            .onPreferenceClickListener {
                requireContext().emailTo(R.string.email)
            }
    }

    private fun checkIgnoreBatteryOptimize() {
        preferenceIgnoreBatteryOptimize.isChecked = requireContext().isIgnoringBatteryOptimizations
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            OtherPreferences.KEY_NIGHT_MODE_TOGGLE -> {
                // Wait for Popup to dismiss
                uiHandler.postDelayed(300) {
                    setNightMode(OtherPreferences.nightMode)
                }
            }
        }
    }

}