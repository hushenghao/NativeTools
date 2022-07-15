package com.dede.nativetools.netspeed.notification

import android.app.Notification
import androidx.core.app.NotificationCompat
import com.dede.nativetools.util.*

/**
 * Meizu Device
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
class MeizuNotificationBuilderExt(private val builder: NotificationCompat.Builder) {

    // androidx.core.app.NotificationCompatBuilder
    private var compatBuilder: Any? = null

    // android.app.NotificationBuilderExt
    private var flymeBuilder: Any? = null

    init {
        try {
            val compatBuilder = createNotificationCompatBuilder(builder)
            this.compatBuilder = compatBuilder
            val nativeBuilder: Notification.Builder = getNotificationBuilder(compatBuilder)
            val flymeField = Notification.Builder::class.java.field("mFlymeNotificationBuilder")
            this.flymeBuilder = flymeField.get(nativeBuilder)
        } catch (e: Exception) {
        }
    }

    private fun setInternalApp(int: Int): MeizuNotificationBuilderExt {
        val flymeBuilder = flymeBuilder ?: return this
        val method =
            flymeBuilder.javaClass.declaredMethod("setInternalApp", Int::class.java)
        method.invoke(flymeBuilder, int)
        return this
    }

    fun build(): Notification {
        val compatBuilder = this.compatBuilder
        if (compatBuilder != null) {
            try {
                return setInternalApp(1).build(compatBuilder)
            } catch (e: Exception) {
            }
        }
        return builder.build()
    }

    private fun build(builder: Any /* NotificationCompatBuilder */): Notification {
        val clazz = Class.forName("androidx.core.app.NotificationCompatBuilder")
        val build = clazz.method("build")
        return build.invoke(builder) as Notification
    }

    private fun createNotificationCompatBuilder(builder: NotificationCompat.Builder): Any /* NotificationCompatBuilder */ {
        val clazz = Class.forName("androidx.core.app.NotificationCompatBuilder")
        val constructor =
            clazz.declaredConstructor(NotificationCompat.Builder::class.java)
        return constructor.newInstance(builder)
    }

    private fun getNotificationBuilder(builder: Any /* NotificationCompatBuilder */): Notification.Builder {
        val clazz = Class.forName("androidx.core.app.NotificationCompatBuilder")
        val field = clazz.declaredField("mBuilder")
        return field.get(builder) as Notification.Builder
    }
}