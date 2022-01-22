package com.dede.nativetools.ui

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.dede.nativetools.R

class FooterPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : Preference(context, attrs, defStyleAttr) {

    init {
        layoutResource = R.layout.layout_footer_preference
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
    }
}

class AndroidView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    companion object {
        private const val ANIMATOR_DURATION = 600L
    }

    init {
        setImageResource(R.drawable.ic_logo_android)
    }

    private val animator: ObjectAnimator =
        ObjectAnimator.ofFloat(this, "translationY", height.toFloat(), 0f)
            .apply {
                interpolator = OvershootInterpolator(1.6f)
                duration = ANIMATOR_DURATION
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
            animator.cancel()
        }
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }
}