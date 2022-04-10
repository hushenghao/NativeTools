package com.dede.nativetools.netspeed

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.annotation.FloatRange
import androidx.datastore.preferences.core.Preferences
import com.dede.nativetools.NativeToolsApp
import com.dede.nativetools.netusage.NetUsageConfigs
import com.dede.nativetools.util.get
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
    var enableWifiUsage: Boolean = true,
    var enableMobileUsage: Boolean = true,
    var hideNotification: Boolean = false,
    var hideLockNotification: Boolean = true,
    var textStyle: Int = NetSpeedPreferences.DEFAULT_TEXT_STYLE,
    var font: String = NetSpeedPreferences.DEFAULT_FONT,
    var mode: String = NetSpeedPreferences.MODE_DOWN,
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
    var imsiSet: Set<String>? = null// 配置的IMSI
) : Parcelable {

    @IgnoredOnParcel
    var cachedBitmap: Bitmap? = null

    @IgnoredOnParcel
    var showBlankNotification: Boolean = false

    fun updateFrom(configuration: NetSpeedConfiguration): NetSpeedConfiguration {
        this.interval = configuration.interval
        this.notifyClickable = configuration.notifyClickable
        this.quickCloseable = configuration.quickCloseable
        this.usage = configuration.usage
        this.enableWifiUsage = configuration.enableWifiUsage
        this.enableMobileUsage = configuration.enableMobileUsage
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
        this.imsiSet = configuration.imsiSet
        return this
    }

    fun updateImsi(imsiSet: Set<String>?): NetSpeedConfiguration {
        this.imsiSet = imsiSet
        return this
    }

    fun updateFrom(preferences: Preferences): NetSpeedConfiguration {
        val defaultConfiguration = NetSpeedConfiguration()

        this.textStyle = preferences.get(
            NetSpeedPreferences.KEY_NET_SPEED_TEXT_STYLE,
            defaultConfiguration.textStyle.toString()
        ).toIntOrNull() ?: defaultConfiguration.textStyle

        this.font =
            preferences.get(NetSpeedPreferences.KEY_NET_SPEED_FONT, defaultConfiguration.font)

        this.verticalOffset = preferences.get(
            NetSpeedPreferences.KEY_NET_SPEED_VERTICAL_OFFSET,
            defaultConfiguration.verticalOffset
        )

        this.horizontalOffset = preferences.get(
            NetSpeedPreferences.KEY_NET_SPEED_HORIZONTAL_OFFSET,
            defaultConfiguration.horizontalOffset
        )

        this.horizontalScale = preferences.get(
            NetSpeedPreferences.KEY_NET_SPEED_HORIZONTAL_SCALE,
            defaultConfiguration.horizontalScale
        )

        this.relativeRatio = preferences.get(
            NetSpeedPreferences.KEY_NET_SPEED_RELATIVE_RATIO,
            defaultConfiguration.relativeRatio
        )

        this.relativeDistance = preferences.get(
            NetSpeedPreferences.KEY_NET_SPEED_RELATIVE_DISTANCE,
            defaultConfiguration.relativeDistance
        )

        this.textScale = preferences.get(
            NetSpeedPreferences.KEY_NET_SPEED_TEXT_SCALE,
            defaultConfiguration.textScale
        )

        this.interval = preferences.get(
            NetSpeedPreferences.KEY_NET_SPEED_INTERVAL,
            defaultConfiguration.interval.toString()
        ).toIntOrNull() ?: defaultConfiguration.interval

        this.mode = preferences.get(
            NetSpeedPreferences.KEY_NET_SPEED_MODE,
            defaultConfiguration.mode
        )

        this.notifyClickable = preferences.get(
            NetSpeedPreferences.KEY_NET_SPEED_NOTIFY_CLICKABLE,
            defaultConfiguration.notifyClickable
        )

        this.hideThreshold = preferences.get(
            NetSpeedPreferences.KEY_NET_SPEED_HIDE_THRESHOLD,
            defaultConfiguration.hideThreshold.toString()
        ).toLongOrNull() ?: defaultConfiguration.hideThreshold

        this.quickCloseable = preferences.get(
            NetSpeedPreferences.KEY_NET_SPEED_QUICK_CLOSEABLE,
            defaultConfiguration.quickCloseable
        )

        this.usage = preferences.get(
            NetSpeedPreferences.KEY_NET_SPEED_USAGE,
            defaultConfiguration.usage
        )

        this.enableWifiUsage = preferences.get(
            NetUsageConfigs.KEY_NET_USAGE_WIFI,
            defaultConfiguration.enableWifiUsage
        )

        this.enableMobileUsage = preferences.get(
            NetUsageConfigs.KEY_NET_USAGE_MOBILE,
            defaultConfiguration.enableMobileUsage
        )

        this.hideNotification = preferences.get(
            NetSpeedPreferences.KEY_NET_SPEED_HIDE_NOTIFICATION,
            defaultConfiguration.hideNotification
        )

        this.hideLockNotification = preferences.get(
            NetSpeedPreferences.KEY_NET_SPEED_HIDE_LOCK_NOTIFICATION,
            defaultConfiguration.hideLockNotification
        )

        // 获取已经启用的imsi
        this.imsiSet = NetUsageConfigs(NativeToolsApp.getInstance()).getEnabledIMSI()

        return this
    }

}