package com.dede.nativetools.beta

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.dede.nativetools.R

class BetaFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preference_beta)
    }
}