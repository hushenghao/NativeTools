package com.dede.nativetools.netspeed.utils

import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import java.util.*

/**
 * 网络使用状态工具
 *
 * @author hsh
 * @since 2021/8/6 10:35 上午
 */
object NetworkUsageUtil {

    private fun Calendar.toZeroH(): Calendar {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        return this
    }

    /**
     * 获取每月网络使用字节数
     */
    fun monthNetworkUsageBytes(context: Context): Long {
        val start = Calendar.getInstance().toZeroH()
        start.set(Calendar.DAY_OF_MONTH, 1)
        return getNetworkUsageBytesInternal(context, start)
    }

    /**
     * 获取每天网络使用字节数
     */
    fun todayNetworkUsageBytes(context: Context): Long {
        val start = Calendar.getInstance().toZeroH()
        return getNetworkUsageBytesInternal(context, start)
    }

    private fun getNetworkUsageBytesInternal(context: Context, start: Calendar): Long {
        val networkStatsManager = context.getSystemService<NetworkStatsManager>() ?: return 0L
        val startTime = start.timeInMillis
        val endTime = System.currentTimeMillis()
        val wifiUsageBytes = networkStatsManager.queryNetworkUsageBytes(
            ConnectivityManager.TYPE_WIFI,
            startTime,
            endTime
        )
        val mobileUsageBytes = networkStatsManager.queryNetworkUsageBytes(
            ConnectivityManager.TYPE_MOBILE,
            startTime,
            endTime
        )
        return wifiUsageBytes + mobileUsageBytes
    }

    private fun NetworkStatsManager.queryNetworkUsageBytes(
        networkType: Int,
        startTime: Long,
        endTime: Long
    ): Long {
        return this.runCatching {
            val bucket = querySummaryForDevice(
                networkType, null, startTime, endTime
            )
            bucket.rxBytes + bucket.txBytes
        }.getOrDefault(0)
    }
}