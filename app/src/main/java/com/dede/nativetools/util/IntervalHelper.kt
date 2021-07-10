package com.dede.nativetools.util

import android.os.Handler
import android.os.Looper

/**
 * 定时回调工具
 *
 * @author hsh
 * @since 2021/5/31 1:59 下午
 */
class IntervalHelper(
    private var interval: Long,
    private val onTick: Function0<Unit>
) {

    private val handler = Handler(Looper.getMainLooper())

    private val tickRunnable = object : Runnable {
        override fun run() {
            handler.postDelayed(this, interval)
            onTick.invoke()
        }
    }

    fun setInterval(interval: Long) {
        this.interval = interval
        start()
    }

    fun start(first: Boolean = true) {
        handler.removeCallbacks(tickRunnable)
        if (first) {
            handler.post(tickRunnable)
        } else {
            handler.postDelayed(tickRunnable, interval)
        }
    }

    fun stop() {
        handler.removeCallbacks(tickRunnable)
    }

}
