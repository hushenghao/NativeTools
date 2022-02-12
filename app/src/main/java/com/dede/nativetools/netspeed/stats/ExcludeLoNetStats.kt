package com.dede.nativetools.netspeed.stats

import android.net.TrafficStats
import com.dede.nativetools.netspeed.stats.NetStats.Companion.isSupported
import com.dede.nativetools.util.method
import java.lang.reflect.Method

class ExcludeLoNetStats : NetStats {

    override val name: String
        get() = "ExcludeLoNetStats"

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
        methodGetLoopbackRxBytes = TrafficStats::class.java
            .runCatching { method("getLoopbackRxBytes") }
            .onFailure(Throwable::printStackTrace)
            .getOrNull()
        methodGetLoopbackTxBytes = TrafficStats::class.java
            .runCatching { method("getLoopbackTxBytes") }
            .onFailure(Throwable::printStackTrace)
            .getOrNull()
    }

    private fun getLoopbackRxBytes(): Long {
        return getLoopbackBytes(methodGetLoopbackRxBytes)
    }

    private fun getLoopbackTxBytes(): Long {
        return getLoopbackBytes(methodGetLoopbackTxBytes)
    }

    private fun getLoopbackBytes(method: Method?): Long {
        if (method == null) return NetStats.UNSUPPORTED
        return method.runCatching { invoke(null) as Long }
            .getOrDefault(NetStats.UNSUPPORTED)
    }

}