package com.dede.nativetools.netspeed.stats

import android.net.TrafficStats
import android.util.Log
import com.google.firebase.perf.metrics.AddTrace

interface NetStats {

    companion object {

        const val UNSUPPORTED = TrafficStats.UNSUPPORTED.toLong()

        const val WLAN_IFACE = "wlan0"
        //const val MOBILE_IFACE = "rmnet_data0"

        val Long.isSupported: Boolean get() = this != UNSUPPORTED

        fun addIfSupported(vararg stats: Long): Long {
            var allStat: Long = 0
            for (stat in stats) {
                allStat += if (stat.isSupported) stat else 0
            }
            return allStat
        }

        private var netStats: NetStats? = null

        @AddTrace(name = "创建NetStats")
        fun getInstance(): NetStats {
            if (netStats != null) {
                return netStats!!
            }
            val allNetBytesClass = arrayOf(
                AndroidTPB3NetStats::class.java,
                Android31NetStats::class.java,
                ReflectNetStats::class.java,
                ExcludeLoNetStats::class.java,
                NormalNetStats::class.java
            )
            for (clazz in allNetBytesClass) {
                val instance = create(clazz)
                if (instance == null || !instance.supported())
                    continue

                netStats = instance
                break
            }
            Log.i("NetStats", "INetStats: $netStats")
            return checkNotNull(netStats) { "Not found supported INetStats" }
        }

        private fun create(clazz: Class<out NetStats>): NetStats? {
            return clazz.runCatching { newInstance() }
                .onFailure(Throwable::printStackTrace)
                .getOrElse { NormalNetStats() }
        }
    }

    /**
     * NetStats是否支持
     */
    fun supported(): Boolean

    /**
     * 下载的字节
     */
    fun getRxBytes(): Long

    /**
     * 长传的字节
     */
    fun getTxBytes(): Long

    val name: String
        get() = this.javaClass.simpleName

}