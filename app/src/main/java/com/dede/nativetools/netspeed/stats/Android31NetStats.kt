package com.dede.nativetools.netspeed.stats

import android.annotation.SuppressLint
import android.net.TrafficStats
import android.os.Build
import androidx.annotation.RequiresApi

class Android31NetStats : INetStats {

    @RequiresApi(Build.VERSION_CODES.S)
    private var supportWlan0 =
        TrafficStats.getRxBytes(INetStats.WLAN_IFACE) != INetStats.UNSUPPORTED

    override fun supported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && supportWlan0
    }

    @SuppressLint("NewApi")
    override fun getRxBytes(): Long {
        return TrafficStats.getMobileRxBytes() +
                INetStats.addIfSupported(TrafficStats.getRxBytes(INetStats.WLAN_IFACE))
    }

    @SuppressLint("NewApi")
    override fun getTxBytes(): Long {
        return TrafficStats.getMobileTxBytes() +
                INetStats.addIfSupported(TrafficStats.getTxBytes(INetStats.WLAN_IFACE))
    }

}