package com.dede.nativetools.other

import android.annotation.SuppressLint
import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.dede.nativetools.R
import com.dede.nativetools.netspeed.NetSpeedPreferences
import com.dede.nativetools.util.*

class OtherFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val KEY_ABOUT = "about"
        private const val KEY_IGNORE_BATTERY_OPTIMIZE = "ignore_battery_optimize"
    }

    private val activityResultLauncherCompat =
        ActivityResultLauncherCompat(this, ActivityResultContracts.StartActivityForResult())

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.other_preference)
        initOtherPreferenceGroup()
    }

    private fun initOtherPreferenceGroup() {
        requirePreference<Preference>(KEY_ABOUT).also {
            it.summary = requireContext().getVersionSummary()

            it.onPreferenceClickListener {
                findNavController().navigate(R.id.about)
            }
        }

        requirePreference<Preference>(KEY_IGNORE_BATTERY_OPTIMIZE).also {
            val context = requireContext()
            val packageName = context.packageName

            fun setVisible() {
                val powerManager = context.requireSystemService<PowerManager>()
                it.isVisible = !powerManager.isIgnoringBatteryOptimizations(packageName)
            }

            setVisible()

            if (!it.isVisible) return@also

            it.onPreferenceClickListener {
                @SuppressLint("BatteryLife")
                val intent = Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, "package:$packageName"
                )
                activityResultLauncherCompat.launch(intent) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        setVisible()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        globalPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        globalPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            NetSpeedPreferences.KEY_NIGHT_MODE_TOGGLE -> {
                setNightMode(NetSpeedPreferences.isNightMode)
            }
        }
    }

}