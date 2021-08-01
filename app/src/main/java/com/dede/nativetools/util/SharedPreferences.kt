package com.dede.nativetools.util

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.dede.nativetools.NativeToolsApp


val defaultSharedPreferences: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(NativeToolsApp.getInstance())

fun SharedPreferences.getStringNotNull(key: String, default: String): String {
    return this.getString(key, default) ?: default
}

fun SharedPreferences.putBoolean(key: String, value: Boolean) {
    this.edit().putBoolean(key, value).apply()
}