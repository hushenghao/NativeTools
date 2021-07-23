package com.dede.nativetools.netspeed

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import com.dede.nativetools.MainActivity
import com.dede.nativetools.R
import com.dede.nativetools.util.checkAppOps
import com.dede.nativetools.util.fromHtml
import com.dede.nativetools.util.safelyStartActivity
import com.dede.nativetools.util.splicing

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
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.safelyStartActivity(intent)
    }

    fun goNotificationSetting(context: Context) {
        val packageName = context.packageName
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                .putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_ID)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:$packageName"))
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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

    fun createNotification(
        context: Context,
        configuration: NetSpeedConfiguration,
        rxSpeed: Long = 0L,
        txSpeed: Long = 0L
    ): Notification {
        createChannel(context)

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
        val contentStr =
            context.getString(R.string.notify_net_speed_msg, downloadSpeedStr, uploadSpeedStr)

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
        } else {
            Notification.Builder(context)
                .setSound(null)
        }

        builder.setSubText(getRxSubText(context).fromHtml())
            .setContentText(contentStr)
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
            val closeBroadcast = PendingIntent.getBroadcast(
                context,
                0,
                Intent(NetSpeedService.ACTION_CLOSE).setPackage(context.packageName),
                pendingFlag
            )
            val closeAction =
                Notification.Action.Builder(
                    null,
                    context.getString(R.string.action_close),
                    closeBroadcast
                )
                    .build()
            builder.addAction(closeAction)
        }

        if (configuration.notifyClickable) {
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            val pendingIntent = PendingIntent.getActivity(
                context, 0,
                intent, pendingFlag
            )
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