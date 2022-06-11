package com.dede.nativetools.netusage.utils

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
object NetUsageUtils {

    @Suppress("DEPRECATION")
    const val TYPE_WIFI = ConnectivityManager.TYPE_WIFI

    @Suppress("DEPRECATION")
    const val TYPE_MOBILE = ConnectivityManager.TYPE_MOBILE

    const val RANGE_TYPE_TODAY = 0
    const val RANGE_TYPE_MONTH = 1

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

    private data class JobKey(val type: Int, val subscriberId: String?, val rangeType: Int)

    private val jobsMap = HashMap<JobKey, NetworkUsageJob<Long>>()

    private fun getJob(type: Int, subscriberId: String?, rangeType: Int): NetworkUsageJob<Long> {
        val key = JobKey(type, subscriberId, rangeType)
        return jobsMap.getOrPut(key) { NetworkUsageJob<Long>(0) }
    }

    /**
     * 快速获取网络流量使用情况，可能数据不是最新的
     *
     * @param   context 上下文
     * @param   type 网络类型
     * @param   rangeType 时间范围类型
     * @param   subscriberId 移动网络IMSI
     */
    fun getNetUsageBytes(
        context: Context,
        type: Int,
        rangeType: Int,
        subscriberId: String? = null
    ): Long {
        val weakRefContext = WeakReference(context)
        val job = getJob(type, subscriberId, rangeType)
        job.execute {
            val ctx = weakRefContext.get() ?: return@execute 0
            val start = Calendar.getInstance().toZeroH()
            if (rangeType == RANGE_TYPE_MONTH) {
                start.set(Calendar.DAY_OF_MONTH, 1)
            }
            getNetUsageBytesInternal(ctx, type, subscriberId, start)
        }
        return job.data
    }

    @WorkerThread
    fun networkUsageDiagnosis(context: Context): Long {
        if (!Logic.checkAppOps(context)) {
            return -1
        }
        val start = Calendar.getInstance().toZeroH()
        start.set(Calendar.DAY_OF_MONTH, 1)
        return runBlocking {
            val wifiUsage =
                getNetUsageBytesInternal(context, TYPE_WIFI, null, start)
            val mobileUsage =
                getNetUsageBytesInternal(context, TYPE_MOBILE, null, start)
            wifiUsage + mobileUsage
        }
    }

    private suspend fun getNetUsageBytesInternal(
        context: Context,
        type: Int,
        subscriberId: String?,
        start: Calendar,
    ): Long {
        val networkStatsManager = context.requireSystemService<NetworkStatsManager>()
        val startTime = start.timeInMillis
        val endTime = System.currentTimeMillis()
        val usageBytes = withContext(Dispatchers.IO) {
            networkStatsManager.queryNetUsageBytes(
                type,
                subscriberId,
                startTime,
                endTime
            )
        }
        return usageBytes shr 12 shl 12// tolerance 4096
    }

    @WorkerThread
    private fun NetworkStatsManager.queryNetUsageBytes(
        networkType: Int,
        subscriberId: String?,
        startTime: Long,
        endTime: Long,
    ): Long {
        val bucket =
            this.queryNetUsageBucket(networkType, subscriberId, startTime, endTime) ?: return 0L
        return bucket.rxBytes + bucket.txBytes
    }

    @WorkerThread
    fun NetworkStatsManager.queryNetUsageBucket(
        networkType: Int,
        subscriberId: String?,
        startTime: Long,
        endTime: Long,
    ): NetworkStats.Bucket? {
        return this.runCatching {
            querySummaryForDevice(
                networkType,
                subscriberId,
                startTime,
                endTime
            )
        }.getOrNull()
    }
}