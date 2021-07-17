package com.dede.nativetools.netspeed

import android.content.SharedPreferences
import android.os.Parcelable
import androidx.preference.PreferenceManager
import com.dede.nativetools.NativeToolsApp
import com.dede.nativetools.util.safeInt
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize


/**
 * 网速指示器配置
 */
@Parcelize
data class NetSpeedConfiguration(
    var interval: Int,
    var notifyClickable: Boolean,
    // 锁屏时隐藏(兼容模式)
    var compatibilityMode: Boolean,
    var mode: String,
    var scale: Float
) : Parcelable, SharedPreferences.OnSharedPreferenceChangeListener {

    constructor() : this(DEFAULT_INTERVAL, true, false, MODE_DOWN, DEFAULT_SCALE)

    fun copy(configuration: NetSpeedConfiguration): NetSpeedConfiguration {
        this.interval = configuration.interval
        this.notifyClickable = configuration.notifyClickable
        this.compatibilityMode = configuration.compatibilityMode
        this.mode = configuration.mode
        this.scale = configuration.scale
        return this
    }

    @IgnoredOnParcel
    var onSharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        when (key) {
            KEY_NET_SPEED_INTERVAL -> {
                val interval = preferences.getString(key, null).safeInt(DEFAULT_INTERVAL)
                this.interval = interval
            }
            KEY_NET_SPEED_COMPATIBILITY_MODE -> {
                val compatibilityMode = preferences.getBoolean(key, false)
                this.compatibilityMode = compatibilityMode
            }
            KEY_NET_SPEED_NOTIFY_CLICKABLE -> {
                val notifyClickable = preferences.getBoolean(key, false)
                this.notifyClickable = notifyClickable
            }
            KEY_NET_SPEED_MODE -> {
                val mode = preferences.getString(key, MODE_DOWN) ?: MODE_DOWN
                this.mode = mode
            }
            KEY_NET_SPEED_SCALE -> {
                val scaleInt = preferences.getInt(key, DEFAULT_SCALE_INT)
                val scale = scaleInt / SCALE_DIVISOR
                this.scale = scale
            }
        }
        onSharedPreferenceChangeListener?.onSharedPreferenceChanged(preferences, key)
    }

    companion object {

        val defaultSharedPreferences: SharedPreferences
            get() = PreferenceManager.getDefaultSharedPreferences(
                NativeToolsApp.getInstance()
            )

        const val KEY_NET_SPEED_STATUS = "net_speed_status"
        const val KEY_NET_SPEED_INTERVAL = "net_speed_interval"
        const val KEY_NET_SPEED_COMPATIBILITY_MODE = "net_speed_locked_hide"
        const val KEY_NET_SPEED_NOTIFY_CLICKABLE = "net_speed_notify_clickable"
        const val KEY_NET_SPEED_AUTO_START = "net_speed_auto_start"
        const val KEY_NET_SPEED_MODE = "net_speed_mode"
        const val KEY_NET_SPEED_SCALE = "net_speed_scale"
        const val KEY_OPS_DONT_ASK = "ops_dont_ask"

        const val DEFAULT_SCALE_INT = 100
        const val SCALE_DIVISOR = 100f

        const val DEFAULT_INTERVAL = 1000
        const val DEFAULT_SCALE = 1f

        const val MODE_DOWN = "0"
        const val MODE_ALL = "1"
        const val MODE_UP = "2"

        fun create(): NetSpeedConfiguration {
            val interval =
                defaultSharedPreferences.getString(KEY_NET_SPEED_INTERVAL, null)
                    .safeInt(DEFAULT_INTERVAL)
            val compatibilityMode =
                defaultSharedPreferences.getBoolean(KEY_NET_SPEED_COMPATIBILITY_MODE, false)
            val notifyClickable =
                defaultSharedPreferences.getBoolean(KEY_NET_SPEED_NOTIFY_CLICKABLE, true)
            val mode =
                defaultSharedPreferences.getString(KEY_NET_SPEED_MODE, MODE_DOWN) ?: MODE_DOWN
            val scaleInt = defaultSharedPreferences.getInt(
                KEY_NET_SPEED_SCALE,
                DEFAULT_SCALE_INT
            )
            return NetSpeedConfiguration(
                interval,
                notifyClickable,
                compatibilityMode,
                mode,
                scaleInt / SCALE_DIVISOR
            )
        }
    }

}