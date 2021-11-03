package com.dede.nativetools.netspeed

import com.dede.nativetools.netspeed.stats.INetStats
import com.dede.nativetools.util.HandlerTick
import kotlin.math.max
import kotlin.math.roundToLong
import kotlin.properties.Delegates

typealias OnTickNetSpeed = (Long, Long) -> Unit

/**
 * 网速计算
 */
class NetSpeedCompute(private var onTick: OnTickNetSpeed? = null) {

    private var rxBytes: Long = 0L
    private var txBytes: Long = 0L
    private val netStats: INetStats = INetStats.getInstance()

    var interval: Int by Delegates.observable(NetSpeedPreferences.DEFAULT_INTERVAL) { _, old, new ->
        if (old != new) {
            reset()
            handlerTick.interval = new.toLong()
        }
    }

    private fun env(a: Long, b: Long, ms: Int): Long {
        return max((ms / 1000.0 * (a - b)).roundToLong(), 0L)
    }

    private val handlerTick: HandlerTick = HandlerTick(interval.toLong()) {
        synchronized(this) {
//            this.rxBytes = netStats.getRxBytes().also {
//                this.rxSpeed = env(it, this.rxBytes, interval)
//            }
//            this.txBytes = netStats.getTxBytes().also {
//                this.txSpeed = env(it, this.txBytes, interval)
//            }
            val rxBytes = netStats.getRxBytes()
            val txBytes = netStats.getTxBytes()
            this.rxSpeed = env(rxBytes, this.rxBytes, interval)
            this.txSpeed = env(txBytes, this.txBytes, interval)
            this.rxBytes = rxBytes
            this.txBytes = txBytes

            onTick?.invoke(this.rxSpeed, this.txSpeed)
        }
    }

    var rxSpeed: Long = 0L
        private set
    var txSpeed: Long = 0L
        private set

    private fun reset() {
        rxBytes = netStats.getRxBytes()
        txBytes = netStats.getTxBytes()
    }

    fun start() {
        reset()
        handlerTick.start()
    }

    fun stop() {
        handlerTick.stop()
    }

}