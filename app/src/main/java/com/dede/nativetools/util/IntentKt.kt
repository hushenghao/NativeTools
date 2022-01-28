@file:JvmName("IntentKt")
@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package com.dede.nativetools.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat


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
            else -> throw IllegalArgumentException("IntentKt: put ${value.javaClass} don`t impl")
        }
    }
    return this
}

inline fun <reified T : Any> Intent.extra(name: String, default: T): T {
    val tClass = T::class
    return when {
        tClass == Int::class -> this.getIntExtra(name, default as Int) as T
        tClass == Boolean::class -> this.getBooleanExtra(name, default as Boolean) as T
        tClass == String::class -> (this.getStringExtra(name) as? T) ?: default
        Parcelable::class.java.isAssignableFrom(tClass.java) ->
            (this.getParcelableExtra(name) as? T) ?: default
        else -> {
            throw IllegalArgumentException("IntentKt: get $tClass don`t impl")
        }
    }
}

inline fun <reified T : Any> Intent.extra(name: String): T? {
    val tClass = T::class.java
    return when {
        tClass == String::class.java -> this.getStringExtra(name) as? T
        Parcelable::class.java.isAssignableFrom(tClass) -> this.getParcelableExtra(name) as? T
        else -> {
            throw IllegalArgumentException("IntentKt: get $tClass don`t impl")
        }
    }
}

fun Intent.newTask(): Intent = this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

fun Intent.newClearTask(): Intent = this.newTask().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

fun Intent.setData(uri: String): Intent = setData(Uri.parse(uri))

fun Intent.toChooser(@StringRes titleId: Int): Intent =
    Intent.createChooser(this, globalContext.getString(titleId))

@kotlin.internal.InlineOnly
inline fun Intent.launchActivity(context: Context) = context.launchActivity(this)

fun Intent.toPendingActivity(context: Context, flags: Int): PendingIntent =
    PendingIntent.getActivity(context, 0, this, flags)

fun Intent.toPendingBroadcast(context: Context, flags: Int): PendingIntent =
    PendingIntent.getBroadcast(context, 0, this, flags)

fun PendingIntent.toNotificationCompatAction(@StringRes titleId: Int): NotificationCompat.Action =
    NotificationCompat.Action.Builder(null, globalContext.getString(titleId), this).build()

fun IntentFilter(vararg actions: String): IntentFilter {
    val intentFilter = IntentFilter()
    for (action in actions) {
        intentFilter.addAction(action)
    }
    return intentFilter
}

fun Intent.queryImplicitActivity(context: Context): Boolean {
    return this.resolveActivityInfo(
        context.packageManager,
        PackageManager.MATCH_DEFAULT_ONLY
    ) != null
}
