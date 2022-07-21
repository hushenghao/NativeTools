package com.dede.nativetools.netspeed.stats

import android.net.TrafficStats
import com.dede.nativetools.netspeed.stats.NetStats.Companion.isSupported
import com.dede.nativetools.util.invokeWithReturn
import com.dede.nativetools.util.method
import java.lang.reflect.Method

class ReflectNetStats : NetStats {

    private var methodGetRxBytes: Method? = null
    private var methodGetTxBytes: Method? = null

    init {
        try {
            methodGetRxBytes = TrafficStats::class.java.method("getRxBytes", String::class.java)
            methodGetTxBytes = TrafficStats::class.java.method("getTxBytes", String::class.java)
        } catch (e: Exception) {}
    }

    override fun supported(): Boolean {
        return methodGetRxBytes != null &&
            methodGetTxBytes != null &&
            getRxBytes(NetStats.WLAN_IFACE).isSupported
    }

    override fun getRxBytes(): Long {
        return NetStats.addIfSupported(
            TrafficStats.getMobileRxBytes(), getRxBytes(NetStats.WLAN_IFACE))
    }

    override fun getTxBytes(): Long {
        return NetStats.addIfSupported(
            TrafficStats.getMobileTxBytes(), getTxBytes(NetStats.WLAN_IFACE))
    }

    private fun getRxBytes(iface: String): Long {
        return getIFaceBytes(methodGetRxBytes, iface)
    }

    private fun getTxBytes(iface: String): Long {
        return getIFaceBytes(methodGetTxBytes, iface)
    }

    private fun getIFaceBytes(method: Method?, iface: String): Long {
        if (method == null) return NetStats.UNSUPPORTED
        return try {
            method.invokeWithReturn(null, iface)
        } catch (e: Exception) {
            NetStats.UNSUPPORTED
        }
    }
}
