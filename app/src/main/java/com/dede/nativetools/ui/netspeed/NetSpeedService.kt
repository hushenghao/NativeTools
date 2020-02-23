package com.dede.nativetools.ui.netspeed


import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.net.TrafficStats
import android.os.*
import android.text.format.Formatter
import android.util.Log
import com.dede.nativetools.MainActivity
import com.dede.nativetools.R
import java.lang.Exception


class NetSpeedService : Service() {

    class NetSpeedBinder(private val service: NetSpeedService) : Binder() {

        fun setInterval(interval: Int) {
            service.interval = interval
        }

        fun setNotifyClickable(clickable: Boolean) {
            service.notifyClickable = clickable
        }

        fun setLockHide(hide: Boolean) {
            service.lockedHide(hide)
        }
    }

    companion object {
        private const val NOTIFY_ID = 10
        private const val CHANNEL_ID = "net_speed"

        const val EXTRA_INTERVAL = "extra_interval"
        const val EXTRA_LOCKED_HIDE = "extra_locked_hide"
        const val EXTRA_NOFITY_CLICKABLE = "extra_nofity_clickable"
        const val DEFAULT_INTERVAL = 1000
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val binder = NetSpeedBinder(this)

    private var rxBytes: Long = 0
    private var txBytes: Long = 0

    internal var interval = DEFAULT_INTERVAL
    internal var notifyClickable = true

    private val handler = Handler(Looper.getMainLooper())

    private val notifyRunnable = object : Runnable {
        override fun run() {
            val notify = createNotification()
            notificationManager.notify(NOTIFY_ID, notify)
            startForeground(NOTIFY_ID, notify)

            handler.postDelayed(this, interval.toLong())
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        resume()
    }

    fun lockedHide(hide: Boolean) {
        if (hide) {
            if (receiver == null) {
                receiver = LockedHideReceiver()
            }
            val intentFilter = IntentFilter()
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF)// 关闭屏幕
            intentFilter.addAction(Intent.ACTION_USER_PRESENT)// 解锁
            registerReceiver(receiver, intentFilter)
        } else if (receiver != null) {
            unregisterReceiver(receiver)
            receiver = null
        }
    }

    private var receiver: BroadcastReceiver? = null

    private fun resume() {
        val notify = createNotification()
        startForeground(NOTIFY_ID, notify)

        rxBytes = TrafficStats.getTotalRxBytes()
        txBytes = TrafficStats.getTotalTxBytes()
        handler.post(notifyRunnable)
    }

    private fun pause() {
        handler.removeCallbacks(notifyRunnable)
        notificationManager.cancel(NOTIFY_ID)

        stopForeground(true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            if (intent.getBooleanExtra(EXTRA_LOCKED_HIDE, false)) {
                lockedHide(true)
            }
            this.interval = intent.getIntExtra(EXTRA_INTERVAL, DEFAULT_INTERVAL)
            this.notifyClickable = intent.getBooleanExtra(EXTRA_NOFITY_CLICKABLE, true)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (channel == null) {
                val notificationChannel = NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.label_net_speed),
                    NotificationManager.IMPORTANCE_LOW
                )
                notificationChannel.setShowBadge(false)
                notificationChannel.setSound(null, null)
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }
    }

    private fun createNotification(): Notification {
        val rxBytes = TrafficStats.getTotalRxBytes()
        val txBytes = TrafficStats.getTotalTxBytes()
        val downloadSpeed = ((rxBytes - this.rxBytes) * 1f / interval * 1000 + .5).toLong()
        val uploadSpeed = ((txBytes - this.txBytes) * 1f / interval * 1000 + .5).toLong()

        this.txBytes = txBytes
        this.rxBytes = rxBytes


        createChannel()

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
                .setSound(null)
        }

        val downloadSpeedStr: String = Formatter.formatFileSize(this, downloadSpeed)
        val uploadSpeedStr: String = Formatter.formatFileSize(this, uploadSpeed)

        builder.setContentTitle(getString(R.string.label_net_speed))
            .setContentText(
                getString(
                    R.string.notify_net_speed_msg,
                    downloadSpeedStr,
                    uploadSpeedStr
                )
            )
            .setAutoCancel(false)
            .setVisibility(Notification.VISIBILITY_PRIVATE)
            .setBadgeIconType(Notification.BADGE_ICON_NONE)

        val split: Array<String> = NetUtil.formatNetSpeed(downloadSpeed)
        val icon = Icon.createWithBitmap(
            NetTextIconFactory.createSingleIcon(split[0], split[1])
        )
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

    override fun onDestroy() {
        pause()
        if (receiver != null) {
            unregisterReceiver(receiver)
        }
        super.onDestroy()
    }

    private inner class LockedHideReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i("LockedHideReceiver", intent?.action ?: "null")
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    pause()
                }
                Intent.ACTION_USER_PRESENT -> {
                    resume()
                }
            }
        }
    }
}
