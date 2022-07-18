package com.dede.nativetools.netspeed.notification

import android.app.Notification
import androidx.core.app.NotificationCompat
import com.dede.nativetools.util.declaredMethod
import com.dede.nativetools.util.field
import com.dede.nativetools.util.getNullable

/**
 * Meizu Device Notification Ext
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

    private fun setInternalApp(flymeBuilder: Any?, int: Int): MeizuNotificationBuilder {
        if (flymeBuilder == null) return this
        flymeBuilder.javaClass.declaredMethod("setInternalApp", Int::class.java)
            .invoke(flymeBuilder, int)
        return this
    }

    override fun build(): Notification {
        try {
            val builderWrapper = NotificationCompatBuilderWrapper(builder)
            val notificationBuilder: Notification.Builder = builderWrapper.notificationBuilder
            val flymeBuilder: Any? = Notification.Builder::class.java
                .field("mFlymeNotificationBuilder")
                .getNullable(notificationBuilder)
            setInternalApp(flymeBuilder, 1)
            return builderWrapper.build()
        } catch (e: Exception) {
        }
        return builder.build()
    }

}