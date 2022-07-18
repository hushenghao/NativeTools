package com.dede.nativetools.netspeed.service

import android.app.KeyguardManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.navigation.NavDeepLinkBuilder
import com.dede.nativetools.R
import com.dede.nativetools.netspeed.NetSpeedConfiguration
import com.dede.nativetools.netspeed.notification.NotificationExtension
import com.dede.nativetools.netspeed.utils.NetFormatter
import com.dede.nativetools.netspeed.utils.NetTextIconFactory
import com.dede.nativetools.netusage.utils.NetUsageUtils
import com.dede.nativetools.util.*

/**
 * 网速通知
 */
object NetSpeedNotificationHelper {

    private const val CHANNEL_GROUP_ID = "net_speed_channel_group"

    private const val CHANNEL_ID_DEFAULT = "net_speed_channel_default"
    private const val CHANNEL_ID_SILENCE = "net_speed_channel_silence"

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
                Settings.EXTRA_CHANNEL_ID to CHANNEL_ID_DEFAULT
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
                Settings.EXTRA_CHANNEL_ID to CHANNEL_ID_DEFAULT
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
            val notificationChannel =
                notificationManagerCompat.getNotificationChannel(CHANNEL_ID_DEFAULT)
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

        var todayBytes: Long
        var monthBytes: Long
        val sb = StringBuilder()
        if (!configuration.enableWifiUsage && !configuration.enableMobileUsage) {
            // wifi和移动流量都关闭，显示全部
            todayBytes = NetUsageUtils.getNetUsageBytes(
                context,
                NetUsageUtils.TYPE_WIFI,
                NetUsageUtils.RANGE_TYPE_TODAY
            ) + NetUsageUtils.getNetUsageBytes(
                context,
                NetUsageUtils.TYPE_MOBILE,
                NetUsageUtils.RANGE_TYPE_TODAY
            )
            monthBytes = NetUsageUtils.getNetUsageBytes(
                context,
                NetUsageUtils.TYPE_WIFI,
                NetUsageUtils.RANGE_TYPE_MONTH
            ) + NetUsageUtils.getNetUsageBytes(
                context,
                NetUsageUtils.TYPE_MOBILE,
                NetUsageUtils.RANGE_TYPE_MONTH
            )
            sb.append(getUsageText(context, todayBytes, monthBytes))
            return sb.toString()
        }

        if (configuration.enableWifiUsage) {
            // wifi 流量
            sb.append("WLAN • ")
            todayBytes = NetUsageUtils.getNetUsageBytes(
                context,
                NetUsageUtils.TYPE_WIFI,
                NetUsageUtils.RANGE_TYPE_TODAY
            )
            monthBytes = NetUsageUtils.getNetUsageBytes(
                context,
                NetUsageUtils.TYPE_WIFI,
                NetUsageUtils.RANGE_TYPE_MONTH
            )
            sb.append(getUsageText(context, todayBytes, monthBytes))
        }

        if (!configuration.enableMobileUsage) {
            return sb.toString()
        }

        if (configuration.enableWifiUsage) {
            sb.appendLine()
        }
        // 移动流量
        var imsiSet: Set<String?>? = configuration.imsiSet
        if (imsiSet == null || imsiSet.isEmpty()) {
            imsiSet = setOf<String?>(null)
        }
        val size = imsiSet.size
        var index = 1
        for (imsi in imsiSet) {
            todayBytes = NetUsageUtils.getNetUsageBytes(
                context,
                NetUsageUtils.TYPE_MOBILE, NetUsageUtils.RANGE_TYPE_TODAY, imsi
            )
            monthBytes = NetUsageUtils.getNetUsageBytes(
                context,
                NetUsageUtils.TYPE_MOBILE, NetUsageUtils.RANGE_TYPE_MONTH, imsi
            )
            sb.append("SIM")
            if (size > 1) {
                sb.append(index)
            }
            sb.append(" • ")
                .append(getUsageText(context, todayBytes, monthBytes))
            if (index++ < size) {
                sb.appendLine()
            }
        }
        return sb.toString()
    }

    private fun getUsageText(context: Context, todayBytes: Long, monthBytes: Long): String {
        return context.getString(
            R.string.notify_net_speed_sub,
            NetFormatter.format(todayBytes, NetFormatter.FLAG_BYTE, NetFormatter.ACCURACY_EXACT)
                .splicing(),
            NetFormatter.format(monthBytes, NetFormatter.FLAG_BYTE, NetFormatter.ACCURACY_EXACT)
                .splicing()
        )
    }

    /**
     * 创建网速通知
     *
     * @param context 上下文
     * @param configuration 配置
     * @param rxSpeed 下行网速
     * @param txSpeed 上行网速
     */
    fun createNotification(
        context: Context,
        configuration: NetSpeedConfiguration,
        rxSpeed: Long = 0L,
        txSpeed: Long = 0L,
    ): Notification {

        createChannels(context)

        val builder = if (configuration.showBlankNotification) {
            // 显示透明图标，并降低通知优先级
            NotificationCompat.Builder(context, CHANNEL_ID_SILENCE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSmallIcon(createBlankIcon(configuration))
        } else {
            NotificationCompat.Builder(context, CHANNEL_ID_DEFAULT)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(createIconCompat(configuration, rxSpeed, txSpeed))
        }

        builder.setOnlyAlertOnce(false)
            .setOngoing(true)
            .setLocalOnly(true)
            .setShowWhen(false)
            .setCategory(null)
            .setSound(null)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
            .setColorized(false)

        if (configuration.hideLockNotification) {
            builder.setVisibility(NotificationCompat.VISIBILITY_SECRET)
        } else {
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }

        var pendingFlag = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // https://developer.android.com/about/versions/12/behavior-changes-all#foreground-service-notification-delay
            builder.foregroundServiceBehavior = NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
            // https://developer.android.com/about/versions/12/behavior-changes-12#pending-intent-mutability
            pendingFlag = pendingFlag or PendingIntent.FLAG_MUTABLE
        }

        val downloadSpeedStr: String =
            NetFormatter.format(rxSpeed, NetFormatter.FLAG_FULL, NetFormatter.ACCURACY_EXACT)
                .splicing()
        val uploadSpeedStr: String =
            NetFormatter.format(txSpeed, NetFormatter.FLAG_FULL, NetFormatter.ACCURACY_EXACT)
                .splicing()
        val contentStr =
            context.getString(R.string.notify_net_speed_msg, uploadSpeedStr, downloadSpeedStr)
        builder.setContentTitle(contentStr)

        if (configuration.usage) {
            val usageText = getUsageText(context, configuration)
            builder.setContentText(usageText)
            // big text
            if (usageText != null && usageText.lines().size > 1) {
                // 多行文字
                val bigTextStyle = NotificationCompat.BigTextStyle()
                    .setBigContentTitle(contentStr)
                    .bigText(usageText)
                builder.setStyle(bigTextStyle)
            }
        }

        if (configuration.quickCloseable) {
            val closePending = Intent(NetSpeedService.ACTION_CLOSE)
                .toPendingBroadcast(context, pendingFlag)
            builder.addAction(closePending.toNotificationCompatAction(R.string.action_close))
        }

        if (configuration.notifyClickable) {
            // 默认启动应用首页
            val pendingIntent = NavDeepLinkBuilder(context)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.netSpeed)
                .createPendingIntent()
            builder.setContentIntent(pendingIntent)
        }

        return NotificationExtension.build(builder)
    }

    private fun createChannels(context: Context) {
        val manager = NotificationManagerCompat.from(context)

        val channelGroup = NotificationChannelGroupCompat.Builder(CHANNEL_GROUP_ID)
            .setName(context.getString(R.string.label_net_speed_service))
            .setDescription(context.getString(R.string.desc_net_speed_notify))
            .build()
        manager.createNotificationChannelGroup(channelGroup)

        val channelSilence = context.createChannel(true)
        manager.createNotificationChannel(channelSilence)

        val channelDefault = context.createChannel(false)
        manager.createNotificationChannel(channelDefault)
    }

    private fun Context.createChannel(isSilence: Boolean): NotificationChannelCompat {
        val builder = if (isSilence) {
            NotificationChannelCompat.Builder(
                CHANNEL_ID_SILENCE,
                NotificationManagerCompat.IMPORTANCE_LOW
            ).setName(this.getString(R.string.label_net_speed_silence_channel))
        } else {
            NotificationChannelCompat.Builder(
                CHANNEL_ID_DEFAULT,
                NotificationManagerCompat.IMPORTANCE_DEFAULT
            ).setName(this.getString(R.string.label_net_speed_default_channel))
        }
        return builder.setDescription(this.getString(R.string.desc_net_speed_notify))
            .setShowBadge(false)
            .setGroup(CHANNEL_GROUP_ID)
            .setVibrationEnabled(false)
            .setLightsEnabled(false)
            .setSound(null, null)
            .build()
    }

    private fun createIconCompat(
        configuration: NetSpeedConfiguration,
        rxSpeed: Long,
        txSpeed: Long,
    ): IconCompat {
        val bitmap = NetTextIconFactory.create(rxSpeed, txSpeed, configuration)
        return IconCompat.createWithBitmap(bitmap)
    }

    private fun createBlankIcon(configuration: NetSpeedConfiguration): IconCompat {
        val bitmap = NetTextIconFactory.createBlank(configuration)
        return IconCompat.createWithBitmap(bitmap)
    }

}