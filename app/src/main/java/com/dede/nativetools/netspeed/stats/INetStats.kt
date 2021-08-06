package com.dede.nativetools.netspeed.stats

import android.net.TrafficStats
import android.util.Log

interface INetStats {

    companion object {

        const val UNSUPPORTED = TrafficStats.UNSUPPORTED.toLong()

        const val WLAN_IFACE = "wlan0"
        const val MOBILE_IFACE = "rmnet_data0"

        fun Long.isSupported(): Boolean = this != UNSUPPORTED

        fun addIfSupported(vararg stats: Long): Long {
            var allStat: Long = 0
            for (stat in stats) {
                allStat += if (stat.isSupported()) stat else 0
            }
            return allStat
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
            Log.i("NetSpeedHelper", "INetStats: $iNetStats")
            return checkNotNull(iNetStats) { "Not found supported INetStats" }
        }

        private fun create(clazz: Class<out INetStats>): INetStats {
            return when (clazz) {
                Android31NetStats::class.java -> Android31NetStats()
                ReflectNetStats::class.java -> ReflectNetStats()
                ExcludeLoNetStats::class.java -> ExcludeLoNetStats()
                NormalNetStats::class.java -> NormalNetStats()
                else -> throw IllegalArgumentException("INetStats: $clazz don`t supported")
            }
        }
    }

    /**
     * NetStats是否支持
     */
    fun supported(): Boolean

    /**
     * 下载字节
     */
    fun getRxBytes(): Long

    /**
     * 长传字节
     */
    fun getTxBytes(): Long

}