package com.dede.nativetools.netspeed.stats

import android.net.TrafficStats
import com.dede.nativetools.util.method
import java.lang.reflect.Method

class ExcludeLoNetStats : INetStats {

    override fun supported(): Boolean {
        return methodGetLoopbackRxBytes != null && methodGetLoopbackTxBytes != null
    }

    override fun getRxBytes(): Long {
        return TrafficStats.getTotalRxBytes() -
                INetStats.addIfSupported(getLoopbackRxBytes())
    }

    override fun getTxBytes(): Long {
        return TrafficStats.getTotalTxBytes() -
                INetStats.addIfSupported(getLoopbackTxBytes())
    }

    private var methodGetLoopbackRxBytes: Method? = null
    private var methodGetLoopbackTxBytes: Method? = null

    init {
        try {
            methodGetLoopbackRxBytes = TrafficStats::class.java.method("getLoopbackRxBytes")
            methodGetLoopbackTxBytes = TrafficStats::class.java.method("getLoopbackTxBytes")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getLoopbackRxBytes(): Long {
        return getLoopbackBytes(methodGetLoopbackRxBytes)
    }

    private fun getLoopbackTxBytes(): Long {
        return getLoopbackBytes(methodGetLoopbackTxBytes)
    }

    private fun getLoopbackBytes(method: Method?): Long {
        if (method == null) return INetStats.UNSUPPORTED
        try {
            return (method.invoke(null) as? Long) ?: INetStats.UNSUPPORTED
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return INetStats.UNSUPPORTED
    }

}