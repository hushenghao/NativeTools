package com.dede.nativetools.netspeed

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.annotation.FloatRange
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize


/**
 * 网速指示器配置
 */
@Parcelize
data class NetSpeedConfiguration @JvmOverloads constructor(
    var interval: Int = NetSpeedPreferences.DEFAULT_INTERVAL,
    var isBold: Boolean = true,
    var notifyClickable: Boolean = true,
    var mode: String = MODE_DOWN,
    var quickCloseable: Boolean = false,
    var usage: Boolean = false,
    var hideNotification: Boolean = false,
    var hideLockNotification: Boolean = true,

    @FloatRange(from = -0.5, to = 0.5)
    var verticalOffset: Float = -0.04f,// Y轴偏移量
    @FloatRange(from = 0.0, to = 1.0)
    var relativeRatio: Float = 0.61f,// 相对比例
    @FloatRange(from = -0.5, to = 0.5)
    var relativeDistance: Float = 0.05f,// 相对距离
    @FloatRange(from = 0.5, to = 1.5)
    var textScale: Float = 1.11f// 字体缩放
) : Parcelable {

    @IgnoredOnParcel
    var cachedBitmap: Bitmap? = null

    fun reinitialize(): NetSpeedConfiguration {
        return this.updateFrom(initialize())
    }

    fun updateFrom(configuration: NetSpeedConfiguration): NetSpeedConfiguration {
        this.interval = configuration.interval
        this.isBold = configuration.isBold
        this.notifyClickable = configuration.notifyClickable
        this.mode = configuration.mode
        this.quickCloseable = configuration.quickCloseable
        this.usage = configuration.usage
        this.hideNotification = configuration.hideNotification
        this.hideLockNotification = configuration.hideLockNotification
        this.verticalOffset = configuration.verticalOffset
        this.relativeRatio = configuration.relativeRatio
        this.relativeDistance = configuration.relativeDistance
        this.textScale = configuration.textScale
        return this
    }

    fun updateOnPreferenceChanged(key: String) {
        when (key) {
            NetSpeedPreferences.KEY_NET_SPEED_INTERVAL -> {
                this.interval = NetSpeedPreferences.interval
            }
            NetSpeedPreferences.KEY_NET_SPEED_BOLD -> {
                this.isBold = NetSpeedPreferences.isBold
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
            NetSpeedPreferences.KEY_NET_SPEED_VERTICAL_OFFSET -> {
                this.verticalOffset = NetSpeedPreferences.verticalOffset
            }
            NetSpeedPreferences.KEY_NET_SPEED_RELATIVE_RATIO -> {
                this.relativeRatio = NetSpeedPreferences.relativeRatio
            }
            NetSpeedPreferences.KEY_NET_SPEED_RELATIVE_DISTANCE -> {
                this.relativeDistance = NetSpeedPreferences.relativeDistance
            }
            NetSpeedPreferences.KEY_NET_SPEED_TEXT_SCALE -> {
                this.textScale = NetSpeedPreferences.textScale
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
                NetSpeedPreferences.isBold,
                NetSpeedPreferences.notifyClickable,
                NetSpeedPreferences.mode,
                NetSpeedPreferences.quickCloseable,
                NetSpeedPreferences.usage,
                NetSpeedPreferences.hideNotification,
                NetSpeedPreferences.hideLockNotification,
                NetSpeedPreferences.verticalOffset,
                NetSpeedPreferences.relativeRatio,
                NetSpeedPreferences.relativeDistance,
                NetSpeedPreferences.textScale
            )
        }
    }

}