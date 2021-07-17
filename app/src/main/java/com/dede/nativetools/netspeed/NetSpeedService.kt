package com.dede.nativetools.netspeed


import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import com.dede.nativetools.MainActivity
import com.dede.nativetools.R
import com.dede.nativetools.util.checkAppOps
import com.dede.nativetools.util.splicing


class NetSpeedService : Service() {

    class NetSpeedBinder(private val service: NetSpeedService) : INetSpeedInterface.Stub() {

        override fun updateConfiguration(configuration: NetSpeedConfiguration?) {
            service.configuration.copy(configuration ?: return)
                .also { service.netSpeedHelper.interval = it.interval }
        }
    }

    companion object {
        private const val NOTIFY_ID = 10
        private const val CHANNEL_ID = "net_speed"
        const val ACTION_CLOSE = "com.dede.nativetools.CLOSE"

        const val EXTRA_CONFIGURATION = "extra_configuration"

        fun createServiceIntent(context: Context): Intent {
            val intent = Intent(context, NetSpeedService::class.java)
            val configuration = NetSpeedConfiguration.create()
            intent.putExtra(EXTRA_CONFIGURATION, configuration)
            return intent
        }
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val binder = NetSpeedBinder(this)

    private val netSpeedHelper = NetSpeedHelper { rxSpeed, txSpeed ->
        val notify = createNotification(rxSpeed, txSpeed)
        notificationManager.notify(NOTIFY_ID, notify)
    }

    private val configuration = NetSpeedConfiguration()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_SCREEN_ON)// 打开屏幕
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF)// 关闭屏幕
        intentFilter.addAction(Intent.ACTION_USER_PRESENT)// 解锁
        intentFilter.addAction(ACTION_CLOSE)// 关闭
        registerReceiver(lockedHideReceiver, intentFilter)

        resume()
    }

    private fun startForeground() {
        val notify = createNotification()
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
            notificationManager.cancel(NOTIFY_ID)
            stopForeground(true)
        } else {
            startForeground()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val configuration = intent?.getParcelableExtra<NetSpeedConfiguration>(EXTRA_CONFIGURATION)
        if (configuration != null) {
            this.configuration.copy(configuration).also { netSpeedHelper.interval = it.interval }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
        if (channel != null) {
            return
        }

        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.label_net_speed),
            NotificationManager.IMPORTANCE_LOW
        )
        notificationChannel.setShowBadge(false)
        notificationChannel.setSound(null, null)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun createNotification(rxSpeed: Long = 0L, txSpeed: Long = 0L): Notification {
        createChannel()

        /**
         * 获取所有数据下载量
         */
        fun getRxSubText(context: Context): String? {
            if (!context.checkAppOps()) {
                return null
            }
            val todayBytes = NetUtil.getTodayNetworkUsageRxBytes(context)
            val monthBytes = NetUtil.getMonthNetworkUsageRxBytes(context)
            return context.getString(
                R.string.notify_net_speed_sub,
                NetUtil.formatBytes(todayBytes, NetUtil.FLAG_BYTE, NetUtil.ACCURACY_EXACT)
                    .splicing(),
                NetUtil.formatBytes(monthBytes, NetUtil.FLAG_BYTE, NetUtil.ACCURACY_EXACT)
                    .splicing()
            )
        }

        val downloadSpeedStr: String =
            NetUtil.formatBytes(rxSpeed, NetUtil.FLAG_FULL, NetUtil.ACCURACY_EXACT).splicing()
        val uploadSpeedStr: String =
            NetUtil.formatBytes(txSpeed, NetUtil.FLAG_FULL, NetUtil.ACCURACY_EXACT).splicing()
        val contentStr = getString(R.string.notify_net_speed_msg, downloadSpeedStr, uploadSpeedStr)

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
                .setSound(null)
        }

        builder.setSubText(getRxSubText(this))
            .setContentText(contentStr)
            .setAutoCancel(false)
            .setVisibility(Notification.VISIBILITY_SECRET)
            .setSmallIcon(createIcon(rxSpeed, txSpeed))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setBadgeIconType(Notification.BADGE_ICON_NONE)
        }

        var pendingFlag = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
            pendingFlag = PendingIntent.FLAG_MUTABLE
        }

        val closeBroadcast = PendingIntent.getBroadcast(
            this,
            0,
            Intent().setAction(ACTION_CLOSE).setPackage(packageName),
            pendingFlag
        )
        val closeAction =
            Notification.Action.Builder(null, getString(R.string.action_close), closeBroadcast)
                .build()
        builder.addAction(closeAction)

        if (configuration.notifyClickable) {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            val pendingIntent = PendingIntent.getActivity(
                this, 0,
                intent, pendingFlag
            )
            builder.setContentIntent(pendingIntent)
        } else {
            builder.setContentIntent(null)
        }

        return builder.build()
    }

    private fun createIcon(downloadSpeed: Long, uploadSpeed: Long): Icon {
        val scale = configuration.scale
        val bitmap = when (configuration.mode) {
            NetSpeedConfiguration.MODE_ALL -> {
                val down =
                    NetUtil.formatBytes(downloadSpeed, 0, NetUtil.ACCURACY_EQUAL_WIDTH).splicing()
                val up =
                    NetUtil.formatBytes(uploadSpeed, 0, NetUtil.ACCURACY_EQUAL_WIDTH).splicing()
                NetTextIconFactory.createDoubleIcon(up, down, scale)
            }
            NetSpeedConfiguration.MODE_UP -> {
                val upSplit = NetUtil.formatBytes(
                    uploadSpeed,
                    NetUtil.FLAG_FULL,
                    NetUtil.ACCURACY_EQUAL_WIDTH_EXACT
                )
                NetTextIconFactory.createSingleIcon(upSplit.first, upSplit.second, scale)
            }
            else -> {
                val downSplit = NetUtil.formatBytes(
                    downloadSpeed,
                    NetUtil.FLAG_FULL,
                    NetUtil.ACCURACY_EQUAL_WIDTH_EXACT
                )
                NetTextIconFactory.createSingleIcon(downSplit.first, downSplit.second, scale)
            }
        }
        return Icon.createWithBitmap(bitmap)
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

        private val keyguardManager by lazy(LazyThreadSafetyMode.NONE) {
            getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        }

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
                    if (keyguardManager.isDeviceLocked || keyguardManager.isKeyguardLocked) {
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
