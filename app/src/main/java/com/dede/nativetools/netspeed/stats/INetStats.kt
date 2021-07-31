package com.dede.nativetools.netspeed.stats

import android.net.TrafficStats
import android.util.Log

interface INetStats {

    companion object {

        const val UNSUPPORTED = TrafficStats.UNSUPPORTED.toLong()

        const val WLAN_IFACE = "wlan0"
        const val MOBILE_IFACE = "rmnet_data0"

        fun addIfSupported(stat: Long): Long {
            return if (stat == UNSUPPORTED) 0 else stat
        }

        private var iNetStats: INetStats? = null

        fun getInstance(): INetStats {
            if (iNetStats != null) {
                return iNetStats!!
            }
            val allNetBytesClass = arrayOf(
                Android31NetStats::class.java,
                ReflectNetStats::class.java,
                ExcludeLoNetStats::class.java,
                NormalNetStats::class.java
            )
            for (clazz in allNetBytesClass) {
                val instance = create(clazz)
                if (!instance.supported())
                    continue

                iNetStats = instance
                break
            }
            if (iNetStats == null) {
                throw IllegalArgumentException("Not found supported INetStats")
            }
            Log.i("NetSpeedHelper", "INetStats: $iNetStats")
            return iNetStats!!
        }

        private fun create(clazz: Class<out INetStats>): INetStats {
            return when (clazz) {
                Android31NetStats::class.java -> Android31NetStats()
                ReflectNetStats::class.java -> ReflectNetStats()
                ExcludeLoNetStats::class.java -> ExcludeLoNetStats()
                else -> NormalNetStats()
            }
        }
    }

    fun supported(): Boolean

    fun getRxBytes(): Long

    fun getTxBytes(): Long

}