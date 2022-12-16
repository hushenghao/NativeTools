package com.dede.nativetools.netspeed.service

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import androidx.core.content.getSystemService
import androidx.work.*
import com.dede.nativetools.util.event
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

/**
 * 守护服务.
 *
 * @author shhu
 * @since 2022/12/16
 */
class HeartbeatWork(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object {

        private const val WORK_NAME = "Heartbeat Work"

        fun daemon(context: Context) {
            val workManager = WorkManager.getInstance(context)

            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
            // The minimum interval duration for PeriodicWorkRequest (in milliseconds).
            // 15 minutes
            val workRequest = PeriodicWorkRequestBuilder<HeartbeatWork>(15L, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            workManager.enqueue(workRequest)
            workManager.enqueueUniquePeriodicWork(WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, workRequest)
        }

        fun stop(context: Context) {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(WORK_NAME)
        }
    }

    private fun Context.isServiceRunning(clazz: Class<out Service>): Boolean {
        val activityManager = this.getSystemService<ActivityManager>() ?: return false
        val runningServices = activityManager.getRunningServices(10) ?: return false
        for (serviceInfo in runningServices) {
            if (serviceInfo.service.className == clazz.name) {
                return true
            }
        }
        return false
    }

    override fun doWork(): Result {
        if (applicationContext.isServiceRunning(NetSpeedService::class.java)) {
            return Result.success()
        }
        event("Heartbeat awaken")
        NetSpeedService.launchForeground(applicationContext)
        return Result.success()
    }
}
