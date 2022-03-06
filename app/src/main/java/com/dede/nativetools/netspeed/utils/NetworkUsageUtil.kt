package com.dede.nativetools.netspeed.utils

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import androidx.annotation.WorkerThread
import com.dede.nativetools.util.Logic
import com.dede.nativetools.util.requireSystemService
import com.dede.nativetools.util.toZeroH
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

    private class NetworkUsageJob<T : Comparable<T>>(data: T) : CompletionHandler {
        private val dataRef = AtomicReference<T>(data)
        val data: T get() = dataRef.get()

        private var job: Job? = null

        private val coroutineScope = CoroutineScope(Dispatchers.IO)

        fun execute(block: suspend CoroutineScope.() -> T) {
            var job = this.job
            if (job != null && !job.isCompleted && !job.isCancelled) {
                return
            }
            job = coroutineScope.launch {
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
     * 获取每月移动网络使用字节数
     */
    fun monthMobileUsageBytes(context: Context): Long {
        val weakRefContext = WeakReference(context)
        monthNetworkUsageJob.execute {
            val ctx = weakRefContext.get() ?: return@execute 0
            val start = Calendar.getInstance().toZeroH()
            start.set(Calendar.DAY_OF_MONTH, 1)
            getMobileUsageBytesInternal(ctx, start)
        }
        return monthNetworkUsageJob.data
    }

    /**
     * 获取每天移动网络使用字节数
     */
    fun todayMobileUsageBytes(context: Context): Long {
        val weakRefContext = WeakReference(context)
        todayNetworkUsageJob.execute {
            val ctx = weakRefContext.get() ?: return@execute 0
            val start = Calendar.getInstance().toZeroH()
            getMobileUsageBytesInternal(ctx, start)
        }
        return todayNetworkUsageJob.data
    }

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

    fun networkUsageDiagnosis(context: Context): Long {
        if (!Logic.checkAppOps(context)) {
            return -1
        }
        val start = Calendar.getInstance().toZeroH()
        return runBlocking {
            getNetworkUsageBytesInternal(context, start)
        }
    }

    private suspend fun getMobileUsageBytesInternal(context: Context, start: Calendar): Long {
        val networkStatsManager = context.requireSystemService<NetworkStatsManager>()
        val startTime = start.timeInMillis
        val endTime = System.currentTimeMillis()
        val mobileUsageBytes = withContext(Dispatchers.IO) {
            networkStatsManager.queryNetworkUsageBytes(
                @Suppress("DEPRECATION") ConnectivityManager.TYPE_MOBILE,
                startTime,
                endTime
            )
        }
        return mobileUsageBytes shr 12 shl 12// tolerance 4096
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
        return (wifiUsageBytes + mobileUsageBytes) shr 12 shl 12// tolerance 4096
    }

    @WorkerThread
    private fun NetworkStatsManager.queryNetworkUsageBytes(
        networkType: Int,
        startTime: Long,
        endTime: Long,
    ): Long {
        val bucket = this.queryNetworkUsageBucket(networkType, startTime, endTime) ?: return 0L
        return bucket.rxBytes + bucket.txBytes
    }

    @WorkerThread
    fun NetworkStatsManager.queryNetworkUsageBucket(
        networkType: Int,
        startTime: Long,
        endTime: Long,
    ): NetworkStats.Bucket? {
        return this.querySummaryForDevice(networkType, null, startTime, endTime)
    }
}