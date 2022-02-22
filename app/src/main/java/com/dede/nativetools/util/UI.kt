package com.dede.nativetools.util

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.TextViewCompat
import com.dede.nativetools.R
import com.dede.nativetools.netspeed.NetSpeedPreferences
import com.dede.nativetools.netspeed.service.NetSpeedNotificationHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.roundToInt

const val matchParent = ViewGroup.LayoutParams.MATCH_PARENT
const val wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT

val Configuration.isNightMode: Boolean
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) this.isNightModeActive else
            this.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

fun Configuration.isSmallestScreenWidthDpAtLast(swDp: Int): Boolean {
    return this.smallestScreenWidthDp >= swDp
}

val Configuration.isLandscape: Boolean
    get() = this.orientation == Configuration.ORIENTATION_LANDSCAPE

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun TextView.setCompoundDrawablesRelative(
    start: Drawable? = null,
    top: Drawable? = null,
    end: Drawable? = null,
    bottom: Drawable? = null,
) {
    TextViewCompat.setCompoundDrawablesRelative(this, start, top, end, bottom)
}

object UI {

    @IntDef(flag = true, value = [UI.SW320DP, UI.SW480DP, UI.SW600DP, UI.SW720DP])
    @Retention(AnnotationRetention.SOURCE)
    annotation class SW

    const val SW320DP = 320
    const val SW480DP = 480
    const val SW600DP = 600
    const val SW720DP = 720

    val resources: Resources
        get() = globalContext.resources

    fun isSmallestScreenWidthDpAtLast(@SW swDp: Int): Boolean {
        return resources.configuration.isSmallestScreenWidthDpAtLast(swDp)
    }

    val isLandscape: Boolean
        get() = resources.configuration.isLandscape

    fun isWideSize(): Boolean {
        return isLandscape || isSmallestScreenWidthDpAtLast(SW600DP)
    }

    fun displayMetrics(): DisplayMetrics {
        return resources.displayMetrics
    }
}

/**
 * 获取View在全屏Window上的位置
 *
 * 例如: PopupWindow的View相对于父Window的位置
 */
fun View.getRectOnFullWindow(rect: Rect): Rect {
    // Window可以显示的区域
    // 相对于屏幕的位置，默认是屏幕去除系统栏后的大小
    // 分屏模式下也会根据分屏区域大小进行变更
    getWindowVisibleDisplayFrame(rect)
    val intArray = IntArray(2)
    // View在屏幕上的位置
    getLocationOnScreen(intArray)
    var x = intArray[0]
    var y = intArray[1]
    // 全屏模式下显示区域可能不正常，这里过滤一下
    if (rect.left >= 0 && rect.top >= 0 && !rect.isEmpty) {
        // 减去分屏模式下左边和上边的偏移量
        x -= rect.left
        y -= rect.top
    }
    rect.set(x, y, x + width, y + height)
    return rect
}

val Number.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        UI.displayMetrics()
    ).roundToInt()

val Number.dpf: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        UI.displayMetrics()
    )

typealias DialogOnClick = (dialog: DialogInterface) -> Unit

class AlertBuilder(private val builder: AlertDialog.Builder) {

    var isCancelable: Boolean = false
        set(value) {
            builder.setCancelable(value)
        }
    var show: Boolean = true

    fun setMessage(@StringRes messageId: Int) {
        builder.setMessage(messageId)
    }

    fun setMessage(message: CharSequence) {
        builder.setMessage(message)
    }

    fun positiveButton(@StringRes textId: Int, onClick: DialogOnClick? = null) {
        builder.setPositiveButton(textId) { dialog, _ ->
            onClick?.invoke(dialog)
        }
    }

    fun neutralButton(@StringRes textId: Int, onClick: DialogOnClick? = null) {
        builder.setNeutralButton(textId) { dialog, _ ->
            onClick?.invoke(dialog)
        }
    }

    fun negativeButton(@StringRes textId: Int, onClick: DialogOnClick? = null) {
        builder.setNegativeButton(textId) { dialog, _ ->
            onClick?.invoke(dialog)
        }
    }

}

fun Context.alert(
    @StringRes titleId: Int,
    @StringRes messageId: Int = -1,
    init: (AlertBuilder.() -> Unit)? = null,
): Dialog {
    val builder = MaterialAlertDialogBuilder(this)
        .setTitle(titleId)
    if (messageId > 0) {
        builder.setMessage(messageId)
    }
    val alertBuilder = AlertBuilder(builder)
    init?.invoke(alertBuilder)
    return if (alertBuilder.show) {
        builder.show()
    } else {
        builder.create()
    }
}

fun Context.showHideLockNotificationDialog() {
    val context = this@showHideLockNotificationDialog
    context.alert(
        R.string.label_net_speed_hide_lock_notification,
        R.string.alert_msg_hide_lock_notification
    ) {
        positiveButton(R.string.settings) {
            NetSpeedNotificationHelper.goLockHideNotificationSetting(context)
        }
        negativeButton(R.string.i_know)
        neutralButton(R.string.help) {
            context.browse(R.string.url_hide_lock_notification)
        }
    }
}

fun Context.showNotificationDisableDialog() {
    val context = this
    context.alert(
        R.string.alert_title_notification_disable,
        R.string.alert_msg_notification_disable
    ) {
        positiveButton(R.string.settings) {
            NetSpeedNotificationHelper.goNotificationSetting(context)
        }
        neutralButton(R.string.dont_ask) {
            NetSpeedPreferences.dontAskNotify = true
        }
        negativeButton(android.R.string.cancel, null)
    }
}