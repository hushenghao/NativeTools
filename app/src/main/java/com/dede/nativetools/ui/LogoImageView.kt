package com.dede.nativetools.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.SoundEffectConstants
import android.view.ViewConfiguration
import android.view.animation.OvershootInterpolator
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.abs
import kotlin.math.max

class LogoImageView(context: Context, attrs: AttributeSet) :
    AppCompatImageView(context, attrs) {

    init {
        isHapticFeedbackEnabled = true
        isSoundEffectsEnabled = true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        downPoint.set(this.x, this.y)
    }

    private val eventPoint = PointF()
    private val downPoint = PointF()
    private val slop = ViewConfiguration.get(context).scaledTouchSlop
    private var moved = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (inAnim) {
            return super.onTouchEvent(event)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                eventPoint.set(event.x, event.y)
                cleanAnim()
                playSoundEffect(SoundEffectConstants.CLICK)
                moved = false
                super.onTouchEvent(event)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - eventPoint.x
                val dy = event.y - eventPoint.y
                if (!moved && max(abs(dx), abs(dy)) > slop) {
                    moved = true
                }
                this.x += dx
                this.y += dy
                performHapticFeedback(
                    HapticFeedbackConstants.VIRTUAL_KEY,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
                return true
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                if (moved) {
                    startAnim(PointF(this.x, this.y), downPoint)
                    playSoundEffect(SoundEffectConstants.CLICK)
                    isPressed = false
                    return false
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private var animatorSet: AnimatorSet? = null

    private fun cleanAnim() {
        animatorSet?.cancel()
    }

    private val inAnim: Boolean get() = animatorSet?.isRunning ?: false

    private fun startAnim(start: PointF, end: PointF) {
        cleanAnim()
        val x = ObjectAnimator.ofFloat(this, "x", start.x, end.x)
        val y = ObjectAnimator.ofFloat(this, "y", start.y, end.y)
        animatorSet = AnimatorSet().apply {
            duration = 300L
            interpolator = OvershootInterpolator(1.8f)
            playTogether(x, y)
            start()
        }
    }

}