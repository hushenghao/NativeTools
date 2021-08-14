package com.dede.nativetools.netspeed


import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.content.getSystemService
import com.dede.nativetools.netspeed.utils.DebugClipboardUtil
import com.dede.nativetools.util.Intent
import com.dede.nativetools.util.addActions
import com.dede.nativetools.util.startService


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
        val intentFilter = IntentFilter().addActions(
            Intent.ACTION_SCREEN_ON,// 打开屏幕
            Intent.ACTION_SCREEN_OFF,// 关闭屏幕
            Intent.ACTION_USER_PRESENT,// 解锁
            ACTION_CLOSE// 关闭
        )
        registerReceiver(innerReceiver, intentFilter)

        // adb广播操作剪切板
        DebugClipboardUtil.register(this)

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
        unregisterReceiver(innerReceiver)
        DebugClipboardUtil.unregister(this)
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
                    pause(false)// 关闭屏幕时显示，只保留服务保活
                }
            }
        }
    }
}
