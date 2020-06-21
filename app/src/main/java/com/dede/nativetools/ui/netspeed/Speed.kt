package com.dede.nativetools.ui.netspeed

import android.net.TrafficStats

class Speed {

    var interval: Int = 1000

    private var rxBytes: Long = TrafficStats.getTotalRxBytes()
    private var txBytes: Long = TrafficStats.getTotalTxBytes()

    fun reset() {
        rxBytes = TrafficStats.getTotalRxBytes()
        txBytes = TrafficStats.getTotalTxBytes()
    }

    fun getRxSpeed(): Long {
        val rxBytes = TrafficStats.getTotalRxBytes()
        val rxSpeed = ((rxBytes - this.rxBytes) * 1f / interval * 1000 + .5).toLong()

        this.rxBytes = rxBytes
        return rxSpeed
    }

    fun getTxSpeed(): Long {
        val txBytes = TrafficStats.getTotalTxBytes()
        val txSpeed = ((txBytes - this.txBytes) * 1f / interval * 1000 + .5).toLong()

        this.txBytes = txBytes
        return txSpeed
    }
}