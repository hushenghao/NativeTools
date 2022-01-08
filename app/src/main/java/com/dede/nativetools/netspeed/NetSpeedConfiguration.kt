package com.dede.nativetools.netspeed

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.annotation.FloatRange
import com.dede.nativetools.netspeed.typeface.TypefaceGetter
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize


/**
 * 网速指示器配置
 */
@Parcelize
data class NetSpeedConfiguration @JvmOverloads constructor(
    var interval: Int = NetSpeedPreferences.DEFAULT_INTERVAL,
    var notifyClickable: Boolean = true,
    var quickCloseable: Boolean = false,
    var usage: Boolean = false,
    var hideNotification: Boolean = false,
    var hideLockNotification: Boolean = true,
    var textStyle: Int = NetSpeedPreferences.DEFAULT_TEXT_STYLE,
    var font: String = NetSpeedPreferences.DEFAULT_FONT,
    var mode: String = MODE_DOWN,
    @FloatRange(from = -0.5, to = 0.5)
    var verticalOffset: Float = -0.06f,// Y轴偏移量
    @FloatRange(from = -0.5, to = 0.5)
    var horizontalOffset:Float = 0f,// X轴偏移量
    @FloatRange(from = 0.0, to = 1.0)
    var relativeRatio: Float = 0.6f,// 相对比例
    @FloatRange(from = -0.5, to = 0.5)
    var relativeDistance: Float = 0.15f,// 相对距离
    @FloatRange(from = 0.5, to = 1.5)
    var textScale: Float = 0.87f// 字体缩放
) : Parcelable {

    @IgnoredOnParcel
    var cachedBitmap: Bitmap? = null

    fun reinitialize(): NetSpeedConfiguration {
        return this.updateFrom(initialize())
    }

    fun updateFrom(configuration: NetSpeedConfiguration): NetSpeedConfiguration {
        this.interval = configuration.interval
        this.notifyClickable = configuration.notifyClickable
        this.quickCloseable = configuration.quickCloseable
        this.usage = configuration.usage
        this.hideNotification = configuration.hideNotification
        this.hideLockNotification = configuration.hideLockNotification
        this.textStyle = configuration.textStyle
        this.font = configuration.font
        this.mode = configuration.mode
        this.verticalOffset = configuration.verticalOffset
        this.horizontalOffset = configuration.horizontalOffset
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
            NetSpeedPreferences.KEY_NET_SPEED_TEXT_STYLE -> {
                this.textStyle = NetSpeedPreferences.textStyle
            }
            NetSpeedPreferences.KEY_NET_SPEED_FONT -> {
                this.font = NetSpeedPreferences.font
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
            NetSpeedPreferences.KEY_NET_SPEED_HORIZONTAL_OFFSET -> {
                this.horizontalOffset = NetSpeedPreferences.horizontalOffset
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
                interval = NetSpeedPreferences.interval,
                notifyClickable = NetSpeedPreferences.notifyClickable,
                quickCloseable = NetSpeedPreferences.quickCloseable,
                usage = NetSpeedPreferences.usage,
                hideNotification = NetSpeedPreferences.hideNotification,
                hideLockNotification = NetSpeedPreferences.hideLockNotification,
                textStyle = NetSpeedPreferences.textStyle,
                font = NetSpeedPreferences.font,
                mode = NetSpeedPreferences.mode,
                verticalOffset = NetSpeedPreferences.verticalOffset,
                horizontalOffset = NetSpeedPreferences.horizontalOffset,
                relativeRatio = NetSpeedPreferences.relativeRatio,
                relativeDistance = NetSpeedPreferences.relativeDistance,
                textScale = NetSpeedPreferences.textScale
            )
        }
    }

}