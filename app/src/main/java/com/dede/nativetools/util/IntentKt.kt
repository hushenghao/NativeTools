@file:JvmName("IntentKt")

package com.dede.nativetools.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import androidx.annotation.StringRes


inline fun <reified T> Intent(context: Context, vararg extras: Pair<String, Any>): Intent {
    return Intent(context, T::class.java).putExtras(*extras)
}

fun Intent(action: String, vararg extras: Pair<String, Any>): Intent {
    return Intent(action).putExtras(*extras)
}

fun Intent.putExtras(vararg extras: Pair<String, Any>): Intent {
    for (extra in extras) {
        val value = extra.second
        val key = extra.first
        when (value) {
            is Int -> this.putExtra(key, value)
            is Boolean -> this.putExtra(key, value)
            is String -> this.putExtra(key, value)
            is Parcelable -> this.putExtra(key, value)
            else -> Log.w("IntentKt", "Intent: ${value.javaClass} don`t impl")
        }
    }
    return this
}

fun Intent.newTask(): Intent = this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

fun Intent.setData(uri: String): Intent = setData(Uri.parse(uri))

fun Intent.toChooser(@StringRes titleId: Int): Intent =
    Intent.createChooser(this, globalContext.getString(titleId))

fun Intent.toPendingActivity(context: Context, flags: Int): PendingIntent =
    PendingIntent.getActivity(context, 0, this, flags)

fun Intent.toPendingBroadcast(context: Context, flags: Int): PendingIntent =
    PendingIntent.getBroadcast(context, 0, this, flags)


