package com.dede.nativetools.netspeed


import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.dede.nativetools.util.Intent
import com.dede.nativetools.util.get
import com.dede.nativetools.util.globalPreferences


class NetSpeedService : Service() {

    class NetSpeedBinder(private val service: NetSpeedService) : INetSpeedInterface.Stub() {

        override fun updateConfiguration(configuration: NetSpeedConfiguration?) {
            service.updateConfiguration(configuration)
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
            val status = globalPreferences.get(NetSpeedConfiguration.KEY_NET_SPEED_STATUS, false)
            if (status) {
                val intent = createIntent(context)
                ContextCompat.startForegroundService(context, intent)
            }
        }
    }

    private var notificationManager: NotificationManager? = null

    private val netSpeedHelper = NetSpeedHelper { rxSpeed, txSpeed ->
        val notify =
            NetSpeedNotificationHelp.createNotification(this, configuration, rxSpeed, txSpeed)
        notificationManager?.notify(NOTIFY_ID, notify)
    }

    private val configuration = NetSpeedConfiguration.defaultConfiguration

    override fun onBind(intent: Intent): IBinder {
        return NetSpeedBinder(this)
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService<NotificationManager>()
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_SCREEN_ON)// 打开屏幕
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF)// 关闭屏幕
        intentFilter.addAction(Intent.ACTION_USER_PRESENT)// 解锁
        intentFilter.addAction(ACTION_CLOSE)// 关闭
        registerReceiver(lockedHideReceiver, intentFilter)

        resume()
    }

    private fun startForeground() {
        val notify = NetSpeedNotificationHelp.createNotification(this, configuration)
        startForeground(NOTIFY_ID, notify)
    }

    /**
     * 恢复指示器
     */
    private fun resume() {
        startForeground()
        netSpeedHelper.resume()
    }

    /**
     * 暂停指示器
     */
    private fun pause(stopForeground: Boolean = true) {
        netSpeedHelper.pause()
        if (stopForeground) {
            notificationManager?.cancel(NOTIFY_ID)
            stopForeground(true)
        } else {
            startForeground()
        }
    }

    private fun updateConfiguration(configuration: NetSpeedConfiguration?) {
        this.configuration.copy(configuration ?: return)
            .also { netSpeedHelper.interval = it.interval }
        val notification = NetSpeedNotificationHelp.createNotification(
            this,
            this.configuration,
            this.netSpeedHelper.rxSpeed,
            this.netSpeedHelper.txSpeed
        )
        notificationManager?.notify(NOTIFY_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val configuration = intent?.getParcelableExtra<NetSpeedConfiguration>(EXTRA_CONFIGURATION)
        updateConfiguration(configuration)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        pause()
        unregisterReceiver(lockedHideReceiver)
        super.onDestroy()
    }

    /**
     * 接收解锁、熄屏、亮屏广播
     */
    private val lockedHideReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            if (action == ACTION_CLOSE) {
                stopSelf()
                return
            }
            // 非兼容模式
            if (!configuration.compatibilityMode) {
                when (action) {
                    Intent.ACTION_SCREEN_ON -> {
                        resume()// 直接更新指示器
                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        pause(false)// 关闭屏幕时显示，只保留服务保活
                    }
                }
                return
            }

            // 兼容模式
            when (action) {
                Intent.ACTION_SCREEN_ON -> {
                    // 屏幕打开
                    val keyguardManager = getSystemService<KeyguardManager>()
                    if (keyguardManager == null) {
                        resume()
                    } else if (keyguardManager.isDeviceLocked || keyguardManager.isKeyguardLocked) {
                        pause()// 已锁定时隐藏，临时关闭前台服务关闭通知（会降低进程优先级）
                    } else {
                        resume()// 未锁定时显示
                    }
                }
                Intent.ACTION_SCREEN_OFF -> {
                    pause(false)// 关闭屏幕时显示，只保留服务保活
                }
                Intent.ACTION_USER_PRESENT -> {
                    resume()// 解锁后显示
                }
            }
        }
    }
}
