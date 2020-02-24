package com.dede.nativetools.ui.main

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.dede.nativetools.R

class MainFragment : PreferenceFragmentCompat() {

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.main_preference)
    }

}
