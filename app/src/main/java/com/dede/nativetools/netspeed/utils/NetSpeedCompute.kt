package com.dede.nativetools.netspeed.utils

import com.dede.nativetools.netspeed.NetSpeedPreferences
import com.dede.nativetools.netspeed.stats.NetStats
import com.dede.nativetools.util.HandlerTicker
import kotlin.math.max
import kotlin.math.roundToLong
import kotlin.properties.Delegates

typealias OnTickNetSpeed = (Long, Long) -> Unit

/**
 * 网速计算
 */
class NetSpeedCompute(private var onTick: OnTickNetSpeed? = null) {

    private var rxBytes: Long = 0L
        set(value) {
            rxSpeed = env(value, field, interval)
            field = value
        }
    private var txBytes: Long = 0L
        set(value) {
            txSpeed = env(value, field, interval)
            field = value
        }
    private val netStats: NetStats = NetStats.getInstance()

    var interval: Int by Delegates.observable(NetSpeedPreferences.DEFAULT_INTERVAL) { _, old, new ->
        if (old != new) {
            refresh()
            handlerTicker.interval = new.toLong()
        }
    }

    private fun env(n: Long, o: Long, interval: Int): Long {
        return max((1000.0 / interval * (n - o)).roundToLong(), 0L)
    }

    private val handlerTicker: HandlerTicker = HandlerTicker(interval.toLong()) {
        synchronized(this) {
            refresh()
            onTick?.invoke(rxSpeed, txSpeed)
        }
    }

    var rxSpeed: Long = 0L
        private set
    var txSpeed: Long = 0L
        private set

    private fun refresh() {
        rxBytes = netStats.getRxBytes()
        txBytes = netStats.getTxBytes()
    }

    fun start() {
        refresh()
        handlerTicker.start()
    }

    fun stop() {
        handlerTicker.stop()
    }

    fun destroy() {
        handlerTicker.destroy()
    }

}