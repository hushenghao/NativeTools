package com.dede.nativetools.ui

import android.animation.Animator
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
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.ViewCompat
import com.dede.nativetools.R
import kotlin.math.abs
import kotlin.math.max

class LogoImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    AppCompatImageView(context, attrs) {

    companion object {
        private const val TAG = "LogoImageView"

        private const val TAG_ID: Int = R.id.iv_logo
        private const val TAG_FOLLOW_ID: Int = R.id.iv_logo_1
        private const val RESUME_ANIMATOR_DURATION: Long = 700L
        private const val FOLLOW_ANIMATOR_DURATION: Long = 80L
        private const val FOLLOW_ANIMATOR_START_DELAY: Long = FOLLOW_ANIMATOR_DURATION - 10L
    }

    override fun layout(l: Int, t: Int, r: Int, b: Int) {
        super.layout(l, t, r, b)
        layoutPoint.set(this.x, this.y)
    }

    private val eventPoint = PointF()
    private val downPoint = PointF()
    private val upPoint = PointF()
    private val layoutPoint = PointF()
    private val slop = ViewConfiguration.get(context).scaledTouchSlop
    private var moved = false

    var followViews: Array<View>? = null
    var enableFeedback = true
        set(value) {
            field = value
            isHapticFeedbackEnabled = value
            isSoundEffectsEnabled = value
        }
    private val hasFollowViews: Boolean get() = followViews?.isNotEmpty() ?: false

    private fun filterMove(event: MotionEvent): Boolean {
        return max(abs(event.rawX - downPoint.x), abs(event.rawY - downPoint.y)) > slop
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (inAnimator) {
            return super.onTouchEvent(event)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                eventPoint.set(event.x, event.y)
                downPoint.set(event.rawX, event.rawY)
                cleanAnimator()
                playSoundEffect(SoundEffectConstants.CLICK)
                moved = false
                super.onTouchEvent(event)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - eventPoint.x
                val dy = event.y - eventPoint.y
                if (!moved && filterMove(event)) {
                    moved = true
                    // fix background
                    isPressed = false
                }
                this.x += dx
                this.y += dy
                performHapticFeedback(
                    HapticFeedbackConstants.CLOCK_TICK,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
                return true
            }
            MotionEvent.ACTION_UP -> {
                upPoint.set(this.x, this.y)
                startUpAnimator(upPoint, layoutPoint)
                playSoundEffect(SoundEffectConstants.CLICK)
                if (moved) {
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
        if (!hasFollowViews) return

        val items = followViews.map {
            it.visibility = VISIBLE
            createAnimator(it, propertyName, start, end)
        }
        val oldAnimator = getTag(TAG_FOLLOW_ID) as? Animator
        val animator = AnimatorSet().apply {
            playSequentially(items)
            interpolator = LinearInterpolator()
            duration = FOLLOW_ANIMATOR_DURATION
            startDelay = FOLLOW_ANIMATOR_START_DELAY
            start()
            if (oldAnimator != null) {
                doOnStart { oldAnimator.cancel() }
            }
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
        if (followViews != null && hasFollowViews) {
            animator.doOnEnd {
                postDelayed({
                    followViews.forEach { it.visibility = INVISIBLE }
                }, FOLLOW_ANIMATOR_DURATION * followViews.size)
            }
        }
        setTag(TAG_ID, animator)
    }

    override fun onDetachedFromWindow() {
        cleanAnimator()
        super.onDetachedFromWindow()
    }

}