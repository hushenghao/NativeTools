package com.dede.nativetools.netspeed


import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import com.dede.nativetools.util.trimZeroAndDot
import java.text.DecimalFormat
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

    private val df0 = DecimalFormat("0")
    private val df1 = DecimalFormat("0.0")
    private val df2 = DecimalFormat("0.00")

    private fun formatNumberInternal(num: Double, accuracy: Int): String {
        val format = when (accuracy) {
            ACCURACY_EQUAL_WIDTH_EXACT -> when {
                num >= 100 -> df0 // 100.2 -> 100
                num >= 10 -> df1 // 10.22 -> 10.2
                else -> df2 // 0.223 -> 0.22
            }
            ACCURACY_EQUAL_WIDTH -> when {
                num >= 10 -> df0 // 10.2 -> 10
                else -> df1 // 1.22 -> 1.2
            }
            ACCURACY_EXACT -> df2 // 0.223 -> 0.22
            else -> df2
        }
        return format.format(num).trimZeroAndDot()
    }

    /**
     * 获取每月下载数据字节数
     */
    fun getMonthRxBytes(context: Context): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return getRxBytesInternal(context, calendar)
    }

    /**
     * 获取每天下载数据字节数
     */
    fun getTodayRxBytes(context: Context): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return getRxBytesInternal(context, calendar)
    }

    private fun getRxBytesInternal(context: Context, start: Calendar): Long {
        val startTime = start.timeInMillis
        val endTime = System.currentTimeMillis()
        val networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
                ?: return 0L
        val wifiBucket = try {
            networkStatsManager.querySummaryForDevice(
                ConnectivityManager.TYPE_WIFI,
                null,
                startTime,
                endTime
            )
        } catch (e: Exception) {
            null
        }
        val mobileBucket = try {
            networkStatsManager.querySummaryForDevice(
                ConnectivityManager.TYPE_MOBILE,
                null,
                startTime,
                endTime
            )
        } catch (e: Exception) {
            null
        }
        return (wifiBucket?.rxBytes ?: 0) + (mobileBucket?.rxBytes ?: 0)

    }

}
