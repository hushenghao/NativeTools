package com.dede.nativetools.netspeed.utils

import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import androidx.annotation.WorkerThread
import com.dede.nativetools.util.mainScope
import com.dede.nativetools.util.requireSystemService
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.atomic.AtomicReference

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

    private class NetworkUsageJob<T : Comparable<T>>(data: T) : CompletionHandler {
        private val dataRef = AtomicReference<T>(data)
        val data: T get() = dataRef.get()

        private var job: Job? = null

        fun execute(block: suspend CoroutineScope.() -> T) {
            var job = this.job
            if (job != null && !job.isCompleted && !job.isCancelled) {
                return
            }
            job = mainScope.launch {
                dataRef.set(block())
            }
            job.invokeOnCompletion(this)
            this.job = job
        }

        override fun invoke(cause: Throwable?) {
            cause?.printStackTrace()
        }
    }

    private val monthNetworkUsageJob = NetworkUsageJob<Long>(0)
    private val todayNetworkUsageJob = NetworkUsageJob<Long>(0)

    /**
     * 获取每月网络使用字节数
     */
    fun monthNetworkUsageBytes(context: Context): Long {
        val weakRefContext = WeakReference(context)
        monthNetworkUsageJob.execute {
            val ctx = weakRefContext.get() ?: return@execute 0
            val start = Calendar.getInstance().toZeroH()
            start.set(Calendar.DAY_OF_MONTH, 1)
            getNetworkUsageBytesInternal(ctx, start)
        }
        return monthNetworkUsageJob.data
    }

    /**
     * 获取每天网络使用字节数
     */
    fun todayNetworkUsageBytes(context: Context): Long {
        val weakRefContext = WeakReference(context)
        todayNetworkUsageJob.execute {
            val ctx = weakRefContext.get() ?: return@execute 0
            val start = Calendar.getInstance().toZeroH()
            getNetworkUsageBytesInternal(ctx, start)
        }
        return todayNetworkUsageJob.data
    }

    private suspend fun getNetworkUsageBytesInternal(context: Context, start: Calendar): Long {
        val networkStatsManager = context.requireSystemService<NetworkStatsManager>()
        val startTime = start.timeInMillis
        val endTime = System.currentTimeMillis()
        val wifiUsageBytes = withContext(Dispatchers.IO) {
            networkStatsManager.queryNetworkUsageBytes(
                @Suppress("DEPRECATION") ConnectivityManager.TYPE_WIFI,
                startTime,
                endTime
            )
        }
        val mobileUsageBytes = withContext(Dispatchers.IO) {
            networkStatsManager.queryNetworkUsageBytes(
                @Suppress("DEPRECATION") ConnectivityManager.TYPE_MOBILE,
                startTime,
                endTime
            )
        }
        return (wifiUsageBytes + mobileUsageBytes) shr 12 shl 12
    }

    @WorkerThread
    private fun NetworkStatsManager.queryNetworkUsageBytes(
        networkType: Int,
        startTime: Long,
        endTime: Long
    ): Long {
        val bucket = querySummaryForDevice(networkType, null, startTime, endTime)
        return bucket.rxBytes + bucket.txBytes
    }
}