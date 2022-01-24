@file:JvmName("PreferencesKt")

package com.dede.nativetools.util

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun <T : Preference> PreferenceFragmentCompat.requirePreference(key: CharSequence): T {
    return findPreference(key) as? T
        ?: throw NullPointerException("Preference not found, key: $key")
}

val globalPreferences: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(globalContext)

suspend fun SharedPreferences.tryReload(): Boolean {
    val shared = this
    return withContext(Dispatchers.IO) {
        kotlin.runCatching {
            @SuppressLint("PrivateApi")
            val clazz = Class.forName("android.app.SharedPreferencesImpl")
            val method = clazz.declaredMethod("startReloadIfChangedUnexpectedly")
            method.invoke(shared)
            true
        }.onFailure(Throwable::printStackTrace)
            .getOrDefault(false)
    }
}

fun SharedPreferences.has(key: String): Boolean {
    return this.all.containsKey(key)
}

fun SharedPreferences.get(key: String, default: Int): Int {
    return this.getInt(key, default)
}

fun SharedPreferences.get(key: String, default: Float): Float {
    return this.getFloat(key, default)
}

fun SharedPreferences.get(key: String, default: String): String {
    return this.getString(key, default) ?: default
}

fun SharedPreferences.get(key: String, default: Boolean): Boolean {
    return this.getBoolean(key, default)
}

fun SharedPreferences.set(key: String, value: Boolean) {
    this.edit().putBoolean(key, value).apply()
}

fun SharedPreferences.set(key: String, value: Int) {
    this.edit().putInt(key, value).apply()
}

fun SharedPreferences.set(key: String, value: Float) {
    this.edit().putFloat(key, value).apply()
}

fun Preference.onPreferenceClickListener(listener: (preference: Preference) -> Unit) {
    this.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
        listener.invoke(preference)
        true
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> Preference.onPreferenceChangeListener(listener: (preference: Preference, newValue: T) -> Unit) {
    this.onPreferenceChangeListener =
        Preference.OnPreferenceChangeListener { preference, newValue ->
            listener.invoke(preference, newValue as T)
            true
        }
}
