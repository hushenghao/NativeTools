package com.dede.nativetools.main

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.dede.nativetools.R
import com.dede.nativetools.netspeed.NetSpeedPreferences
import com.dede.nativetools.util.alert
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class GuideDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return requireContext().alert(R.string.label_privacy_agreement, R.string.label_privacy_agreement) {
            show = false
            negativeButton(android.R.string.cancel) {
                requireActivity().finish()
            }
            positiveButton(android.R.string.ok) {
                Firebase.analytics.setAnalyticsCollectionEnabled(true)
                NetSpeedPreferences.privacyAgreed = true
            }
        }
    }
}