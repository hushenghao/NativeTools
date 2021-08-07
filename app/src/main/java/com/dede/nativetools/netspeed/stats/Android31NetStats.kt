package com.dede.nativetools.netspeed.stats

import android.annotation.SuppressLint
import android.net.TrafficStats
import android.os.Build
import androidx.annotation.RequiresApi
import com.dede.nativetools.netspeed.stats.INetStats.Companion.isSupported
import com.dede.nativetools.util.safely

class Android31NetStats : INetStats {

    @RequiresApi(Build.VERSION_CODES.S)
    private var supportWlan0 = safely(false) {
        // no hide ???
        TrafficStats.getRxBytes(INetStats.WLAN_IFACE).isSupported()
    }

    override fun supported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && supportWlan0
    }

    @SuppressLint("NewApi")
    override fun getRxBytes(): Long {
        return INetStats.addIfSupported(
            TrafficStats.getMobileRxBytes(),
            TrafficStats.getRxBytes(INetStats.WLAN_IFACE)
        )
    }

    @SuppressLint("NewApi")
    override fun getTxBytes(): Long {
        return INetStats.addIfSupported(
            TrafficStats.getMobileTxBytes(),
            TrafficStats.getTxBytes(INetStats.WLAN_IFACE)
        )
    }

}