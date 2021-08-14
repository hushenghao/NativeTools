package com.dede.nativetools.netspeed

import com.dede.nativetools.netspeed.stats.INetStats
import com.dede.nativetools.util.IntervalHelper
import kotlin.properties.Delegates

typealias NetSpeedChanged = (Long, Long) -> Unit

/**
 * 网速计算
 */
class NetSpeedHelper(private var netSpeedChanged: NetSpeedChanged? = null) {

    private var rxBytes: Long = 0L
    private var txBytes: Long = 0L
    private val netStats: INetStats = INetStats.getInstance()

    var interval: Int by Delegates.observable(NetSpeedPreferences.DEFAULT_INTERVAL) { _, old, new ->
        if (old != new) {
            reset()
            intervalHelper.setInterval(new.toLong())
        }
    }

    private val intervalHelper: IntervalHelper = IntervalHelper(interval.toLong()) {
        synchronized(this) {
            val rxBytes = netStats.getRxBytes()
            val txBytes = netStats.getTxBytes()
            val rxSpeed = (rxBytes - this.rxBytes).toDouble() / interval * 1000 + .5f
            val txSpeed = (txBytes - this.txBytes).toDouble() / interval * 1000 + .5f
            this.rxSpeed = rxSpeed.toLong()
            this.txSpeed = txSpeed.toLong()
            this.rxBytes = rxBytes
            this.txBytes = txBytes

            netSpeedChanged?.invoke(this.rxSpeed, this.txSpeed)
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

    fun resume() {
        reset()
        intervalHelper.start()
    }

    fun pause() {
        intervalHelper.stop()
    }

}