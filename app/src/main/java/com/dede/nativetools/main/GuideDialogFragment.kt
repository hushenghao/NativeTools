package com.dede.nativetools.main

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.dede.nativetools.R
import com.dede.nativetools.netspeed.NetSpeedPreferences
import com.dede.nativetools.util.alert
import com.dede.nativetools.util.browse
import com.dede.nativetools.util.color
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class GuideDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        return requireContext().alert(R.string.label_privacy_agreement) {
            show = false
            val privacyAgreement = getString(R.string.label_privacy_agreement)
            val string = getString(R.string.alert_msg_guide,
                getString(R.string.app_name),
                privacyAgreement,
                privacyAgreement)
            val span = SpannableString(string)
            var indexOf = span.lastIndexOf(privacyAgreement)
            applySpan(span, indexOf, indexOf + privacyAgreement.length)
            indexOf = span.indexOf(privacyAgreement)
            applySpan(span, indexOf, indexOf + privacyAgreement.length)

            setMessage(span)
            negativeButton(android.R.string.cancel) {
                requireActivity().finish()
            }
            positiveButton(android.R.string.ok) {
                Firebase.analytics.setAnalyticsCollectionEnabled(true)
                NetSpeedPreferences.privacyAgreed = true
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val messageView = requireDialog().findViewById<TextView>(android.R.id.message)
        messageView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun applySpan(span: SpannableString, start: Int, end: Int) {
        span.setSpan(object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                ds.color = requireContext().color(
                    com.google.android.material.R.attr.colorPrimary,
                    Color.BLUE)
                ds.isUnderlineText = true
            }

            override fun onClick(widget: View) {
                requireContext().browse(R.string.url_privacy_agreement)
            }
        },
            start,
            end,
            SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
        span.setSpan(StyleSpan(Typeface.BOLD),
            start,
            end,
            SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)

    }

}