package com.dede.nativetools.netspeed

import android.content.SharedPreferences
import android.os.Parcelable
import androidx.preference.PreferenceManager
import com.dede.nativetools.NativeToolsApp
import com.dede.nativetools.util.defaultSharedPreferences
import com.dede.nativetools.util.getStringNotNull
import com.dede.nativetools.util.safeInt
import kotlinx.parcelize.Parcelize


/**
 * 网速指示器配置
 */
@Parcelize
data class NetSpeedConfiguration constructor(
    var interval: Int,
    var notifyClickable: Boolean,
    // 锁屏时隐藏(兼容模式)
    var compatibilityMode: Boolean,
    var mode: String,
    var scale: Float,
    var quickCloseable: Boolean,
    var background: String
) : Parcelable {

    constructor() : this(
        DEFAULT_INTERVAL, true, false,
        MODE_DOWN, 1f, false, BACKGROUND_NONE
    )

    fun copy(configuration: NetSpeedConfiguration): NetSpeedConfiguration {
        this.interval = configuration.interval
        this.notifyClickable = configuration.notifyClickable
        this.compatibilityMode = configuration.compatibilityMode
        this.mode = configuration.mode
        this.scale = configuration.scale
        this.quickCloseable = configuration.quickCloseable
        this.background = configuration.background
        return this
    }

    // @IgnoredOnParcel
    // var onSharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    fun updateOnSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        when (key) {
            KEY_NET_SPEED_INTERVAL -> {
                this.interval = preferences.getInterval()
            }
            KEY_NET_SPEED_COMPATIBILITY_MODE -> {
                this.compatibilityMode =
                    preferences.getBoolean(key, defaultConfiguration.compatibilityMode)
            }
            KEY_NET_SPEED_NOTIFY_CLICKABLE -> {
                this.notifyClickable =
                    preferences.getBoolean(key, defaultConfiguration.notifyClickable)
            }
            KEY_NET_SPEED_MODE -> {
                this.mode = preferences.getMode()
            }
            KEY_NET_SPEED_SCALE -> {
                this.scale = preferences.getScale()
            }
            KEY_NET_SPEED_QUICK_CLOSEABLE -> {
                this.quickCloseable =
                    preferences.getBoolean(key, defaultConfiguration.quickCloseable)
            }
            KEY_NET_SPEED_BACKGROUND -> {
                this.background = preferences.getBackground()
            }
        }
        // onSharedPreferenceChangeListener?.onSharedPreferenceChanged(preferences, key)
    }

    companion object {

        val defaultConfiguration: NetSpeedConfiguration
            get() = NetSpeedConfiguration()

        const val KEY_NET_SPEED_STATUS = "net_speed_status"
        const val KEY_NET_SPEED_INTERVAL = "net_speed_interval"
        const val KEY_NET_SPEED_COMPATIBILITY_MODE = "net_speed_locked_hide"
        const val KEY_NET_SPEED_NOTIFY_CLICKABLE = "net_speed_notify_clickable"
        const val KEY_NET_SPEED_AUTO_START = "net_speed_auto_start"
        const val KEY_NET_SPEED_MODE = "net_speed_mode"
        const val KEY_NET_SPEED_SCALE = "net_speed_scale"
        const val KEY_NET_SPEED_QUICK_CLOSEABLE = "net_speed_notify_quick_closeable"
        const val KEY_NET_SPEED_BACKGROUND = "net_speed_background"
        const val KEY_OPS_DONT_ASK = "ops_dont_ask"
        const val KEY_NOTIFICATION_DONT_ASK = "notification_dont_ask"

        private const val DEFAULT_SCALE_INT = 100
        private const val SCALE_DIVISOR = 100f

        const val DEFAULT_INTERVAL = 1000

        const val MODE_DOWN = "0"
        const val MODE_ALL = "1"
        const val MODE_UP = "2"

        const val BACKGROUND_NONE = "0"
        const val BACKGROUND_CIRCLE = "1"
        const val BACKGROUND_ROUNDED_CORNERS = "2"
        const val BACKGROUND_SQUIRCLE = "3"

        fun SharedPreferences.getScale(): Float {
            val scaleInt = this.getInt(
                KEY_NET_SPEED_SCALE,
                DEFAULT_SCALE_INT
            )
            return scaleInt / SCALE_DIVISOR
        }

        fun SharedPreferences.getInterval(): Int {
            return this.getString(KEY_NET_SPEED_INTERVAL, null)
                .safeInt(defaultConfiguration.interval)
        }

        fun SharedPreferences.getMode(): String {
            return this.getStringNotNull(KEY_NET_SPEED_MODE, MODE_DOWN)
        }

        fun SharedPreferences.getBackground(): String {
            return this.getStringNotNull(KEY_NET_SPEED_BACKGROUND, BACKGROUND_NONE)
        }

        fun initialize(): NetSpeedConfiguration {
            val interval = defaultSharedPreferences.getInterval()
            val compatibilityMode =
                defaultSharedPreferences.getBoolean(
                    KEY_NET_SPEED_COMPATIBILITY_MODE,
                    defaultConfiguration.compatibilityMode
                )
            val notifyClickable =
                defaultSharedPreferences.getBoolean(
                    KEY_NET_SPEED_NOTIFY_CLICKABLE,
                    defaultConfiguration.notifyClickable
                )
            val mode = defaultSharedPreferences.getMode()
            val scale = defaultSharedPreferences.getScale()
            val quickCloseable =
                defaultSharedPreferences.getBoolean(
                    KEY_NET_SPEED_QUICK_CLOSEABLE,
                    defaultConfiguration.quickCloseable
                )
            val background = defaultSharedPreferences.getBackground()
            return NetSpeedConfiguration(
                interval,
                notifyClickable,
                compatibilityMode,
                mode,
                scale,
                quickCloseable,
                background
            )
        }
    }

}