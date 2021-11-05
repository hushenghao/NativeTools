package com.dede.nativetools.util

import android.os.Handler
import android.os.Looper

/**
 * 定时回调工具
 *
 * @author hsh
 * @since 2021/5/31 1:59 下午
 */
class HandlerTicker(interval: Long, private val onTick: () -> Unit) : Runnable {

    var interval: Long = interval
        set(value) {
            field = value
            handler.removeCallbacks(this)
            handler.post(this)
        }

    private val handler = Handler(Looper.getMainLooper())

    override fun run() {
        handler.postDelayed(this, this.interval)
        onTick.invoke()
    }

    fun start(first: Boolean = true) {
        handler.removeCallbacks(this)
        if (first) {
            handler.post(this)
        } else {
            handler.postDelayed(this, this.interval)
        }
    }

    fun stop() {
        handler.removeCallbacks(this)
    }

}
