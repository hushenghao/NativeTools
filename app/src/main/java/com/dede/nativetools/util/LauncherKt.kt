@file:JvmName("LauncherKt")

package com.dede.nativetools.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.os.Build
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import com.dede.nativetools.R
import com.dede.nativetools.main.MainActivity
import kotlinx.coroutines.launch

private val packageName = globalContext.packageName
private val componentDay = ComponentName(globalContext, "$packageName.main.Day")
private val componentNight = ComponentName(globalContext, "$packageName.main.Night_1")

private fun createShortcutIcon(context: Context, resId: Int): IconCompat {
    val bitmap = LayerDrawable(
        arrayOf(
            GradientDrawable().apply {
                setColor(context.color(R.color.md_theme_primary))
                shape = GradientDrawable.OVAL
            },
            InsetDrawable(context.requireDrawable(resId), 4.dp).apply {
                setTint(Color.WHITE)
            }
        )
    ).toBitmap(24.dp, 24.dp)
    return IconCompat.createWithBitmap(bitmap)
}

fun installShortcuts() {
    val context = globalContext
    val shortcuts = arrayListOf(
        ShortcutInfoCompat.Builder(context, "shortcut_about")
            .setIcon(createShortcutIcon(context, R.drawable.ic_outline_info))
            .setIntent(
                android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    Uri.parse("https://dede.nativetools/about")
                )
                    .setClass(context, MainActivity::class.java)
            )
            .setShortLabel(context.getString(R.string.label_about))
            .setLongLabel(context.getString(R.string.label_about))
            .build(),
        ShortcutInfoCompat.Builder(context, "shortcut_toggle")
            .setIcon(createShortcutIcon(context, R.drawable.ic_outline_toggle_on))
            .setIntent(
                Intent(android.content.Intent.ACTION_VIEW, MainActivity.EXTRA_TOGGLE to true)
                    .setClass(context, MainActivity::class.java)
            )
            .setShortLabel(context.getString(R.string.label_net_speed_toggle))
            .setLongLabel(context.getString(R.string.label_net_speed_toggle))
            .build()
    )
    ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
}


fun tryApplyLauncherIcon() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        return
    }
    val context = globalContext
    val processInfo = context.currentProcess()
    if (processInfo.isMainProcess(context)) {
        // 主进程
        if (!processInfo.isForeground()) {
            applyLauncherIcon()
        }
        return
    }

    // 子进程中获取主进程信息
    val mainProcess = context.mainProcess()
    if (mainProcess != null && mainProcess.isForeground()) {
        // 主进程还在运行中
        return
    }
    mainScope.launch {
        val result = globalPreferences.tryReload()
        if (result) {
            applyLauncherIcon()
        }
    }
}

fun applyLauncherIcon() {
    val pm = globalContext.packageManager
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        // O以下不使用动态入口
        componentDay.enable(pm)
        componentNight.disable(pm)
    } else {
        if (isNightMode()) {
            componentNight.enable(pm)
            componentDay.disable(pm)
        } else {
            componentDay.enable(pm)
            componentNight.disable(pm)
        }
    }
    installShortcuts()
}

fun ComponentName.enable(pm: PackageManager) {
    val enable = PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    if (pm.getComponentEnabledSetting(this) == enable) return
    pm.setComponentEnabledSetting(this, enable, PackageManager.DONT_KILL_APP)
}

fun ComponentName.disable(pm: PackageManager) {
    val disabled = PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    if (pm.getComponentEnabledSetting(this) == disabled) return
    pm.setComponentEnabledSetting(this, disabled, PackageManager.DONT_KILL_APP)
}
