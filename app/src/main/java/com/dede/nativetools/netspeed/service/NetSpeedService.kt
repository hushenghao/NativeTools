package com.dede.nativetools.netspeed.service


import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.dede.nativetools.netspeed.INetSpeedInterface
import com.dede.nativetools.netspeed.NetSpeedConfiguration
import com.dede.nativetools.netspeed.NetSpeedPreferences
import com.dede.nativetools.netspeed.utils.NetSpeedCompute
import com.dede.nativetools.util.*
import kotlinx.coroutines.*


class NetSpeedService : Service() {

    class NetSpeedBinder(private val service: NetSpeedService) : INetSpeedInterface.Stub() {

        override fun updateConfiguration(configuration: NetSpeedConfiguration?) {
            if (configuration == null) return
            service.lifecycleScope.launch(Dispatchers.Main) {
                service.updateConfiguration(configuration)
            }
        }
    }

    companion object {
        private const val NOTIFY_ID = 10
        const val ACTION_CLOSE = "com.dede.nativetools.CLOSE"

        const val EXTRA_CONFIGURATION = "extra_configuration"

        fun createIntent(context: Context): Intent {
            return Intent<NetSpeedService>(
                context,
                EXTRA_CONFIGURATION to NetSpeedConfiguration.initialize()
            )
        }

        fun launchForeground(context: Context) {
            if (NetSpeedPreferences.status) {
                context.startService(createIntent(context), true)
            }
        }

        fun toggle(context: Context) {
            val status = NetSpeedPreferences.status
            val intent = createIntent(context)
            if (status) {
                context.stopService(intent)
            } else {
                context.startService(intent, true)
            }
            NetSpeedPreferences.status = !status
        }
    }

    private val notificationManager: NotificationManager by systemService()
    private val powerManager: PowerManager by systemService()

    val lifecycleScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val netSpeedCompute = NetSpeedCompute { rxSpeed, txSpeed ->
        if (!powerManager.isInteractive) {
            // ACTION_SCREEN_OFF广播有一定的延迟，所以屏幕关闭时不处理
            return@NetSpeedCompute
        }
        val notify =
            NetSpeedNotificationHelper.createNotification(this, configuration, rxSpeed, txSpeed)
        notificationManager.notify(NOTIFY_ID, notify)
    }

    private val configuration = NetSpeedConfiguration.defaultConfiguration

    override fun onBind(intent: Intent): IBinder {
        return NetSpeedBinder(this)
    }

    override fun onCreate() {
        super.onCreate()
        val intentFilter = IntentFilter(
            Intent.ACTION_SCREEN_ON,// 打开屏幕
            Intent.ACTION_SCREEN_OFF,// 关闭屏幕
            ACTION_CLOSE// 关闭
        )
        registerReceiver(innerReceiver, intentFilter)

        startForeground()
        resume()
    }

    private fun startForeground() {
        val notify = NetSpeedNotificationHelper.createNotification(this, configuration)
        startForeground(NOTIFY_ID, notify)
    }

    /**
     * 恢复指示器
     */
    private fun resume() {
        netSpeedCompute.start()
    }

    /**
     * 暂停指示器
     */
    private fun pause() {
        netSpeedCompute.stop()
    }

    private fun updateConfiguration(configuration: NetSpeedConfiguration?) {
        if (configuration ?: return == this.configuration) {
            return
        }
        this.configuration.updateFrom(configuration)
            .also { netSpeedCompute.interval = it.interval }
        val notification = NetSpeedNotificationHelper.createNotification(
            this,
            this.configuration,
            this.netSpeedCompute.rxSpeed,
            this.netSpeedCompute.txSpeed
        )
        notificationManager.notify(NOTIFY_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val configuration = intent?.extra<NetSpeedConfiguration>(EXTRA_CONFIGURATION)
        updateConfiguration(configuration)
        // https://developer.android.google.cn/guide/components/services#CreatingAService
        // https://developer.android.google.cn/reference/android/app/Service#START_REDELIVER_INTENT
        return START_REDELIVER_INTENT// 重建时再次传递Intent
    }

    override fun onDestroy() {
        lifecycleScope.cancel()
        netSpeedCompute.destroy()
        stopForeground(true)
        notificationManager.cancel(NOTIFY_ID)
        unregisterReceiver(innerReceiver)
        super.onDestroy()
    }

    /**
     * 接收解锁、熄屏、亮屏广播
     */
    private val innerReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action ?: return) {
                ACTION_CLOSE -> {
                    stopSelf()
                }
                Intent.ACTION_SCREEN_ON -> {
                    resume()// 直接更新指示器
                }
                Intent.ACTION_SCREEN_OFF -> {
                    pause()// 关闭屏幕时显示，只保留服务保活
                }
            }
        }
    }
}
