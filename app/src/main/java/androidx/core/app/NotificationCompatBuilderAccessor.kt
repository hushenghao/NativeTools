package androidx.core.app

import android.annotation.SuppressLint
import android.app.Notification

/** [NotificationCompatBuilder] Accessor */
@SuppressLint("RestrictedApi")
class NotificationCompatBuilderAccessor(builder: NotificationCompat.Builder) :
    NotificationBuilderWithBuilderAccessor {

    private val access: NotificationCompatBuilder = NotificationCompatBuilder(builder)

    fun build(): Notification {
        return access.build()
    }

    override fun getBuilder(): Notification.Builder {
        return access.builder
    }
}
