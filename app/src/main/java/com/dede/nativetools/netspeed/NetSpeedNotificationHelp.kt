package com.dede.nativetools.netspeed

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import com.dede.nativetools.R
import com.dede.nativetools.netspeed.utils.NetFormater
import com.dede.nativetools.netspeed.utils.NetworkUsageUtil
import com.dede.nativetools.ui.MainActivity
import com.dede.nativetools.util.*

/**
 * 网速通知
 */
object NetSpeedNotificationHelp {

    private const val CHANNEL_ID = "net_speed"

    fun goSystemNotification(context: Context) {
        // ConfigureNotificationSettings
        // ShowOnLockScreenNotificationPreferenceController
        val intent = Intent("android.settings.NOTIFICATION_SETTINGS")
            //.putExtra(":settings:fragment_args_key", "configure_notifications_lock")
            .newTask()
        context.safelyStartActivity(intent)
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
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData("package:$packageName")
        }
        intent.newTask()
        context.safelyStartActivity(intent)
    }

    fun areNotificationEnabled(context: Context): Boolean {
        val areNotificationsEnabled =
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        val notificationManager =
            context.getSystemService<NotificationManager>() ?: return areNotificationsEnabled
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
            NetFormater.formatBytes(todayBytes, NetFormater.FLAG_BYTE, NetFormater.ACCURACY_EXACT)
                .splicing(),
            NetFormater.formatBytes(monthBytes, NetFormater.FLAG_BYTE, NetFormater.ACCURACY_EXACT)
                .splicing()
        )
    }

    fun createNotification(
        context: Context,
        configuration: NetSpeedConfiguration,
        rxSpeed: Long = 0L,
        txSpeed: Long = 0L
    ): Notification {
        createChannel(context)

        val downloadSpeedStr: String =
            NetFormater.formatBytes(rxSpeed, NetFormater.FLAG_FULL, NetFormater.ACCURACY_EXACT)
                .splicing()
        val uploadSpeedStr: String =
            NetFormater.formatBytes(txSpeed, NetFormater.FLAG_FULL, NetFormater.ACCURACY_EXACT)
                .splicing()
        val contentStr =
            context.getString(R.string.notify_net_speed_msg, downloadSpeedStr, uploadSpeedStr)

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
        } else {
            Notification.Builder(context)
                .setSound(null)
        }

        if (configuration.usage) {
            val usageText = getUsageText(context)
            builder.setContentText(usageText)
        }
        builder.setContentTitle(contentStr)
            .setAutoCancel(false)
            .setVisibility(Notification.VISIBILITY_SECRET)
            .setSmallIcon(createIcon(configuration, rxSpeed, txSpeed))
            .setCategory(null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setBadgeIconType(Notification.BADGE_ICON_NONE)
        }

        var pendingFlag = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // support Android S
            // https://developer.android.com/about/versions/12/behavior-changes-all#foreground-service-notification-delay
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
            // https://developer.android.com/about/versions/12/behavior-changes-12#pending-intent-mutability
            pendingFlag = PendingIntent.FLAG_MUTABLE
        }

        if (configuration.quickCloseable) {
            val closeAction = Intent(NetSpeedService.ACTION_CLOSE)
                .toPendingBroadcast(context, pendingFlag)
                .toNotificationAction(R.string.action_close)
            builder.addAction(closeAction)
        }

        if (configuration.notifyClickable) {
            val pendingIntent = Intent<MainActivity>(context)
                .newClearTask()
                .toPendingActivity(context, pendingFlag)
            builder.setContentIntent(pendingIntent)
        }

        return builder.build()
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val notificationManager = context.getSystemService<NotificationManager>() ?: return
        val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
        if (channel != null) {
            return
        }

        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.label_net_speed),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.desc_net_speed_notify)
            setShowBadge(false)
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
            NetTextIconFactory.createIconBitmap(rxSpeed, txSpeed, configuration, fromCache = true)
        return Icon.createWithBitmap(bitmap)
    }

}