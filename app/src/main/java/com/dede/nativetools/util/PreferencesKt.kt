@file:JvmName("PreferencesKt")

package com.dede.nativetools.util

import android.content.SharedPreferences
import androidx.preference.PreferenceManager


val globalPreferences: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(globalContext)

fun SharedPreferences.get(key: String, default: Int): Int {
    return this.getInt(key, default)
}

fun SharedPreferences.get(key: String, default: String): String {
    return this.getString(key, default) ?: default
}

fun SharedPreferences.get(key: String, default: Boolean): Boolean {
    return this.getBoolean(key, default)
}

fun SharedPreferences.put(key: String, value: Boolean) {
    this.edit().putBoolean(key, value).apply()
}
