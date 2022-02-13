@file:JvmName("PreferencesKt")

package com.dede.nativetools.util

import android.content.SharedPreferences
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

fun <T : Preference> PreferenceFragmentCompat.requirePreference(key: CharSequence): T {
    return findPreference(key) as? T
        ?: throw NullPointerException("Preference not found, key: $key")
}

val globalPreferences: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(globalContext)

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

fun SharedPreferences.set(key: String, value: String?) {
    this.edit().putString(key, value).apply()
}

fun Preference.onPreferenceClickListener(listener: (preference: Preference) -> Unit) {
    this.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
        listener.invoke(preference)
        true
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> Preference.onPreferenceChangeListener(listener: (preference: Preference, newValue: T) -> Boolean) {
    this.onPreferenceChangeListener =
        Preference.OnPreferenceChangeListener { preference, newValue ->
            listener.invoke(preference, newValue as T)
        }
}

fun PreferenceFragmentCompat.bindPreferenceChangeListener(
    listener: Preference.OnPreferenceChangeListener,
    vararg keys: String
) {
    for (key in keys) {
        requirePreference<Preference>(key).onPreferenceChangeListener = listener
    }
}
