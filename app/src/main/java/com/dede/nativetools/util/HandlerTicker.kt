package com.dede.nativetools.util

import android.os.*

typealias OnTick = () -> Unit

/**
 * 定时回调工具
 *
 * @author hsh
 * @since 2021/5/31 1:59 下午
 */
class HandlerTicker(
    interval: Long,
    private val handler: Handler = uiHandler,
    onTick: OnTick
) : Runnable {

    var interval: Long = interval
        set(value) {
            field = value
            workHandler.singlePost(this)
        }

    private val workHandler: Handler

    init {
        val handlerThread = HandlerThread("Ticker")
        handlerThread.start()
        workHandler = Handler(handlerThread.looper)
    }

    private val onTickRunnable = Runnable(onTick)

    override fun run() {
        val lastTickStart = SystemClock.elapsedRealtime()

        handler.singlePost(onTickRunnable)

        // take into account user's onTick taking time to execute
        val lastTickDuration = SystemClock.elapsedRealtime() - lastTickStart
        var delay = interval - lastTickDuration

        // special case: user's onTick took more than interval to
        // complete, skip to next interval
        while (delay < 0) delay += interval

        workHandler.postDelayed(this, delay)
    }

    fun start() {
        workHandler.singlePost(this)
    }

    fun stop() {
        workHandler.removeCallbacks(this)
    }

    fun destroy() {
        workHandler.removeCallbacks(this)
        workHandler.looper.quitSafely()
    }

}
