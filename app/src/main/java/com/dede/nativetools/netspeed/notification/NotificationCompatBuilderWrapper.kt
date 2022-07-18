package com.dede.nativetools.netspeed.notification

import android.annotation.SuppressLint
import android.app.Notification
import androidx.core.app.NotificationBuilderWithBuilderAccessor
import androidx.core.app.NotificationCompat
import com.dede.nativetools.util.declaredConstructor
import com.dede.nativetools.util.invokeWithReturn
import com.dede.nativetools.util.method
import java.lang.reflect.InvocationTargetException

/**
 * Wrapper [androidx.core.app.NotificationCompatBuilder] obj
 *
 * @since 2022/7/18
 */
@SuppressLint("RestrictedApi")
class NotificationCompatBuilderWrapper
@Throws(
    ClassNotFoundException::class,
    NoSuchFieldException::class,
    SecurityException::class,
    IllegalArgumentException::class,
    IllegalAccessException::class,
    InvocationTargetException::class)
constructor(private val builder: NotificationCompat.Builder) : NotificationExtension.Builder,
    NotificationBuilderWithBuilderAccessor {

    // androidx.core.app.NotificationCompatBuilder
    private val obj: NotificationBuilderWithBuilderAccessor =
        createNotificationCompatBuilder(builder)

    override fun build(): Notification {
        try {
            return build(obj)
        } catch (e: Exception) {
        }
        return builder.build()
    }

    private fun build(builder: Any /* NotificationCompatBuilder */): Notification {
        return Class.forName("androidx.core.app.NotificationCompatBuilder")
            .method("build")
            .invokeWithReturn(builder)
    }

    /* NotificationCompatBuilder */
    private fun createNotificationCompatBuilder(builder: NotificationCompat.Builder): NotificationBuilderWithBuilderAccessor {
        return Class.forName("androidx.core.app.NotificationCompatBuilder")
            .declaredConstructor<NotificationBuilderWithBuilderAccessor>(NotificationCompat.Builder::class.java)
            .newInstance(builder)
    }

    /**
     * 系统通知构建器
     */
    override fun getBuilder(): Notification.Builder {
        return this.obj.builder
    }
}