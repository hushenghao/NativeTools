package com.dede.nativetools.netspeed

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.annotation.FloatRange
import com.dede.nativetools.util.dataStore
import com.dede.nativetools.util.get
import com.dede.nativetools.util.globalContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.runBlocking
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
    var justMobileUsage: Boolean = false,
    var hideNotification: Boolean = false,
    var hideLockNotification: Boolean = true,
    var textStyle: Int = NetSpeedPreferences.DEFAULT_TEXT_STYLE,
    var font: String = NetSpeedPreferences.DEFAULT_FONT,
    var mode: String = MODE_DOWN,
    var hideThreshold: Long = 0,
    @FloatRange(from = -0.5, to = 0.5)
    var verticalOffset: Float = -0.05f,// Y轴偏移量
    @FloatRange(from = -0.5, to = 0.5)
    var horizontalOffset: Float = 0f,// X轴偏移量
    @FloatRange(from = 0.0, to = 1.0)
    var relativeRatio: Float = 0.6f,// 相对比例
    @FloatRange(from = -0.5, to = 0.5)
    var relativeDistance: Float = 0.15f,// 相对距离
    @FloatRange(from = 0.1, to = 1.5)
    var textScale: Float = 1f,// 字体缩放
    @FloatRange(from = 0.2, to = 1.3)
    var horizontalScale: Float = 1f,// X轴缩放
) : Parcelable {

    @IgnoredOnParcel
    var cachedBitmap: Bitmap? = null

    @IgnoredOnParcel
    var showBlankNotification: Boolean = false

    fun reinitialize(): NetSpeedConfiguration {
        return this.updateFrom(initialize())
    }

    fun updateFrom(configuration: NetSpeedConfiguration): NetSpeedConfiguration {
        this.interval = configuration.interval
        this.notifyClickable = configuration.notifyClickable
        this.quickCloseable = configuration.quickCloseable
        this.usage = configuration.usage
        this.justMobileUsage = configuration.justMobileUsage
        this.hideNotification = configuration.hideNotification
        this.hideLockNotification = configuration.hideLockNotification
        this.textStyle = configuration.textStyle
        this.font = configuration.font
        this.mode = configuration.mode
        this.hideThreshold = configuration.hideThreshold
        this.verticalOffset = configuration.verticalOffset
        this.horizontalOffset = configuration.horizontalOffset
        this.relativeRatio = configuration.relativeRatio
        this.relativeDistance = configuration.relativeDistance
        this.textScale = configuration.textScale
        this.horizontalScale = configuration.horizontalScale
        return this
    }

    companion object {

        val defaultConfiguration: NetSpeedConfiguration
            get() = NetSpeedConfiguration()

        const val MODE_DOWN = "0"
        const val MODE_ALL = "1"
        const val MODE_UP = "2"

        fun initialize(): NetSpeedConfiguration {
            return runBlocking {
                val dataStore = globalContext.dataStore
                val defaultConfiguration = NetSpeedConfiguration()
                dataStore.data.map {
                    val textStyle: Int = it.get(
                        NetSpeedPreferences.KEY_NET_SPEED_TEXT_STYLE,
                        defaultConfiguration.textStyle.toString()
                    ).toIntOrNull() ?: defaultConfiguration.textStyle

                    val font: String =
                        it.get(NetSpeedPreferences.KEY_NET_SPEED_FONT, defaultConfiguration.font)

                    val verticalOffset: Float = it.get(
                        NetSpeedPreferences.KEY_NET_SPEED_VERTICAL_OFFSET,
                        defaultConfiguration.verticalOffset
                    )

                    val horizontalOffset: Float = it.get(
                        NetSpeedPreferences.KEY_NET_SPEED_HORIZONTAL_OFFSET,
                        defaultConfiguration.horizontalOffset
                    )

                    val horizontalScale: Float = it.get(
                        NetSpeedPreferences.KEY_NET_SPEED_HORIZONTAL_SCALE,
                        defaultConfiguration.horizontalScale
                    )

                    val relativeRatio: Float = it.get(
                        NetSpeedPreferences.KEY_NET_SPEED_RELATIVE_RATIO,
                        defaultConfiguration.relativeRatio
                    )

                    val relativeDistance: Float = it.get(
                        NetSpeedPreferences.KEY_NET_SPEED_RELATIVE_DISTANCE,
                        defaultConfiguration.relativeDistance
                    )

                    val textScale: Float = it.get(
                        NetSpeedPreferences.KEY_NET_SPEED_TEXT_SCALE,
                        defaultConfiguration.textScale
                    )

                    val interval: Int = it.get(
                        NetSpeedPreferences.KEY_NET_SPEED_INTERVAL,
                        defaultConfiguration.interval.toString()
                    ).toIntOrNull() ?: defaultConfiguration.interval

                    val mode: String = it.get(
                        NetSpeedPreferences.KEY_NET_SPEED_MODE,
                        defaultConfiguration.mode
                    )

                    val notifyClickable: Boolean = it.get(
                        NetSpeedPreferences.KEY_NET_SPEED_NOTIFY_CLICKABLE,
                        defaultConfiguration.notifyClickable
                    )

                    val hideThreshold: Long = it.get(
                        NetSpeedPreferences.KEY_NET_SPEED_HIDE_THRESHOLD,
                        defaultConfiguration.hideThreshold.toString()
                    ).toLongOrNull() ?: defaultConfiguration.hideThreshold

                    val quickCloseable: Boolean = it.get(
                        NetSpeedPreferences.KEY_NET_SPEED_QUICK_CLOSEABLE,
                        defaultConfiguration.quickCloseable
                    )

                    val usage: Boolean = it.get(
                        NetSpeedPreferences.KEY_NET_SPEED_USAGE,
                        defaultConfiguration.usage
                    )

                    val justMobileUsage: Boolean = it.get(
                        NetSpeedPreferences.KEY_NET_SPEED_USAGE_JUST_MOBILE,
                        defaultConfiguration.justMobileUsage
                    )

                    val hideNotification: Boolean = it.get(
                        NetSpeedPreferences.KEY_NET_SPEED_HIDE_NOTIFICATION,
                        defaultConfiguration.hideNotification
                    )

                    val hideLockNotification: Boolean = it.get(
                        NetSpeedPreferences.KEY_NET_SPEED_HIDE_LOCK_NOTIFICATION,
                        defaultConfiguration.hideLockNotification
                    )
                    NetSpeedConfiguration(
                        interval = interval,
                        notifyClickable = notifyClickable,
                        quickCloseable = quickCloseable,
                        usage = usage,
                        justMobileUsage = justMobileUsage,
                        hideNotification = hideNotification,
                        hideLockNotification = hideLockNotification,
                        textStyle = textStyle,
                        font = font,
                        mode = mode,
                        hideThreshold = hideThreshold,
                        verticalOffset = verticalOffset,
                        horizontalOffset = horizontalOffset,
                        relativeRatio = relativeRatio,
                        relativeDistance = relativeDistance,
                        textScale = textScale,
                        horizontalScale = horizontalScale
                    )
                }.first()
            }
        }
    }

}