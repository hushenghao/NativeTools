package com.dede.nativetools.ui

import android.animation.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Property
import android.view.*
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.animation.doOnEnd
import androidx.core.view.ViewCompat
import androidx.core.view.allViews
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.dede.nativetools.R
import com.dede.nativetools.util.XProperty
import com.dede.nativetools.util.YProperty
import kotlin.math.abs
import kotlin.math.max

/** Egg LogoImageView */
class LogoImageView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AppCompatImageView(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "LogoImageView"

        private const val TAG_ID: Int = R.id.iv_logo
        private const val RESUME_ANIMATOR_DURATION: Long = 600L
        private const val FOLLOW_ANIMATOR_DURATION: Long = 70L
        private const val FOLLOW_ANIMATOR_START_DELAY: Long = FOLLOW_ANIMATOR_DURATION - 10L

        private val LINEAR_INTERPOLATOR = LinearInterpolator()
    }

    private val eventPoint = PointF()
    private val downPoint = PointF()
    private val upPoint = PointF()
    private val layoutPoint = PointF()
    private val slop = ViewConfiguration.get(context).scaledTouchSlop
    private var moved = false

    private val xProperty = XProperty()
    private val yProperty = YProperty()

    var followViews: Array<View>? = null
        set(value) {
            field = value
            prepareFollowViews(field)
        }

    private fun prepareFollowViews(followViews: Array<View>?) {
        if (isInLayout) {
            return
        }
        if (followViews == null || followViews.isEmpty()) {
            return
        }
        val size = followViews.size
        for (i in (0 until size)) {
            val view = followViews[i]
            view.isInvisible = true
            val elevation =
                elevationEvaluator.evaluate((i + 1f) / size, maxElevation - 1, maxElevation - 2)
            ViewCompat.setElevation(view, elevation)
        }
    }

    var enableFeedback = true
        set(value) {
            field = value
            isHapticFeedbackEnabled = value
            isSoundEffectsEnabled = value
        }
    var dragEnable = true
    private val hasFollowView: Boolean
        get() = followViews?.isNotEmpty() ?: false

    private var savedElevation: Float = 0f
    private var maxElevation: Float = 0f
    private val elevationEvaluator = FloatEvaluator()

    private fun filterMove(event: MotionEvent): Boolean {
        return max(abs(event.rawX - downPoint.x), abs(event.rawY - downPoint.y)) > slop
    }

    override fun layout(l: Int, t: Int, r: Int, b: Int) {
        super.layout(l, t, r, b)
        layoutPoint.set(this.x, this.y)
        savedElevation = ViewCompat.getElevation(this)
        maxElevation =
            max((parent as ViewGroup).allViews.maxOf(ViewCompat::getElevation) + 3, savedElevation)
        prepareFollowViews(followViews)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (inUpAnimator || !dragEnable) {
            return super.onTouchEvent(event)
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                eventPoint.set(event.x, event.y)
                downPoint.set(event.rawX, event.rawY)
                cleanUpAnimator()
                moved = false
                // super.onTouchEvent(event)
                ViewCompat.setElevation(this, maxElevation)
                isPressed = true
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
                if (moved) {
                    this.x += dx
                    this.y += dy
                    performHapticFeedback(
                        HapticFeedbackConstants.CLOCK_TICK,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                    )
                    return true
                }
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                if (moved) {
                    upPoint.set(this.x, this.y)
                    startUpAnimator(upPoint, layoutPoint)
                    moved = false
                    playSoundEffect(SoundEffectConstants.CLICK)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun startFollowAnimator(property: Property<View, Float>, start: Float, end: Float) {
        if (!ViewCompat.isAttachedToWindow(this)) {
            return
        }
        if (!hasFollowView) return
        val followViews = this.followViews ?: return

        val items =
            followViews.map {
                it.isVisible = true
                createAnimator(it, property, start, end)
            }
        AnimatorSet().apply {
            playSequentially(items)
            interpolator = LINEAR_INTERPOLATOR
            duration = FOLLOW_ANIMATOR_DURATION
            startDelay = FOLLOW_ANIMATOR_START_DELAY
            start()
        }
    }

    private fun createAnimator(
        target: View,
        property: Property<View, Float>,
        start: Float,
        end: Float
    ): ValueAnimator {
        return ObjectAnimator.ofFloat(target, property, start, end)
    }

    private fun cleanUpAnimator() {
        (getTag(TAG_ID) as? Animator)?.cancel()
        removeCallbacks(upAnimatorEndDelayCallback)
    }

    override fun setX(x: Float) {
        startFollowAnimator(xProperty, this.x, x)
        super.setX(x)
    }

    override fun setY(y: Float) {
        startFollowAnimator(yProperty, this.y, y)
        super.setY(y)
    }

    private val inUpAnimator: Boolean
        get() = (getTag(TAG_ID) as? Animator)?.isRunning ?: false

    private val upAnimatorEndDelayCallback = Runnable {
        this.followViews?.forEach { it.isInvisible = true }
        // resume elevation
        ViewCompat.setElevation(this, savedElevation)
    }

    private fun startUpAnimator(start: PointF, end: PointF) {
        val xOfFloat = createAnimator(this, xProperty, start.x, end.x)
        val yOfFloat = createAnimator(this, yProperty, start.y, end.y)
        val animator =
            AnimatorSet().apply {
                playTogether(xOfFloat, yOfFloat)
                duration = RESUME_ANIMATOR_DURATION
                interpolator = OvershootInterpolator(1.6f)
                start()
            }
        val followViews = this.followViews
        if (followViews != null && hasFollowView) {
            animator.doOnEnd {
                postDelayed(upAnimatorEndDelayCallback, FOLLOW_ANIMATOR_DURATION * followViews.size)
            }
        }
        setTag(TAG_ID, animator)
    }

    override fun onDetachedFromWindow() {
        cleanUpAnimator()
        super.onDetachedFromWindow()
    }
}
