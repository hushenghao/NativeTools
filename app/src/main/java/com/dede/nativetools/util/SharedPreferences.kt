package com.dede.nativetools.util

import android.content.SharedPreferences


fun SharedPreferences.getStringNotNull(key: String, default: String): String {
    return this.getString(key, default) ?: default
}

fun SharedPreferences.putBoolean(key: String, value: Boolean) {
    this.edit().putBoolean(key, value).apply()
}