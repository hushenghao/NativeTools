package com.dede.nativetools.netspeed.notification

import android.app.Notification
import androidx.core.app.NotificationCompat
import com.dede.nativetools.util.Logic

/**
 * Created by shhu on 2022/7/15 13:55.
 *
 * Notification extension
 *
 * @since 2022/7/15
 */
object NotificationExtension {

    fun build(builder: NotificationCompat.Builder): Notification {
        if (Logic.isXiaomi()) {
            return MIUINotificationBuilder(builder).build()
        }
        if (Logic.isMeizu()) {
            return MeizuNotificationBuilder(builder).build()
        }
        return builder.build()
    }

    interface Builder {
        fun build(): Notification
    }
}