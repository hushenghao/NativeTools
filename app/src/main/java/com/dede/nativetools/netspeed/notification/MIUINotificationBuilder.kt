package com.dede.nativetools.netspeed.notification

import android.app.Notification
import androidx.core.app.NotificationCompat
import com.dede.nativetools.util.declaredMethod
import com.dede.nativetools.util.field

/**
 * MIUI
 */
class MIUINotificationBuilder(private val builder: NotificationCompat.Builder) :
    NotificationExtension.Builder {

    override fun build(): Notification {
        val notification = builder.build()
        try {
            val miuiNotificationClass = Class.forName("android.app.MiuiNotification")

            val field = Notification::class.java.field("extraNotification")
            var miuiNotification = field.get(notification)
            if (miuiNotification == null) {
                miuiNotification = miuiNotificationClass.newInstance()
                field.set(notification, miuiNotification)
            }
            val method =
                miuiNotificationClass.declaredMethod("setCustomizedIcon", Boolean::class.java)
            method.invoke(miuiNotification, true)
        } catch (e: Exception) {
        }
        return notification
    }
}