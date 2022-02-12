@file:JvmName("LauncherKt")

package com.dede.nativetools.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
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

