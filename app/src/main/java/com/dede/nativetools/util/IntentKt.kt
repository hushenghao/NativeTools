@file:JvmName("IntentKt")

package com.dede.nativetools.util

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.StringRes


inline fun <reified T> Intent(context: Context, vararg extras: Pair<String, Any>): Intent {
    return Intent(context, T::class.java).putExtras(*extras)
}

fun Intent(action: String, vararg extras: Pair<String, Any>): Intent {
    return Intent(action).putExtras(*extras)
}

fun Intent(action: String, data: String): Intent {
    return Intent(action).setData(data)
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

fun Intent.newClearTask(): Intent = this.newTask().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

fun Intent.setData(uri: String): Intent = setData(Uri.parse(uri))

fun Intent.toChooser(@StringRes titleId: Int): Intent =
    Intent.createChooser(this, globalContext.getString(titleId))

inline fun Intent.safelyStartActivity(context: Context) = context.safelyStartActivity(this)

fun Intent.toPendingActivity(context: Context, flags: Int): PendingIntent =
    PendingIntent.getActivity(context, 0, this, flags)

fun Intent.toPendingBroadcast(context: Context, flags: Int): PendingIntent =
    PendingIntent.getBroadcast(context, 0, this, flags)

fun PendingIntent.toNotificationAction(@StringRes titleId: Int): Notification.Action =
    Notification.Action.Builder(null, globalContext.getString(titleId), this).build()

fun IntentFilter.addActions(vararg actions: String): IntentFilter {
    for (action in actions) {
        this.addAction(action)
    }
    return this
}

fun Intent.queryImplicitActivity(context: Context): Boolean {
    return this.resolveActivityInfo(context.packageManager, PackageManager.MATCH_DEFAULT_ONLY) != null
}

fun <I> ActivityResultLauncher<I>.safelyLaunch(i: I? = null) {
    i.runCatching(this::launch).onFailure(Throwable::printStackTrace)
}
