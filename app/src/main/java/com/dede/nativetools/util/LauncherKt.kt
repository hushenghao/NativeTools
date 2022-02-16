@file:JvmName("LauncherKt")

package com.dede.nativetools.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import com.dede.nativetools.R
import com.dede.nativetools.main.MainActivity

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
        context.createShortcutInfo("shortcut_diagnosis",
            Intent(Intent.ACTION_VIEW, Uri.parse("https://dede.nativetools/diagnosis"))
                .setClass(context, MainActivity::class.java),
            R.drawable.ic_outline_pest_control,
            R.string.label_diagnosis),
        context.createShortcutInfo("shortcut_about",
            Intent(Intent.ACTION_VIEW, Uri.parse("https://dede.nativetools/about"))
                .setClass(context, MainActivity::class.java),
            R.drawable.ic_outline_info,
            R.string.label_about),
        context.createShortcutInfo("shortcut_toggle",
            Intent(Intent.ACTION_VIEW, MainActivity.EXTRA_TOGGLE to true)
                .setClass(context, MainActivity::class.java),
            R.drawable.ic_outline_toggle_on,
            R.string.label_net_speed_toggle)
    )
    ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
}

private fun Context.createShortcutInfo(
    id: String,
    intent: Intent,
    @DrawableRes icon: Int,
    @StringRes label: Int,
): ShortcutInfoCompat {
    return ShortcutInfoCompat.Builder(this, id)
        .setIcon(createShortcutIcon(this, icon))
        .setIntent(intent)
        .setShortLabel(this.getString(label))
        .setLongLabel(this.getString(label))
        .build()
}

