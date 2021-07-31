package com.dede.nativetools.netspeed.stats

import android.net.TrafficStats
import com.dede.nativetools.util.method
import java.lang.reflect.Method

class ReflectNetStats : INetStats {

    private var methodGetRxBytes: Method? = null
    private var methodGetTxBytes: Method? = null

    init {
        try {
            methodGetRxBytes =
                TrafficStats::class.java.method("getRxBytes", String::class.java)
            methodGetTxBytes =
                TrafficStats::class.java.method("getTxBytes", String::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun supported(): Boolean {
        return methodGetRxBytes != null && methodGetTxBytes != null
    }

    override fun getRxBytes(): Long {
        return TrafficStats.getMobileRxBytes() +
                INetStats.addIfSupported(getRxBytes(INetStats.WLAN_IFACE))
    }

    override fun getTxBytes(): Long {
        return TrafficStats.getMobileTxBytes() +
                INetStats.addIfSupported(getTxBytes(INetStats.WLAN_IFACE))
    }

    private fun getRxBytes(iface: String): Long {
        return getIFaceBytes(methodGetRxBytes, iface)
    }

    private fun getTxBytes(iface: String): Long {
        return getIFaceBytes(methodGetTxBytes, iface)
    }

    private fun getIFaceBytes(method: Method?, iface: String): Long {
        if (method == null) return INetStats.UNSUPPORTED
        try {
            return (method.invoke(null, iface) as? Long) ?: INetStats.UNSUPPORTED
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return INetStats.UNSUPPORTED
    }
}