package com.dede.nativetools.netspeed.service

import android.app.KeyguardManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.RemoteViews
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.navigation.NavDeepLinkBuilder
import com.dede.nativetools.R
import com.dede.nativetools.netspeed.NetSpeedConfiguration
import com.dede.nativetools.netspeed.utils.NetFormatter
import com.dede.nativetools.netspeed.utils.NetTextIconFactory
import com.dede.nativetools.netspeed.utils.NetworkUsageUtil
import com.dede.nativetools.util.*

/**
 * 网速通知
 */
object NetSpeedNotificationHelper {

    private const val CHANNEL_ID = "net_speed_2"

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
        val notificationManagerCompat = NotificationManagerCompat.from(context)
        val areNotificationsEnabled = notificationManagerCompat.areNotificationsEnabled()
        var channelDisabled = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = notificationManagerCompat.getNotificationChannel(CHANNEL_ID)
            if (notificationChannel != null) {
                val importance = notificationChannel.importance
                channelDisabled = importance <= NotificationManagerCompat.IMPORTANCE_MIN
            }
        }

        return areNotificationsEnabled && !channelDisabled
    }

    /**
     * 获取数据使用量
     */
    private fun getUsageText(context: Context, configuration: NetSpeedConfiguration): String? {
        if (!Logic.checkAppOps(context)) {
            return null
        }
        val todayBytes: Long
        val monthBytes: Long
        if (configuration.justMobileUsage) {
            todayBytes = NetworkUsageUtil.todayMobileUsageBytes(context)
            monthBytes = NetworkUsageUtil.monthMobileUsageBytes(context)
        } else {
            todayBytes = NetworkUsageUtil.todayNetworkUsageBytes(context)
            monthBytes = NetworkUsageUtil.monthNetworkUsageBytes(context)
        }
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
    fun itSSAbove(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                // https://developer.android.google.cn/about/versions/12/behavior-changes-12#custom-notifications
                context.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.S
    }

    fun createNotification(
        context: Context,
        configuration: NetSpeedConfiguration,
        rxSpeed: Long = 0L,
        txSpeed: Long = 0L,
    ): Notification {
        val downloadSpeedStr: String =
            NetFormatter.format(rxSpeed, NetFormatter.FLAG_FULL, NetFormatter.ACCURACY_EXACT)
                .splicing()
        val uploadSpeedStr: String =
            NetFormatter.format(txSpeed, NetFormatter.FLAG_FULL, NetFormatter.ACCURACY_EXACT)
                .splicing()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setOnlyAlertOnce(false)
            .setOngoing(true)
            .setLocalOnly(true)
            .setShowWhen(false)
            .setCategory(null)
            .setSound(null)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
            .setColorized(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(createIconCompat(configuration, rxSpeed, txSpeed))

        createChannel(context)

        if (configuration.hideLockNotification) {
            builder.setVisibility(NotificationCompat.VISIBILITY_SECRET)
        } else {
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }

        var pendingFlag = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // https://developer.android.com/about/versions/12/behavior-changes-all#foreground-service-notification-delay
            @Suppress("WrongConstant")
            builder.foregroundServiceBehavior = NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
            // https://developer.android.com/about/versions/12/behavior-changes-12#pending-intent-mutability
            pendingFlag = PendingIntent.FLAG_MUTABLE
        }

        if (configuration.hideNotification && !itSSAbove(context)) {
            // context.applicationInfo.targetSdkVersion < Build.VERSION_CODES.S
            // https://developer.android.google.cn/about/versions/12/behavior-changes-12#custom-notifications
            val remoteViews = RemoteViews(context.packageName, R.layout.notification_empty_view)
            builder.setCustomContentView(remoteViews)
        } else {
            val contentStr =
                context.getString(R.string.notify_net_speed_msg, uploadSpeedStr, downloadSpeedStr)
            builder.setContentTitle(contentStr)
            if (configuration.usage) {
                val usageText = getUsageText(context, configuration)
                builder.setContentText(usageText)
            }

            if (configuration.quickCloseable) {
                val closePending = Intent(NetSpeedService.ACTION_CLOSE)
                    .toPendingBroadcast(context, pendingFlag)
                builder.addAction(closePending.toNotificationCompatAction(R.string.action_close))
            }
        }

        if (configuration.notifyClickable) {
            // 默认启动应用首页
            val pendingIntent = NavDeepLinkBuilder(context)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.netSpeed)
                .createPendingIntent()
            builder.setContentIntent(pendingIntent)
        }

        return builder.build()
    }

    private fun createChannel(context: Context) {
        val channel =
            NotificationChannelCompat.Builder(
                CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_DEFAULT// 只允许降级
            )
                .setName(context.getString(R.string.label_net_speed))
                .setDescription(context.getString(R.string.desc_net_speed_notify))
                .setShowBadge(false)
                .setVibrationEnabled(false)
                .setLightsEnabled(false)
                .setSound(null, null)
                .build()
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }

    private fun createIconCompat(
        configuration: NetSpeedConfiguration,
        rxSpeed: Long,
        txSpeed: Long,
    ): IconCompat {
        val bitmap = NetTextIconFactory.create(rxSpeed, txSpeed, configuration)
        return IconCompat.createWithBitmap(bitmap)
    }

}