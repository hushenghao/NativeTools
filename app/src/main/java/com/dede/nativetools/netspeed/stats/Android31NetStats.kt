package com.dede.nativetools.netspeed.stats

import android.net.TrafficStats
import android.os.Build
import androidx.annotation.RequiresApi
import com.dede.nativetools.netspeed.stats.NetStats.Companion.isSupported

class Android31NetStats : NetStats {

    // no hide
    @RequiresApi(Build.VERSION_CODES.S)
    private var supportWlan0 = TrafficStats.getRxBytes(NetStats.WLAN_IFACE).isSupported

    override fun supported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && supportWlan0
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun getRxBytes(): Long {
        return NetStats.addIfSupported(
            TrafficStats.getMobileRxBytes(),
            TrafficStats.getRxBytes(NetStats.WLAN_IFACE)
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun getTxBytes(): Long {
        return NetStats.addIfSupported(
            TrafficStats.getMobileTxBytes(),
            TrafficStats.getTxBytes(NetStats.WLAN_IFACE)
        )
    }
}
