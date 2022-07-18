package com.dede.nativetools.netspeed.notification

import android.app.Notification
import android.app.NotificationBuilderExt
import androidx.core.app.NotificationCompat
import com.dede.nativetools.util.field
import com.dede.nativetools.util.getNotnull

/**
 * Meizu Notification Ext
 *
 * [Meizu Open Platform](http://open-wiki.flyme.cn/doc-wiki/index#id?76)
 *
 * package android.app;
 *
 * class Notification$Builder {
 *    public NotificationBuilderExt mFlymeNotificationBuilder;
 * }
 *
 * class NotificationBuilderExt {
 *    setCircleProgressBar(boolean)
 *    setCircleProgressBarColor(int)
 *    setCircleProgressRimColor(int)
 *    setIconIntent(android.app.PendingIntent)
 *    setInternalApp(int)// 传1设置为内部app，显示状态栏通知图标
 *    setNotificationBitmapIcon(android.graphics.Bitmap)
 *    setNotificationIcon(int)
 *    setProgressBarDrawable(int)
 *    setRightIcon(int)
 *    setSimSlot(int)
 *    setSubTitle(java.lang.CharSequence)
 * }
 */
class MeizuNotificationBuilder(private val builder: NotificationCompat.Builder) :
    NotificationExtension.Builder {

    override fun build(): Notification {
        try {
            val builderWrapper = NotificationCompatBuilderWrapper(builder)
            val notificationBuilder: Notification.Builder = builderWrapper.notificationBuilder

            Notification.Builder::class.java
                .field("mFlymeNotificationBuilder")
                .getNotnull<NotificationBuilderExt>(notificationBuilder)
                .setInternalApp(1)
            return builderWrapper.build()
        } catch (e: Exception) {
        }
        return builder.build()
    }

}