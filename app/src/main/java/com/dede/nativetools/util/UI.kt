package com.dede.nativetools.util

import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.dede.nativetools.R
import com.dede.nativetools.netspeed.NetSpeedPreferences
import com.dede.nativetools.netspeed.service.NetSpeedNotificationHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*
import kotlin.math.roundToInt


val resources: Resources
    get() = globalContext.resources

val smallestScreenWidthDp: Int
    get() = resources.configuration.smallestScreenWidthDp

val isLandscape: Boolean
    get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

fun displayMetrics(): DisplayMetrics {
    return resources.displayMetrics
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
        displayMetrics()
    ).roundToInt()

val Number.dpf: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        displayMetrics()
    )

typealias DialogOnClick = (dialog: DialogInterface) -> Unit

class AlertBuilder(private val builder: AlertDialog.Builder) {

    var isCancelable: Boolean = false
        set(value) {
            builder.setCancelable(value)
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
    @StringRes messageId: Int,
    init: (AlertBuilder.() -> Unit)? = null
) {
    val builder = MaterialAlertDialogBuilder(this)
        .setTitle(titleId)
        .setMessage(messageId)
    init?.invoke(AlertBuilder(builder))
    builder.show()
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