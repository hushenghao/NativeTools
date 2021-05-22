package com.dede.nativetools.netspeed

import android.net.TrafficStats
import android.os.Handler
import android.os.Looper
import java.lang.reflect.Method
import kotlin.properties.Delegates

typealias NetSpeedChanged = (Long, Long) -> Unit

class NetSpeed(private var netSpeedChanged: NetSpeedChanged? = null) : Runnable {

    private val handler = Handler(Looper.getMainLooper())

    override fun run() {
        synchronized(this) {
            handler.postDelayed(this, interval.toLong())

            val rxBytes = getRxBytes()
            val txBytes = getTxBytes()
            val rxSpeed = (rxBytes - this.rxBytes).toDouble() / interval * 1000 + .5f
            val txSpeed = (txBytes - this.txBytes).toDouble() / interval * 1000 + .5f
            this.rxSpeed = rxSpeed.toLong()
            this.txSpeed = txSpeed.toLong()
            this.rxBytes = rxBytes
            this.txBytes = txBytes

            netSpeedChanged?.invoke(this.rxSpeed, this.txSpeed)
        }
    }

    private var rxBytes: Long = 0L
    private var txBytes: Long = 0L

    var interval: Int by Delegates.observable(NetSpeedService.DEFAULT_INTERVAL) { _, old, new ->
        if (old != new) {
            reset()
        }
    }

    var rxSpeed: Long = 0L
        private set
    var txSpeed: Long = 0L
        private set

    private fun reset() {
        rxBytes = getRxBytes()
        txBytes = getTxBytes()
    }

    fun resume() {
        handler.removeCallbacks(this)
        reset()
        handler.post(this)
    }

    fun pause() {
        handler.removeCallbacks(this)
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