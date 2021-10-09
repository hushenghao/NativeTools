package com.dede.nativetools.netspeed

import com.dede.nativetools.netspeed.NetSpeedConfiguration.Companion.defaultConfiguration
import com.dede.nativetools.util.get
import com.dede.nativetools.util.globalPreferences
import com.dede.nativetools.util.safeInt
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
    const val KEY_NET_SPEED_AUTO_START = "net_speed_auto_start"
    const val KEY_NET_SPEED_MODE = "net_speed_mode"
    const val KEY_NET_SPEED_SCALE = "net_speed_scale"
    const val KEY_NET_SPEED_QUICK_CLOSEABLE = "net_speed_notify_quick_closeable"
    const val KEY_NET_SPEED_BACKGROUND = "net_speed_background"
    const val KEY_NET_SPEED_USAGE = "net_speed_usage"
    const val KEY_NIGHT_MODE_TOGGLE = "v28_night_mode_toggle"
    const val KEY_NET_SPEED_HIDE_LOCK_NOTIFICATION = "net_speed_locked_hide"
    const val KEY_NET_SPEED_HIDE_NOTIFICATION = "net_speed_hide_notification"

    //const val KEY_OPS_DONT_ASK = "ops_dont_ask"
    const val KEY_NOTIFICATION_DONT_ASK = "notification_dont_ask"

    const val DEFAULT_INTERVAL = 1000

    private const val DEFAULT_SCALE_INT = 100
    private const val SCALE_DIVISOR = 100f

    var status: Boolean
        get() = globalPreferences.get(KEY_NET_SPEED_STATUS, false)
        set(value) = globalPreferences.set(KEY_NET_SPEED_STATUS, value)

    val isNightMode: Boolean
        get() = globalPreferences.get(KEY_NIGHT_MODE_TOGGLE, false)

    val autoStart: Boolean
        get() = globalPreferences.get(KEY_NET_SPEED_AUTO_START, false)

    //var dontAskOps: Boolean
    //    get() = globalPreferences.get(KEY_OPS_DONT_ASK, false)
    //    set(value) = globalPreferences.set(KEY_OPS_DONT_ASK, value)

    var dontAskNotify: Boolean
        get() = globalPreferences.get(KEY_NOTIFICATION_DONT_ASK, false)
        set(value) = globalPreferences.set(KEY_NOTIFICATION_DONT_ASK, value)

    val interval: Int
        get() = globalPreferences.get(
            KEY_NET_SPEED_INTERVAL,
            DEFAULT_INTERVAL.toString()
        ).safeInt(defaultConfiguration.interval)

    val mode: String
        get() = globalPreferences.get(KEY_NET_SPEED_MODE, defaultConfiguration.mode)

    val background: String
        get() = globalPreferences.get(
            KEY_NET_SPEED_BACKGROUND,
            NetSpeedConfiguration.BACKGROUND_NONE
        )

    val scale: Float
        get() {
            val scaleInt = globalPreferences
                .get(KEY_NET_SPEED_SCALE, DEFAULT_SCALE_INT)
            return scaleInt / SCALE_DIVISOR
        }

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