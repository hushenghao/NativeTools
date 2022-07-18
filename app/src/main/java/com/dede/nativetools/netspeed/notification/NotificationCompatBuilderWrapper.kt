package com.dede.nativetools.netspeed.notification

import android.app.Notification
import androidx.core.app.NotificationCompat
import com.dede.nativetools.util.*
import java.lang.reflect.InvocationTargetException

/**
 * Wrapper [androidx.core.app.NotificationCompatBuilder] obj
 *
 * @since 2022/7/18
 */

class NotificationCompatBuilderWrapper
@Throws(
    ClassNotFoundException::class,
    NoSuchFieldException::class,
    SecurityException::class,
    IllegalArgumentException::class,
    IllegalAccessException::class,
    InvocationTargetException::class)
constructor(private val builder: NotificationCompat.Builder) {

    /**
     * 系统通知构建器
     */
    var notificationBuilder: Notification.Builder
        private set

    // androidx.core.app.NotificationCompatBuilder
    private var content: Any

    init {
        this.content = createNotificationCompatBuilder(builder)
        this.notificationBuilder = getNotificationBuilder(content)
    }

    fun build(): Notification {
        try {
            return build(content)
        } catch (e: Exception) {
        }
        return builder.build()
    }

    private fun build(builder: Any /* NotificationCompatBuilder */): Notification {
        return Class.forName("androidx.core.app.NotificationCompatBuilder")
            .method("build")
            .invokeWithReturn(builder)
    }

    private fun getNotificationBuilder(builder: Any /* NotificationCompatBuilder */): Notification.Builder {
        return Class.forName("androidx.core.app.NotificationCompatBuilder")
            .declaredField("mBuilder")
            .getNotnull(builder)
    }

    private fun createNotificationCompatBuilder(builder: NotificationCompat.Builder): Any /* NotificationCompatBuilder */ {
        return Class.forName("androidx.core.app.NotificationCompatBuilder")
            .declaredConstructor(NotificationCompat.Builder::class.java)
            .newInstance(builder)
    }
}