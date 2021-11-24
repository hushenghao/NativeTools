package com.dede.nativetools.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.appcompat.widget.AppCompatImageView
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
            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                if (isReverse) {
                    parent?.removePreference(this@FooterPreference)
                }
            }
        })
    }

    class AndroidView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
    ) : AppCompatImageView(context, attrs), Runnable {

        companion object {
            private const val ANIMATOR_DURATION = 600L
            private const val ANIMATOR_DELAY = 1500L
        }

        init {
            setImageResource(R.drawable.ic_logo_android)
        }

        private val listener = object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                val androidView = this@AndroidView
                if (isReverse) {
                    androidView.performHapticFeedback(
                        HapticFeedbackConstants.CONTEXT_CLICK,
                        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                    )
                    return
                }
                postDelayed(androidView, ANIMATOR_DELAY)
            }
        }

        private val animator: ObjectAnimator =
            ObjectAnimator.ofFloat(this, "translationY", height.toFloat(), 0f)
                .apply {
                    addListener(listener)
                    interpolator = OvershootInterpolator(1.6f)
                    duration = ANIMATOR_DURATION
                }

        fun addAnimatorListener(listener: AnimatorListenerAdapter) {
            animator.addListener(listener)
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            if (!isInEditMode && height > 0) {
                animator.start()
            }
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            animator.setFloatValues(height.toFloat(), 0f)
        }

        override fun onVisibilityChanged(changedView: View, visibility: Int) {
            super.onVisibilityChanged(changedView, visibility)
            if (isShown) {
                animator.start()
            } else {
                removeCallbacks(this)
                animator.cancel()
            }
        }

        override fun run() {
            animator.reverse()
        }

        override fun onDetachedFromWindow() {
            removeCallbacks(this)
            animator.cancel()
            super.onDetachedFromWindow()
        }
    }

}