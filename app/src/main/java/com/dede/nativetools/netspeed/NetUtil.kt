package com.dede.nativetools.netspeed


import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import java.text.DecimalFormat
import java.util.*


/**
 * Created by hsh on 2017/5/15 015 下午 05:14.
 */
object NetUtil {

    private const val UNIT = 1024
    private const val THRESHOLD = 900

    private val df0 = DecimalFormat("0")
    private val df1 = DecimalFormat("0.0")
    private val df2 = DecimalFormat("0.00")

    fun formatNetSpeedStr(speedByte: Long): String {
        var speed = speedByte.toDouble()
        var suffix = "B/s"
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "KB/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "MB/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "GB/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "TB/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "PB/s"
        }
        return df2.format(speed).trimZeroAndDot() + suffix
    }

    fun formatNetSpeed(speedByte: Long): Array<String> {
        var speed = speedByte.toDouble()
        var suffix = "B/s"
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "KB/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "MB/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "GB/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "TB/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "PB/s"
        }
        val format = when {
            speed >= 100 -> { // 100.2 -> 100
                df0.format(speed)
            }
            speed >= 10 -> {// 10.22 -> 10.2
                df1.format(speed)
            }
            else -> {// 0.223 -> 0.22
                df2.format(speed)
            }
        }
        return arrayOf(format.trimZeroAndDot(), suffix)
    }

    fun formatNetSize(speedByte: Long): String {
        var speed = speedByte.toDouble()
        var suffix = "B"
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "K"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "M"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "G"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "T"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "P"
        }
        val format = when {
            speed >= 10 -> { // 10.2
                df0.format(speed)
            }
            else -> {// 1.2
                df1.format(speed)
            }
        }
        return format.trimZeroAndDot() + suffix
    }

    private val regexTrimZero = Regex("0+?$")
    private val regexTrimDot = Regex("[.]$")

    private fun String.trimZeroAndDot(): String {
        var s = this
        if (s.indexOf(".") > 0) {
            // 去掉多余的0
            s = s.replace(regexTrimZero, "")
            // 如最后一位是.则去掉
            s = s.replace(regexTrimDot, "")
        }
        return s
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
