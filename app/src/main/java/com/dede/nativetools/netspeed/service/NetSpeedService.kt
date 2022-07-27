package com.dede.nativetools.netspeed.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.os.HandlerCompat
import com.dede.nativetools.netspeed.INetSpeedInterface
import com.dede.nativetools.netspeed.NetSpeedConfiguration
import com.dede.nativetools.netspeed.NetSpeedPreferences
import com.dede.nativetools.netspeed.utils.NetSpeedCompute
import com.dede.nativetools.util.*
import kotlin.math.max
import kotlinx.coroutines.*

class NetSpeedService : Service(), Runnable {

    class NetSpeedBinder(private val service: NetSpeedService) : INetSpeedInterface.Stub() {

        private val coroutineScope = CoroutineScope(Dispatchers.Main + service.lifecycleJob)

        override fun updateConfiguration(configuration: NetSpeedConfiguration?) {
            if (configuration == null) return
            coroutineScope.launch { service.updateConfiguration(configuration) }
        }
    }

    companion object {
        private const val DELAY_BLANK_NOTIFICATION_ICON = 3000L
        const val INTERVAL_POWER_SAVE_MODE = 5000L

        const val ACTION_CLOSE = "com.dede.nativetools.CLOSE"

        const val EXTRA_CONFIGURATION = "extra_configuration"

        fun createIntent(context: Context): Intent {
            val configuration = NetSpeedConfiguration().updateFrom(globalDataStore.load())
            return Intent<NetSpeedService>(context, EXTRA_CONFIGURATION to configuration)
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

    val lifecycleJob = Job()

    private val showBlankNotificationRunnable = this

    override fun run() {
        // 显示透明图标通知
        configuration.showBlankNotification = true
        NetSpeedNotificationHelper.notification(
            this,
            configuration,
            netSpeedCompute.rxSpeed,
            netSpeedCompute.txSpeed
        )
    }

    private val netSpeedCompute = NetSpeedCompute { rxSpeed, txSpeed ->
        if (!powerManager.isInteractive) {
            // ACTION_SCREEN_OFF广播有一定的延迟，所以设备不可交互时不处理
            return@NetSpeedCompute
        }

        val speed =
            when (configuration.mode) {
                NetSpeedPreferences.MODE_ALL -> max(rxSpeed, txSpeed)
                else -> rxSpeed
            }
        if (speed < configuration.hideThreshold) {
            if (!HandlerCompat.hasCallbacks(uiHandler, showBlankNotificationRunnable)) {
                // 延迟3s再显示透明图标，防止通知图标频繁变动
                uiHandler.postDelayed(showBlankNotificationRunnable, DELAY_BLANK_NOTIFICATION_ICON)
            }
        } else {
            uiHandler.removeCallbacks(showBlankNotificationRunnable)
            configuration.showBlankNotification = false
        }
        NetSpeedNotificationHelper.notification(this, configuration, rxSpeed, txSpeed)
    }

    private val configuration = NetSpeedConfiguration()

    private val broadcastHelper =
        BroadcastHelper(
            PowerManager.ACTION_POWER_SAVE_MODE_CHANGED, // 省电模式变更
            Intent.ACTION_SCREEN_ON, // 打开屏幕
            Intent.ACTION_SCREEN_OFF, // 关闭屏幕
            ACTION_CLOSE // 关闭
        )

    override fun onBind(intent: Intent): IBinder {
        return NetSpeedBinder(this)
    }

    override fun onCreate() {
        super.onCreate()
        configuration.isPowerSaveMode = powerManager.isPowerSaveMode
        NetSpeedNotificationHelper.startForeground(this, configuration)

        broadcastHelper.register(this) { action: String?, _ ->
            when (action ?: return@register) {
                PowerManager.ACTION_POWER_SAVE_MODE_CHANGED -> {
                    val copy = configuration.copy(isPowerSaveMode = powerManager.isPowerSaveMode)
                    updateConfiguration(copy)
                }
                ACTION_CLOSE -> {
                    stopSelf()
                }
                Intent.ACTION_SCREEN_ON -> {
                    track("网速服务广播亮屏恢复") {
                        resume() // 直接更新指示器
                    }
                }
                Intent.ACTION_SCREEN_OFF -> {
                    pause() // 关闭屏幕时显示，只保留服务保活
                }
            }
        }

        resume()
    }

    /** 恢复指示器 */
    private fun resume() {
        netSpeedCompute.start()
    }

    /** 暂停指示器 */
    private fun pause() {
        netSpeedCompute.stop()
    }

    private fun updateConfiguration(configuration: NetSpeedConfiguration?) {
        if (configuration == null || configuration == this.configuration) {
            return
        }
        this.configuration.updateFrom(configuration).also {
            if (it.isPowerSaveMode) {
                // 省电模式下延长刷新间隔
                netSpeedCompute.interval = INTERVAL_POWER_SAVE_MODE.toInt()
            } else {
                netSpeedCompute.interval = it.interval
            }
        }
        NetSpeedNotificationHelper.notification(
            this,
            this.configuration,
            this.netSpeedCompute.rxSpeed,
            this.netSpeedCompute.txSpeed
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        @Suppress("DEPRECATION")
        val configuration = intent?.getParcelableExtra<NetSpeedConfiguration>(EXTRA_CONFIGURATION)
        updateConfiguration(configuration)
        // https://developer.android.google.cn/guide/components/services#CreatingAService
        // https://developer.android.google.cn/reference/android/app/Service#START_REDELIVER_INTENT
        return START_REDELIVER_INTENT // 重建时再次传递Intent
    }

    override fun onDestroy() {
        lifecycleJob.cancel()
        netSpeedCompute.destroy()
        @Suppress("DEPRECATION")
        stopForeground(true)
        notificationManager.cancel(NetSpeedNotificationHelper.NOTIFICATION_ID)
        broadcastHelper.unregister(this)
        super.onDestroy()
    }
}
