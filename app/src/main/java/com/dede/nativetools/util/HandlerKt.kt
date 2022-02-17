@file:JvmName("HandlerKt")

package com.dede.nativetools.util

import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.core.os.ExecutorCompat
import androidx.core.os.HandlerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

val uiHandler by lazy { Handler(Looper.getMainLooper()) }

fun Handler.singlePost(r: Runnable, delayMillis: Long = 0) {
    if (HandlerCompat.hasCallbacks(this, r)) {
        this.removeCallbacks(r)
    }
    HandlerCompat.postDelayed(this, r, null, delayMillis)
}

val uiExecutor by lazy { ExecutorCompat.create(uiHandler) }

typealias HandlerMessage = Message.() -> Unit

interface HandlerCallback : Handler.Callback {
    override fun handleMessage(msg: Message): Boolean {
        onHandleMessage(msg)
        return true
    }

    fun onHandleMessage(msg: Message)
}

class LifecycleHandler(
    looper: Looper,
    lifecycleOwner: LifecycleOwner,
    handlerMessage: HandlerMessage,
) : Handler(looper), DefaultLifecycleObserver {

    private val holder = HandlerHolder(handlerMessage)

    private class HandlerHolder(handlerMessage: HandlerMessage) {
        var handlerMessage: HandlerMessage? = handlerMessage
            private set

        fun clear() {
            handlerMessage = null
        }
    }

    init {
        val lifecycle = lifecycleOwner.lifecycle
        if (lifecycle.currentState != Lifecycle.State.DESTROYED) {
            lifecycle.addObserver(this)
        } else {
            holder.clear()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        removeCallbacksAndMessages(null)
        holder.clear()
    }

    override fun handleMessage(msg: Message) {
        holder.handlerMessage?.invoke(msg)
    }
}
