package com.dede.nativetools.netspeed

import android.os.Parcelable
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
    var background: String,
    var usage: Boolean
) : Parcelable {

    constructor() : this(
        NetSpeedPreferences.DEFAULT_INTERVAL, true, false,
        MODE_DOWN, 1f, false, BACKGROUND_NONE, false
    )

    fun copy(configuration: NetSpeedConfiguration): NetSpeedConfiguration {
        this.interval = configuration.interval
        this.notifyClickable = configuration.notifyClickable
        this.compatibilityMode = configuration.compatibilityMode
        this.mode = configuration.mode
        this.scale = configuration.scale
        this.quickCloseable = configuration.quickCloseable
        this.background = configuration.background
        this.usage = configuration.usage
        return this
    }

    fun updateOnPreferenceChanged(key: String) {
        when (key) {
            NetSpeedPreferences.KEY_NET_SPEED_INTERVAL -> {
                this.interval = NetSpeedPreferences.interval
            }
            NetSpeedPreferences.KEY_NET_SPEED_COMPATIBILITY_MODE -> {
                this.compatibilityMode = NetSpeedPreferences.compatibilityMode
            }
            NetSpeedPreferences.KEY_NET_SPEED_NOTIFY_CLICKABLE -> {
                this.notifyClickable = NetSpeedPreferences.notifyClickable
            }
            NetSpeedPreferences.KEY_NET_SPEED_MODE -> {
                this.mode = NetSpeedPreferences.mode
            }
            NetSpeedPreferences.KEY_NET_SPEED_SCALE -> {
                this.scale = NetSpeedPreferences.scale
            }
            NetSpeedPreferences.KEY_NET_SPEED_QUICK_CLOSEABLE -> {
                this.quickCloseable = NetSpeedPreferences.quickCloseable
            }
            NetSpeedPreferences.KEY_NET_SPEED_BACKGROUND -> {
                this.background = NetSpeedPreferences.background
            }
            NetSpeedPreferences.KEY_NET_SPEED_USAGE -> {
                this.usage = NetSpeedPreferences.usage
            }
        }
    }

    companion object {

        val defaultConfiguration: NetSpeedConfiguration
            get() = NetSpeedConfiguration()

        const val MODE_DOWN = "0"
        const val MODE_ALL = "1"
        const val MODE_UP = "2"

        const val BACKGROUND_NONE = "0"
        const val BACKGROUND_CIRCLE = "1"
        const val BACKGROUND_ROUNDED_CORNERS = "2"
        const val BACKGROUND_SQUIRCLE = "3"

        fun initialize(): NetSpeedConfiguration {
            return NetSpeedConfiguration(
                NetSpeedPreferences.interval,
                NetSpeedPreferences.notifyClickable,
                NetSpeedPreferences.compatibilityMode,
                NetSpeedPreferences.mode,
                NetSpeedPreferences.scale,
                NetSpeedPreferences.quickCloseable,
                NetSpeedPreferences.background,
                NetSpeedPreferences.usage
            )
        }
    }

}