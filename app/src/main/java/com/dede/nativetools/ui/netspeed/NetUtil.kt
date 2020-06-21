package com.dede.nativetools.ui.netspeed


import android.app.AppOpsManager
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Process
import com.dede.nativetools.R
import java.math.BigDecimal
import java.util.*
import kotlin.math.roundToInt


/**
 * Created by hsh on 2017/5/15 015 下午 05:14.
 */
object NetUtil {

    private const val UNIT = 1024
    private const val THRESHOLD = 900

    fun formatNetSpeedStr(speedByte: Long): String {
        var speed = speedByte.toDouble()
        var suffix = "B/s"
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "Kb/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "Mb/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "Gb/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "Tb/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "Pb/s"
        }
        val speedStr = BigDecimal(speed)
            .setScale(2, BigDecimal.ROUND_HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
        return speedStr + suffix
    }

    fun formatNetSpeed(speedByte: Long): Array<String> {
        var speed = speedByte.toDouble()
        var suffix = "B/s"
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "Kb/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "Mb/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "Gb/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "Tb/s"
        }
        if (speed > THRESHOLD) {
            speed /= UNIT
            suffix = "Pb/s"
        }
        val format = when {
            speed >= 100 -> { // 100.2 -> 100
                speed.roundToInt().toString()
            }
            speed >= 10 -> {// 10.22 -> 10.2
                BigDecimal(speed)
                    .setScale(1, BigDecimal.ROUND_HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString()
            }
            speed <= 0.0 -> {
                "0"
            }
            else -> {// 0.223 -> 0.22
                BigDecimal(speed)
                    .setScale(2, BigDecimal.ROUND_HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString()
            }
        }
        return arrayOf(format, suffix)
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
                speed.roundToInt().toString()
            }
            speed <= 0.0 -> {
                "0"
            }
            else -> {// 1.2
                BigDecimal(speed)
                    .setScale(1, BigDecimal.ROUND_HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString()
            }
        }
        return format + suffix
    }

    fun checkAppOps(context: Context): Boolean {
        val appOpsManager =
            context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val result = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return result == AppOpsManager.MODE_ALLOWED
    }

    /**
     * 获取今天所有数据下载量
     */
    fun getTodayRx(context: Context): String? {
        if (!NetUtil.checkAppOps(context)) {
            return null
        }
        val networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val current = System.currentTimeMillis()
        val wifiBucket = networkStatsManager.querySummaryForDevice(
            ConnectivityManager.TYPE_WIFI,
            null,
            calendar.timeInMillis,
            current
        )
        val mobileBucket = networkStatsManager.querySummaryForDevice(
            ConnectivityManager.TYPE_MOBILE,
            null,
            calendar.timeInMillis,
            current
        )
        return context.getString(
            R.string.notify_net_speed_sub,
            NetUtil.formatNetSize(wifiBucket.rxBytes + mobileBucket.rxBytes)
        )
    }

}
