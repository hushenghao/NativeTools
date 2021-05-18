package com.dede.nativetools.netspeed

import android.net.TrafficStats
import java.lang.reflect.Method

class Speed {

    var interval: Int = 1000

    private var rxBytes: Long = getRxBytes()
    private var txBytes: Long = getTxBytes()

    fun reset() {
        rxBytes = getRxBytes()
        txBytes = getTxBytes()
    }

    fun getRxSpeed(): Long {
        val rxBytes = getRxBytes()
        val rxSpeed = ((rxBytes - this.rxBytes) * 1f / interval * 1000 + .5).toLong()

        this.rxBytes = rxBytes
        return rxSpeed
    }

    fun getTxSpeed(): Long {
        val txBytes = getTxBytes()
        val txSpeed = ((txBytes - this.txBytes) * 1f / interval * 1000 + .5).toLong()

        this.txBytes = txBytes
        return txSpeed
    }

    private var methodGetLoopbackRxBytes: Method? = null
    private var methodGetLoopbackTxBytes: Method? = null

    init {
        try {
            methodGetLoopbackRxBytes = TrafficStats::class.java.getMethod("getLoopbackRxBytes")
            methodGetLoopbackTxBytes = TrafficStats::class.java.getMethod("getLoopbackTxBytes")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getTxBytes(): Long {
        return getTotalTxBytes() - getLoopbackTxBytes()
    }

    private fun getRxBytes(): Long {
        return getTotalRxBytes() - getLoopbackRxBytes()
    }

    private fun getLoopbackRxBytes(): Long {
        if (methodGetLoopbackRxBytes == null) return 0L
        val result = try {
            methodGetLoopbackRxBytes!!.invoke(null)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        return (result as? Long) ?: 0L
    }

    private fun getLoopbackTxBytes(): Long {
        if (methodGetLoopbackTxBytes == null) return 0L
        val result = try {
            methodGetLoopbackTxBytes!!.invoke(null)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        return (result as? Long) ?: 0L
    }

    private fun getTotalRxBytes(): Long {
        return TrafficStats.getTotalRxBytes()
    }

    private fun getTotalTxBytes(): Long {
        return TrafficStats.getTotalTxBytes()
    }
}