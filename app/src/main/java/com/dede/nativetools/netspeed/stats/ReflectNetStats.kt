package com.dede.nativetools.netspeed.stats

import android.net.TrafficStats
import com.dede.nativetools.netspeed.stats.INetStats.Companion.isSupported
import com.dede.nativetools.util.method
import java.lang.reflect.Method

class ReflectNetStats : INetStats {

    private var methodGetRxBytes: Method? = null
    private var methodGetTxBytes: Method? = null

    init {
        methodGetRxBytes = TrafficStats::class.java
            .runCatching { method("getRxBytes", String::class.java) }
            .onFailure(Throwable::printStackTrace)
            .getOrNull()
        methodGetTxBytes = TrafficStats::class.java
            .runCatching { method("getTxBytes", String::class.java) }
            .onFailure(Throwable::printStackTrace)
            .getOrNull()
    }

    override fun supported(): Boolean {
        return methodGetRxBytes != null && methodGetTxBytes != null
                && getRxBytes(INetStats.WLAN_IFACE).isSupported()
    }

    override fun getRxBytes(): Long {
        return INetStats.addIfSupported(
            TrafficStats.getMobileRxBytes(),
            getRxBytes(INetStats.WLAN_IFACE)
        )
    }

    override fun getTxBytes(): Long {
        return INetStats.addIfSupported(
            TrafficStats.getMobileTxBytes(),
            getTxBytes(INetStats.WLAN_IFACE)
        )
    }

    private fun getRxBytes(iface: String): Long {
        return getIFaceBytes(methodGetRxBytes, iface)
    }

    private fun getTxBytes(iface: String): Long {
        return getIFaceBytes(methodGetTxBytes, iface)
    }

    private fun getIFaceBytes(method: Method?, iface: String): Long {
        if (method == null) return INetStats.UNSUPPORTED
        return method.runCatching { invoke(null, iface) as Long }
            .getOrDefault(INetStats.UNSUPPORTED)
    }
}