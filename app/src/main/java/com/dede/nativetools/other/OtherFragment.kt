package com.dede.nativetools.other

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.dede.nativetools.R
import com.dede.nativetools.main.MainViewModel
import com.dede.nativetools.main.applyBottomBarsInsets
import com.dede.nativetools.ui.NightModeDropDownPreference
import com.dede.nativetools.util.*
import com.google.firebase.analytics.FirebaseAnalytics

class OtherFragment : PreferenceFragmentCompat() {

    private val mainViewModel by activityViewModels<MainViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (UI.isWideSize()) {
            applyBottomBarsInsets(listView)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = DataStorePreference(requireContext())
        addPreferencesFromResource(R.xml.preference_other)
        initOtherPreferenceGroup()
    }

    private fun initOtherPreferenceGroup() {
        requirePreference<Preference>(OtherPreferences.KEY_ABOUT).summary =
            requireContext().getVersionSummary()

        requirePreference<NightModeDropDownPreference>(OtherPreferences.KEY_NIGHT_MODE_TOGGLE).let {
            it.onPreferenceChangeListener<String> { _, mode ->
                val decorView = requireActivity().window.decorView
                mainViewModel.setCircularReveal(decorView, it.pressedPoint)

                setNightMode(mode.toInt())
                return@onPreferenceChangeListener true
            }
        }

        requirePreference<Preference>(OtherPreferences.KEY_RATE).onPreferenceClickListener {
            requireContext().market(requireContext().packageName)
            event(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_NAME, "去评分")
            }
        }
        requirePreference<Preference>(OtherPreferences.KEY_SHARE).onPreferenceClickListener {
            Logic.shareApp(requireContext())
        }
    }

}
