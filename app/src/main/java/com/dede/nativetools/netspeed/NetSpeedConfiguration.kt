package com.dede.nativetools.netspeed

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize


/**
 * 网速指示器配置
 */
@Parcelize
data class NetSpeedConfiguration constructor(
    var interval: Int,
    var notifyClickable: Boolean,
    var mode: String,
    var quickCloseable: Boolean,
    var usage: Boolean,
    var hideNotification: Boolean,
    var hideLockNotification: Boolean
) : Parcelable {

    @IgnoredOnParcel
    var cachedBitmap: Bitmap? = null

    constructor() : this(
        NetSpeedPreferences.DEFAULT_INTERVAL, true, MODE_DOWN,
        false, false, false, true
    )

    fun reinitialize(): NetSpeedConfiguration {
        return this.updateFrom(initialize())
    }

    fun updateFrom(configuration: NetSpeedConfiguration): NetSpeedConfiguration {
        this.interval = configuration.interval
        this.notifyClickable = configuration.notifyClickable
        this.mode = configuration.mode
        this.quickCloseable = configuration.quickCloseable
        this.usage = configuration.usage
        this.hideNotification = configuration.hideNotification
        this.hideLockNotification = configuration.hideLockNotification
        return this
    }

    fun updateOnPreferenceChanged(key: String) {
        when (key) {
            NetSpeedPreferences.KEY_NET_SPEED_INTERVAL -> {
                this.interval = NetSpeedPreferences.interval
            }
            NetSpeedPreferences.KEY_NET_SPEED_NOTIFY_CLICKABLE -> {
                this.notifyClickable = NetSpeedPreferences.notifyClickable
            }
            NetSpeedPreferences.KEY_NET_SPEED_MODE -> {
                this.mode = NetSpeedPreferences.mode
            }
            NetSpeedPreferences.KEY_NET_SPEED_QUICK_CLOSEABLE -> {
                this.quickCloseable = NetSpeedPreferences.quickCloseable
            }
            NetSpeedPreferences.KEY_NET_SPEED_USAGE -> {
                this.usage = NetSpeedPreferences.usage
            }
            NetSpeedPreferences.KEY_NET_SPEED_HIDE_NOTIFICATION -> {
                this.hideNotification = NetSpeedPreferences.hideNotification
            }
            NetSpeedPreferences.KEY_NET_SPEED_HIDE_LOCK_NOTIFICATION -> {
                this.hideLockNotification = NetSpeedPreferences.hideLockNotification
            }
        }
    }

    companion object {

        val defaultConfiguration: NetSpeedConfiguration
            get() = NetSpeedConfiguration()

        const val MODE_DOWN = "0"
        const val MODE_ALL = "1"
        const val MODE_UP = "2"

        fun initialize(): NetSpeedConfiguration {
            return NetSpeedConfiguration(
                NetSpeedPreferences.interval,
                NetSpeedPreferences.notifyClickable,
                NetSpeedPreferences.mode,
                NetSpeedPreferences.quickCloseable,
                NetSpeedPreferences.usage,
                NetSpeedPreferences.hideNotification,
                NetSpeedPreferences.hideLockNotification
            )
        }
    }

}