package com.dede.nativetools.netspeed

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.dede.nativetools.R
import com.dede.nativetools.netspeed.utils.NetFormatter
import com.dede.nativetools.netspeed.utils.NetworkUsageUtil
import com.dede.nativetools.ui.MainActivity
import com.dede.nativetools.util.*

/**
 * 网速通知
 */
object NetSpeedNotificationHelper {

    private const val KEY_NOTIFICATION_CHANNEL_VERSION = "notification_channel_version"
    private const val NOTIFICATION_CHANNEL_VERSION = 2

    private const val CHANNEL_ID = "net_speed_${NOTIFICATION_CHANNEL_VERSION}"

    /**
     * 通知渠道版本号
     */
    private var notificationChannelVersion: Int
        get() = globalPreferences.get(KEY_NOTIFICATION_CHANNEL_VERSION, 0)
        set(value) = globalPreferences.set(KEY_NOTIFICATION_CHANNEL_VERSION, value)

    /**
     * 更新通知渠道版本
     */
    fun checkNotificationChannelAndUpgrade(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val target = NOTIFICATION_CHANNEL_VERSION
        val old = notificationChannelVersion
        if (old == 0) {// 第一次安装
            notificationChannelVersion = target
            return
        }
        if (old > target) {
            Log.w(
                "NotificationChannelVersion",
                "version downgrade, old: $old, target: $target"
            )
        }
        val notificationManager = context.requireSystemService<NotificationManager>()
        for (v in (old..target)) {
            when (v) {
                1 -> {
                    // 通知优先级由IMPORTANCE_LOW提升为IMPORTANCE_DEFAULT
                    // fix version 1 bug
                    notificationManager.deleteNotificationChannels("net_speed", "net_speed_1")
                }
                target -> {
                    // 更新版本号
                    notificationChannelVersion = target
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun NotificationManager.deleteNotificationChannels(vararg channelIds: String) {
        for (channelId in channelIds) {
            this.deleteNotificationChannel(channelId)
        }
    }

    private fun isSecure(context: Context): Boolean {
        val keyguardManager = context.requireSystemService<KeyguardManager>()
        return keyguardManager.isDeviceSecure || keyguardManager.isKeyguardSecure
    }

    /**
     * 锁屏通知显示设置
     */
    fun goLockHideNotificationSetting(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isSecure(context)) {
            Intent(
                Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS,
                Settings.EXTRA_APP_PACKAGE to context.packageName,
                Settings.EXTRA_CHANNEL_ID to CHANNEL_ID
            )
        } else {
            // Settings.ACTION_NOTIFICATION_SETTINGS
            Intent("android.settings.NOTIFICATION_SETTINGS")
        }
        intent.newTask().launchActivity(context)
    }

    fun goNotificationSetting(context: Context) {
        val packageName = context.packageName
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(
                Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS,
                Settings.EXTRA_APP_PACKAGE to packageName,
                Settings.EXTRA_CHANNEL_ID to CHANNEL_ID
            )
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$packageName")
        }
        intent.newTask().launchActivity(context)
    }

    fun areNotificationEnabled(context: Context): Boolean {
        val areNotificationsEnabled =
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        val notificationManager = context.requireSystemService<NotificationManager>()
        var channelDisabled = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                notificationManager.getNotificationChannel(CHANNEL_ID)
            if (notificationChannel != null) {
                val importance = notificationChannel.importance
                channelDisabled = importance <= NotificationManager.IMPORTANCE_MIN
            }
        }

        return areNotificationsEnabled && !channelDisabled
    }

    /**
     * 获取所有数据使用量
     */
    private fun getUsageText(context: Context): String? {
        if (!context.checkAppOps()) {
            return null
        }
        val todayBytes = NetworkUsageUtil.todayNetworkUsageBytes(context)
        val monthBytes = NetworkUsageUtil.monthNetworkUsageBytes(context)
        return context.getString(
            R.string.notify_net_speed_sub,
            NetFormatter.format(
                todayBytes,
                NetFormatter.FLAG_BYTE,
                NetFormatter.ACCURACY_EXACT
            ).splicing(),
            NetFormatter.format(
                monthBytes,
                NetFormatter.FLAG_BYTE,
                NetFormatter.ACCURACY_EXACT
            ).splicing()
        )
    }

    /**
     * 系统版本>=S 且 targetVersion>=S 时，返回true
     */
    fun isSS(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                // https://developer.android.google.cn/about/versions/12/behavior-changes-12#custom-notifications
                context.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.S
    }

    fun createNotification(
        context: Context,
        configuration: NetSpeedConfiguration,
        rxSpeed: Long = 0L,
        txSpeed: Long = 0L
    ): Notification {
        val downloadSpeedStr: String =
            NetFormatter.format(rxSpeed, NetFormatter.FLAG_FULL, NetFormatter.ACCURACY_EXACT)
                .splicing()
        val uploadSpeedStr: String =
            NetFormatter.format(txSpeed, NetFormatter.FLAG_FULL, NetFormatter.ACCURACY_EXACT)
                .splicing()

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(context)
            Notification.Builder(context, CHANNEL_ID)
        } else {
            Notification.Builder(context)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setSound(null)
            //.setDefaults(Notification.DEFAULT_VIBRATE)
        }
        builder.setCategory(null)
            .setSmallIcon(createIcon(configuration, rxSpeed, txSpeed))
            .setColor(context.getColor(R.color.primaryColor))
            .setOnlyAlertOnce(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setBadgeIconType(Notification.BADGE_ICON_NONE)
                .setColorized(false)
        }

        if (configuration.hideLockNotification) {
            builder.setVisibility(Notification.VISIBILITY_SECRET)
        } else {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC)
        }

        var pendingFlag = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // https://developer.android.com/about/versions/12/behavior-changes-all#foreground-service-notification-delay
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
            // https://developer.android.com/about/versions/12/behavior-changes-12#pending-intent-mutability
            pendingFlag = PendingIntent.FLAG_MUTABLE
        }

        if (configuration.hideNotification && !isSS(context)) {
            val remoteViews = RemoteViews(context.packageName, R.layout.notification_empty_view)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // context.applicationInfo.targetSdkVersion < Build.VERSION_CODES.S
                // https://developer.android.google.cn/about/versions/12/behavior-changes-12#custom-notifications
                builder.setCustomContentView(remoteViews)
            } else {
                builder.setContent(remoteViews)
            }
        } else {
            val contentStr =
                context.getString(R.string.notify_net_speed_msg, uploadSpeedStr, downloadSpeedStr)
            builder.setContentTitle(contentStr)
            if (configuration.usage) {
                val usageText = getUsageText(context)
                builder.setContentText(usageText)
            }

            if (configuration.quickCloseable) {
                val closeAction = Intent(NetSpeedService.ACTION_CLOSE)
                    .toPendingBroadcast(context, pendingFlag)
                    .toNotificationAction(R.string.action_close)
                builder.addAction(closeAction)
            }
        }

        if (configuration.notifyClickable) {
            val pendingIntent = NavDeepLinkBuilder(context)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.netSpeed)
                .setComponentName(MainActivity::class.java)
                .createPendingIntent()
            builder.setContentIntent(pendingIntent)
        }

        return builder.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(context: Context) {
        val notificationManager = context.requireSystemService<NotificationManager>()
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.label_net_speed),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.desc_net_speed_notify)
            setShowBadge(false)
            enableVibration(false)
            enableLights(false)
            setSound(null, null)
            lockscreenVisibility = Notification.VISIBILITY_SECRET
        }
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun createIcon(
        configuration: NetSpeedConfiguration,
        rxSpeed: Long,
        txSpeed: Long
    ): Icon {
        val bitmap =
            NetTextIconFactory.createIconBitmap(rxSpeed, txSpeed, configuration)
        return Icon.createWithBitmap(bitmap)
    }

}