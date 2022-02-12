package com.dede.nativetools.netspeed.stats

import android.net.TrafficStats

class NormalNetStats : NetStats {

    override val name: String
        get() = "NormalNetStats"

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