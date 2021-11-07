package com.dede.nativetools.util

import android.os.Handler
import android.os.Looper
import android.os.SystemClock


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
        val lastTickStart = SystemClock.elapsedRealtime()
        onTick()

        // take into account user's onTick taking time to execute
        val lastTickDuration = SystemClock.elapsedRealtime() - lastTickStart
        var delay = interval - lastTickDuration

        // special case: user's onTick took more than interval to
        // complete, skip to next interval
        while (delay < 0) delay += interval

        handler.postDelayed(this, delay)
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
