package com.dede.nativetools.netspeed


import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import com.dede.nativetools.util.trimZeroAndDot
import java.util.*


/**
 * Created by hsh on 2017/5/15 015 下午 05:14.
 */
object NetUtil {

    /**
     * 精确等宽格式
     */
    const val ACCURACY_EQUAL_WIDTH_EXACT = 1

    /**
     * 精确格式
     */
    const val ACCURACY_EXACT = 2

    /**
     * 低精度格式
     */
    const val ACCURACY_SHORTER = 4

    /**
     * 等宽格式
     */
    const val ACCURACY_EQUAL_WIDTH = 3

    const val FLAG_BYTE = 1
    const val FLAG_INFIX = 1 shl 1
    const val FLAG_SECOND = 1 shl 2

    const val FLAG_FULL = FLAG_BYTE or FLAG_INFIX or FLAG_SECOND

    private const val CHAR_BYTE = 'B'
    private const val CHAR_INFIX = '/'
    private const val CHAR_SECOND = 's'

    private val UNIT_CHARS = charArrayOf('K', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y', 'B')

    // android.text.format.Formatter.formatFileSize(android.content.Context, long)
    // 8.0以后使用的单位是1000，非1024
    private const val UNIT_SIZE = 1024
    private const val THRESHOLD = 900

    fun formatBytes(bytes: Long, flags: Int, accuracy: Int): Pair<String, String> {

        fun hasFlag(flag: Int): Boolean = (flags and flag) > 0

        var speed = bytes.toDouble()
        var unit: Char = CHAR_BYTE
        for (char in UNIT_CHARS) {
            if (speed > THRESHOLD) {
                speed /= UNIT_SIZE
                unit = char
            } else {
                break
            }
        }

        val format = formatNumberInternal(speed, accuracy)// 速度

        val sb = StringBuilder()
            .append(unit)// 单位

        if (hasFlag(FLAG_BYTE) && unit != CHAR_BYTE) {
            sb.append(CHAR_BYTE)// 拼接B
        }
        if (hasFlag(FLAG_INFIX)) {
            sb.append(CHAR_INFIX)// 拼接/
        }
        if (hasFlag(FLAG_SECOND)) {
            sb.append(CHAR_SECOND)// 拼接s
        }

        return Pair(format, sb.toString())
    }


    private fun formatNumberInternal(num: Double, accuracy: Int): String {
        val format = when (accuracy) {
            ACCURACY_EQUAL_WIDTH_EXACT -> when {
                num >= 100 -> "%.0f" // 100.2 -> 100
                num >= 10 -> "%.1f" // 10.22 -> 10.2
                else -> "%.2f" // 0.223 -> 0.22
            }
            ACCURACY_EQUAL_WIDTH -> when {
                num >= 10 -> "%.0f" // 10.2 -> 10
                else -> "%.1f" // 1.22 -> 1.2
            }
            ACCURACY_EXACT -> "%.2f" // 0.223 -> 0.22
            ACCURACY_SHORTER -> "%.1f"
            else -> "%.2f"
        }
        return format.format(num).trimZeroAndDot()
    }

    /**
     * 获取每月下载数据字节数
     */
    fun getMonthNetworkUsageRxBytes(context: Context): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return getNetworkUsageRxBytesInternal(context, calendar)
    }

    /**
     * 获取每天下载数据字节数
     */
    fun getTodayNetworkUsageRxBytes(context: Context): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return getNetworkUsageRxBytesInternal(context, calendar)
    }

    private fun getNetworkUsageRxBytesInternal(context: Context, start: Calendar): Long {
        val networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
                ?: return 0L
        val startTime = start.timeInMillis
        val endTime = System.currentTimeMillis()
        val wifiRxBytes = queryNetworkRxBytes(
            networkStatsManager,
            ConnectivityManager.TYPE_WIFI,
            startTime,
            endTime
        )
        val mobileRxBytes = queryNetworkRxBytes(
            networkStatsManager,
            ConnectivityManager.TYPE_MOBILE,
            startTime,
            endTime
        )
        return wifiRxBytes + mobileRxBytes
    }

    private inline fun queryNetworkRxBytes(
        networkStatsManager: NetworkStatsManager,
        networkType: Int,
        startTime: Long, endTime: Long
    ): Long {
        return try {
            networkStatsManager.querySummaryForDevice(
                networkType, null, startTime, endTime
            ).rxBytes
        } catch (e: Exception) {
            0L
        }
    }

}
