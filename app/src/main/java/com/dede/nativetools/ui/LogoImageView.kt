package com.dede.nativetools.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.*
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.annotation.Keep
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import com.dede.nativetools.R
import kotlin.math.abs
import kotlin.math.max

class LogoImageView(context: Context, attrs: AttributeSet) :
    AppCompatImageView(context, attrs) {

    companion object {
        private const val TAG = "LogoImageView"

        private const val TAG_ID: Int = R.id.iv_logo
        private const val TAG_FOLLOW_ID: Int = R.id.iv_logo_1
        private const val RESUME_ANIMATOR_DURATION: Long = 500L
        private const val FOLLOW_ANIMATOR_DURATION: Long = 50L
        private const val FOLLOW_VIEWS_HIDE_DELAY: Long = 200L
    }

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

    var followViews: Array<View>? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (inAnimator) {
            return super.onTouchEvent(event)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                eventPoint.set(event.x, event.y)
                cleanAnimator()
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
                    startUpAnimator(PointF(this.x, this.y), downPoint)
                    playSoundEffect(SoundEffectConstants.CLICK)
                    isPressed = false
                    return false
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun startFollowAnimator(propertyName: String, start: Float, end: Float) {
        if (!ViewCompat.isAttachedToWindow(this)) {
            return
        }
        val followViews = this.followViews ?: return
        val c = followViews.size
        if (c <= 0) return

        val items = followViews.map {
            it.visibility = VISIBLE
            createAnimator(it, propertyName, start, end)
        }
        val oldAnimator = getTag(TAG_FOLLOW_ID) as? Animator
        val animator = AnimatorSet().apply {
            playSequentially(items)
            interpolator = LinearInterpolator()
            duration = FOLLOW_ANIMATOR_DURATION
            startDelay = FOLLOW_ANIMATOR_DURATION
            start()
        }
        if (oldAnimator != null) {
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    oldAnimator.cancel()
                }
            })
        }
        setTag(TAG_FOLLOW_ID, animator)
    }

    private fun createAnimator(
        target: View,
        propertyName: String,
        start: Float,
        end: Float
    ): Animator {
        return ObjectAnimator.ofFloat(target, propertyName, start, end)
    }

    private fun cleanAnimator() {
        (getTag(TAG_ID) as? Animator)?.cancel()
        (getTag(TAG_FOLLOW_ID) as? Animator)?.cancel()
    }

    @Keep
    override fun setX(x: Float) {
        startFollowAnimator("x", this.x, x)
        super.setX(x)
    }

    @Keep
    override fun setY(y: Float) {
        startFollowAnimator("y", this.y, y)
        super.setY(y)
    }

    private val inAnimator: Boolean get() = (getTag(TAG_ID) as? Animator)?.isRunning ?: false

    private fun startUpAnimator(start: PointF, end: PointF) {
        cleanAnimator()
        val xOfFloat = createAnimator(this, "x", start.x, end.x)
        val yOfFloat = createAnimator(this, "y", start.y, end.y)
        val animator = AnimatorSet().apply {
            playTogether(xOfFloat, yOfFloat)
            duration = RESUME_ANIMATOR_DURATION
            interpolator = OvershootInterpolator(1.9f)
            start()
        }
        val followViews = this.followViews
        if (followViews != null) {
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animator: Animator?) {
                    followViews.forEach { it.visibility = VISIBLE }
                }

                override fun onAnimationEnd(animator: Animator?) {
                    postDelayed({
                        followViews.forEach { it.visibility = INVISIBLE }
                    }, FOLLOW_VIEWS_HIDE_DELAY)
                }
            })
        }
        setTag(TAG_ID, animator)
    }

    override fun onDetachedFromWindow() {
        cleanAnimator()
        super.onDetachedFromWindow()
    }

}