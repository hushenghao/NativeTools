package com.dede.nativetools.netspeed

import com.dede.nativetools.netspeed.NetSpeedConfiguration.Companion.defaultConfiguration
import com.dede.nativetools.util.get
import com.dede.nativetools.util.globalPreferences
import com.dede.nativetools.util.set

/**
 * NetSpeed配置
 *
 * @author hsh
 * @since 2021/8/10 2:15 下午
 */
object NetSpeedPreferences {

    const val KEY_NET_SPEED_STATUS = "net_speed_status"
    const val KEY_NET_SPEED_INTERVAL = "net_speed_interval"
    const val KEY_NET_SPEED_NOTIFY_CLICKABLE = "net_speed_notify_clickable"
    const val KEY_NET_SPEED_MODE = "net_speed_mode"
    const val KEY_NET_SPEED_QUICK_CLOSEABLE = "net_speed_notify_quick_closeable"
    const val KEY_NET_SPEED_USAGE = "net_speed_usage"
    const val KEY_NET_SPEED_HIDE_LOCK_NOTIFICATION = "net_speed_locked_hide"
    const val KEY_NET_SPEED_HIDE_NOTIFICATION = "net_speed_hide_notification"
    const val KEY_NET_SPEED_BOLD = "net_speed_bold"

    private const val KEY_NET_SPEED_AUTO_START = "net_speed_auto_start"
    private const val KEY_NOTIFICATION_DONT_ASK = "notification_dont_ask"

    const val DEFAULT_INTERVAL = 1000

    var status: Boolean
        get() = globalPreferences.get(KEY_NET_SPEED_STATUS, false)
        set(value) = globalPreferences.set(KEY_NET_SPEED_STATUS, value)

    val isBold: Boolean
        get() = globalPreferences.get(KEY_NET_SPEED_BOLD, true)

    val autoStart: Boolean
        get() = globalPreferences.get(KEY_NET_SPEED_AUTO_START, false)

    var dontAskNotify: Boolean
        get() = globalPreferences.get(KEY_NOTIFICATION_DONT_ASK, false)
        set(value) = globalPreferences.set(KEY_NOTIFICATION_DONT_ASK, value)

    val interval: Int
        get() = globalPreferences.get(
            KEY_NET_SPEED_INTERVAL,
            DEFAULT_INTERVAL.toString()
        ).toIntOrNull() ?: defaultConfiguration.interval

    val mode: String
        get() = globalPreferences.get(KEY_NET_SPEED_MODE, defaultConfiguration.mode)

    val notifyClickable: Boolean
        get() = globalPreferences.get(
            KEY_NET_SPEED_NOTIFY_CLICKABLE,
            defaultConfiguration.notifyClickable
        )

    val quickCloseable: Boolean
        get() = globalPreferences.get(
            KEY_NET_SPEED_QUICK_CLOSEABLE,
            defaultConfiguration.quickCloseable
        )

    var usage: Boolean
        get() = globalPreferences.get(KEY_NET_SPEED_USAGE, defaultConfiguration.usage)
        set(value) = globalPreferences.set(KEY_NET_SPEED_USAGE, value)

    val hideNotification: Boolean
        get() = globalPreferences.get(
            KEY_NET_SPEED_HIDE_NOTIFICATION,
            defaultConfiguration.hideNotification
        )

    val hideLockNotification: Boolean
        get() = globalPreferences.get(
            KEY_NET_SPEED_HIDE_LOCK_NOTIFICATION,
            defaultConfiguration.hideLockNotification
        )

}