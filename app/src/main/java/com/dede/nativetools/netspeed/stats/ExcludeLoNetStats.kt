package com.dede.nativetools.netspeed.stats

import android.net.TrafficStats
import com.dede.nativetools.netspeed.stats.NetStats.Companion.isSupported
import com.dede.nativetools.util.invokeWithReturn
import com.dede.nativetools.util.method
import java.lang.reflect.Method

class ExcludeLoNetStats : NetStats {

    override fun supported(): Boolean {
        return methodGetLoopbackRxBytes != null && methodGetLoopbackTxBytes != null
                && getLoopbackRxBytes().isSupported
    }

    override fun getRxBytes(): Long {
        return NetStats.addIfSupported(TrafficStats.getTotalRxBytes(), -getLoopbackRxBytes())
    }

    override fun getTxBytes(): Long {
        return NetStats.addIfSupported(TrafficStats.getTotalTxBytes(), -getLoopbackTxBytes())
    }

    private var methodGetLoopbackRxBytes: Method? = null
    private var methodGetLoopbackTxBytes: Method? = null

    init {
        try {
            methodGetLoopbackRxBytes = TrafficStats::class.java.method("getLoopbackRxBytes")
            methodGetLoopbackTxBytes = TrafficStats::class.java.method("getLoopbackTxBytes")
        } catch (e: Exception) {
        }
    }

    private fun getLoopbackRxBytes(): Long {
        return getLoopbackBytes(methodGetLoopbackRxBytes)
    }

    private fun getLoopbackTxBytes(): Long {
        return getLoopbackBytes(methodGetLoopbackTxBytes)
    }

    private fun getLoopbackBytes(method: Method?): Long {
        if (method == null) return NetStats.UNSUPPORTED
        return try {
            method.invokeWithReturn(null)
        } catch (e: Exception) {
            NetStats.UNSUPPORTED
        }
    }

}