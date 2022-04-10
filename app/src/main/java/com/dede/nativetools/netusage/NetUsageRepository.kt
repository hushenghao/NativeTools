package com.dede.nativetools.netusage

import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import com.dede.nativetools.R
import com.dede.nativetools.netspeed.utils.NetFormatter
import com.dede.nativetools.netusage.utils.NetUsageUtils.queryNetUsageBucket
import com.dede.nativetools.util.requireSystemService
import com.dede.nativetools.util.splicing
import com.dede.nativetools.util.toZeroH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt


data class NetUsage(
    val wlanUpload: Long,
    val wlanDownload: Long,

    val mobileUpload: Long,
    val mobileDownload: Long,

    var label: String,
    var fullLabel: String,
) {
    val currentMax: Long
        get() = max(max(wlanUpload, wlanDownload), max(mobileUpload, mobileDownload))

    var wlanDownloadProgress: Int = 0
        private set
    var wlanUploadProgress: Int = 0
        private set

    var mobileDownloadProgress: Int = 0
        private set
    var mobileUploadProgress: Int = 0
        private set

    /**
     * 计算相对max的百分比，max可能为0
     */
    fun calculateProgress(max: Long) {
        if (max <= 0) return
        wlanDownloadProgress = (wlanDownload * 100f / max).roundToInt()
        wlanUploadProgress = (wlanUpload * 100f / max).roundToInt()
        mobileDownloadProgress = (mobileDownload * 100f / max).roundToInt()
        mobileUploadProgress = (mobileUpload * 100f / max).roundToInt()
    }

    private fun formatBytes(bytes: Long?): String {
        if (bytes == null) return "--"
        return NetFormatter.format(bytes, NetFormatter.FLAG_BYTE, NetFormatter.ACCURACY_EXACT)
            .splicing()
    }

    fun formatString(context: Context): String {
        val sb = StringBuilder()
            .append(fullLabel)
            .append(": ")
            .appendLine()
            .append("Total: \t")
            .append(
                context.getString(
                    R.string.notify_net_speed_msg,
                    formatBytes(wlanUpload + mobileUpload),
                    formatBytes(wlanDownload + mobileDownload)
                )
            )
            .appendLine()
            .append("WLAN: \t")
            .append(
                context.getString(
                    R.string.notify_net_speed_msg,
                    formatBytes(wlanUpload),
                    formatBytes(wlanDownload)
                )
            )
            .appendLine()
            .append("Mobile: \t")
            .append(
                context.getString(
                    R.string.notify_net_speed_msg,
                    formatBytes(mobileUpload),
                    formatBytes(mobileDownload)
                )
            )
        return sb.toString()
    }

}

class NetUsageRepository {

    /**
     * 加载网络使用情况
     */
    suspend fun loadNetUsage(context: Context): List<NetUsage> {
        val manager = context.requireSystemService<NetworkStatsManager>()
        val end = Calendar.getInstance()
        val start = Calendar.getInstance().toZeroH()
        start.set(Calendar.DAY_OF_MONTH, 1)// 当月1号
        start.add(Calendar.MONTH, -5)// 5个月前
        val list = getMonthDateRanges(start, end).map {
            loadNetUsage(manager, it.first.timeInMillis, it.second.timeInMillis)
        }.toMutableList()
        // 添加今日流量
        val todayDateRange = getTodayDateRange()
        list.add(
            loadNetUsage(
                manager,
                todayDateRange.first.timeInMillis,
                todayDateRange.second.timeInMillis,
                context.getString(R.string.label_today)
            )
        )
        return list
    }

    /**
     * 计算坐标系最大范围
     */
    fun calculateMax(list: List<NetUsage>): Long {
        var max: Long = 0
        for (netUsage in list) {
            max = max(netUsage.currentMax, max)
        }
        return NetFormatter.calculateCeilBytes(max)// 取天花板数
    }

    /**
     * 获取日期范围内每个月时间范围
     */
    private fun getMonthDateRanges(start: Calendar, end: Calendar): List<Pair<Calendar, Calendar>> {
        val result = mutableListOf<Pair<Calendar, Calendar>>()
        var current = start
        while (current.before(end)) {
            val next = Calendar.getInstance()
            next.timeInMillis = current.timeInMillis
            next.add(Calendar.MONTH, 1)
            result.add(Pair(current, next))
            current = next
        }
        return result
    }

    /**
     * 获取今天的时间范围
     */
    private fun getTodayDateRange(): Pair<Calendar, Calendar> {
        val start = Calendar.getInstance().toZeroH()
        val end = Calendar.getInstance()
        return Pair(start, end)
    }

    private suspend fun loadNetUsage(
        manager: NetworkStatsManager,
        start: Long,
        end: Long,
        label: String = "%tb".format(Date(start)),
    ): NetUsage {
        @Suppress("DEPRECATION")
        return withContext(Dispatchers.IO) {
            val bucketMobile =
                manager.queryNetUsageBucket(ConnectivityManager.TYPE_MOBILE, null, start, end)
            val bucketWlan =
                manager.queryNetUsageBucket(ConnectivityManager.TYPE_WIFI, null, start, end)
            return@withContext NetUsage(
                wlanUpload = bucketWlan?.rxBytes ?: 0L,
                wlanDownload = bucketWlan?.txBytes ?: 0L,
                mobileUpload = bucketMobile?.rxBytes ?: 0L,
                mobileDownload = bucketMobile?.txBytes ?: 0L,
                label = label,
                fullLabel = "%tF ~ %tF".format(Date(start), Date(end - 1))// 减去最后一毫秒，让日期范围显示的更合理
            )
        }
    }
}