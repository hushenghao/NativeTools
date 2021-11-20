package com.dede.nativetools.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.dede.nativetools.R
import com.dede.nativetools.databinding.LayoutFooterPreferenceBinding

class FooterPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : Preference(context, attrs, defStyleAttr) {

    init {
        layoutResource = R.layout.layout_footer_preference
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        val binding = LayoutFooterPreferenceBinding.bind(holder.itemView)
        binding.ivLogo.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                binding.ivLogo.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            }

            override fun onAnimationEnd(animation: Animator?) {
                binding.ivLogo.removeAnimatorListener(this)
                binding.ivLogo.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                parent?.removePreference(this@FooterPreference)
            }
        })
//        binding.ivLogo.setOnClickListener {
//            binding.ivLogo.playAnimation()
//        }
    }

}