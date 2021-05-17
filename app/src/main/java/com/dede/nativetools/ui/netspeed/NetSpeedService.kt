package com.dede.nativetools.ui.netspeed


import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.*
import android.util.Log
import com.dede.nativetools.MainActivity
import com.dede.nativetools.R
import kotlin.properties.Delegates


class NetSpeedService : Service() {

    class NetSpeedBinder(private val service: NetSpeedService) : Binder() {

        fun setInterval(interval: Int) {
            service.interval = interval
        }

        fun setNotifyClickable(clickable: Boolean) {
            service.notifyClickable = clickable
        }

        fun setLockHide(hide: Boolean) {
            service.compatibilityMode = hide
        }

        fun setMode(mode: String) {
            service.mode = mode
        }

        fun setScale(scale: Float) {
            service.scale = scale
        }
    }

    companion object {
        private const val NOTIFY_ID = 10
        private const val CHANNEL_ID = "net_speed"

        const val EXTRA_INTERVAL = "extra_interval"
        const val EXTRA_LOCKED_HIDE = "extra_locked_hide"
        const val EXTRA_NOTIFY_CLICKABLE = "extra_notify_clickable"
        const val EXTRA_MODE = "extra_mode"
        const val EXTRA_SCALE = "extra_scale"
        const val DEFAULT_INTERVAL = 1000
        const val DEFAULT_SCALE = 1f

        const val MODE_DOWN = "0"
        const val MODE_ALL = "1"
        const val MODE_UP = "2"
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val binder = NetSpeedBinder(this)

    private val speed = Speed()
    internal var interval: Int by Delegates.observable(DEFAULT_INTERVAL) { _, _, new ->
        speed.interval = new
    }
    internal var notifyClickable = true
    internal var mode = MODE_DOWN
    internal var scale: Float = DEFAULT_SCALE

    // 锁屏时隐藏(兼容模式)
    private var compatibilityMode = false

    private val handler = Handler(Looper.getMainLooper())

    private val notifyRunnable = object : Runnable {
        override fun run() {
            val notify = createNotification()
            notificationManager.notify(NOTIFY_ID, notify)

            handler.postDelayed(this, interval.toLong())
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_SCREEN_ON)// 打开屏幕
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF)// 关闭屏幕
        intentFilter.addAction(Intent.ACTION_USER_PRESENT)// 解锁
        registerReceiver(receiver, intentFilter)

        resume()
    }

    private val receiver: BroadcastReceiver = LockedHideReceiver()

    /**
     * 恢复指示器
     */
    private fun resume() {
        handler.removeCallbacks(notifyRunnable)

        speed.reset()
        val notify = createNotification()
        startForeground(NOTIFY_ID, notify)

        handler.post(notifyRunnable)
    }

    /**
     * 暂停指示器
     */
    private fun pause(stopForeground: Boolean = true) {
        handler.removeCallbacks(notifyRunnable)
        if (stopForeground) {
            notificationManager.cancel(NOTIFY_ID)
            stopForeground(true)
        } else {
            startForeground(NOTIFY_ID, createNotification())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            if (intent.getBooleanExtra(EXTRA_LOCKED_HIDE, false)) {
                this.compatibilityMode = true
            }
            this.interval = intent.getIntExtra(EXTRA_INTERVAL, DEFAULT_INTERVAL)
            this.notifyClickable = intent.getBooleanExtra(EXTRA_NOTIFY_CLICKABLE, true)
            this.mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_DOWN
            this.scale = intent.getFloatExtra(EXTRA_SCALE, DEFAULT_SCALE)
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

    private val builder by lazy(LazyThreadSafetyMode.NONE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
                .setSound(null)
        }
    }

    private fun createNotification(): Notification {
        createChannel()

        val downloadSpeed = speed.getRxSpeed()
        val uploadSpeed = speed.getTxSpeed()
        // android.text.format.Formatter.formatFileSize(android.content.Context, long)
        // 8.0以后使用的单位是1000，非1024
        val downloadSpeedStr: String = NetUtil.formatNetSpeedStr(downloadSpeed)
        val uploadSpeedStr: String = NetUtil.formatNetSpeedStr(uploadSpeed)

        val contentStr = getString(R.string.notify_net_speed_msg, downloadSpeedStr, uploadSpeedStr)
        builder.setSubText(NetUtil.getTodayRx(this))
            .setContentText(contentStr)
            .setAutoCancel(false)
            .setVisibility(Notification.VISIBILITY_SECRET)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setBadgeIconType(Notification.BADGE_ICON_NONE)
        }

        val icon = createIcon(downloadSpeed, uploadSpeed)
        builder.setSmallIcon(icon)

        if (notifyClickable) {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val pendingIntent = PendingIntent.getActivity(
                this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.setContentIntent(pendingIntent)
        }

        return builder.build()
    }

    private fun createIcon(downloadSpeed: Long, uploadSpeed: Long): Icon {
        val bitmap = when (mode) {
            MODE_ALL -> {
                val down = NetUtil.formatNetSize(downloadSpeed)
                val up = NetUtil.formatNetSize(uploadSpeed)
                NetTextIconFactory.createDoubleIcon(up, down, scale)
            }
            MODE_UP -> {
                val upSplit: Array<String> = NetUtil.formatNetSpeed(uploadSpeed)
                NetTextIconFactory.createSingleIcon(upSplit[0], upSplit[1], scale)
            }
            else -> {
                val downSplit: Array<String> = NetUtil.formatNetSpeed(downloadSpeed)
                NetTextIconFactory.createSingleIcon(downSplit[0], downSplit[1], scale)
            }
        }
        return Icon.createWithBitmap(bitmap)
    }

    override fun onDestroy() {
        pause()
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    /**
     * 接收解锁、熄屏、亮屏广播
     */
    private inner class LockedHideReceiver : BroadcastReceiver() {

        private val keyguardManager by lazy(LazyThreadSafetyMode.NONE) {
            getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i("LockedHideReceiver", intent?.action ?: "null")
            // 非兼容模式
            if (!compatibilityMode) {
                when (intent?.action) {
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
            when (intent?.action) {
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
