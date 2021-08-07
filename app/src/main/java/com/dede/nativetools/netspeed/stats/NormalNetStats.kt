package com.dede.nativetools.netspeed.stats

import android.net.TrafficStats

class NormalNetStats : INetStats {

    override fun supported(): Boolean {
        return true
    }

    override fun getRxBytes(): Long {
        return TrafficStats.getTotalRxBytes()
    }

    override fun getTxBytes(): Long {
        return TrafficStats.getTotalTxBytes()
    }

}