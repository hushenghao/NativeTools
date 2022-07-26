@file:JvmName("AnimatorKt")

package com.dede.nativetools.util

import android.animation.ObjectAnimator
import android.util.Property
import android.view.View
import androidx.core.animation.addListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

fun LifecycleOwner.lifecycleAnimator(
    target: View,
    property: Property<View, Float>,
    vararg values: Float,
    block: ObjectAnimator.() -> Unit
) {
    val lifecycle = this.lifecycle
    if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
        return
    }
    val animator = ObjectAnimator.ofFloat(target, property, *values)
    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            animator.removeAllListeners()
            animator.cancel()
        }
    }
    lifecycle.addObserver(observer)
    animator.addListener(onEnd = { lifecycle.removeObserver(observer) })
    block.invoke(animator)
}

class XProperty : Property<View, Float>(Float::class.java, "x") {
    override fun set(view: View, value: Float) {
        view.x = value
    }

    override fun get(view: View): Float {
        return view.x
    }
}

class YProperty : Property<View, Float>(Float::class.java, "y") {
    override fun set(view: View, value: Float) {
        view.y = value
    }

    override fun get(view: View): Float {
        return view.y
    }
}

class ScaleProperty : Property<View, Float>(Float::class.java, "scale") {
    override fun get(view: View): Float {
        return view.scaleX
    }

    override fun set(view: View, value: Float) {
        view.scaleX = value
        view.scaleY = value
    }
}
