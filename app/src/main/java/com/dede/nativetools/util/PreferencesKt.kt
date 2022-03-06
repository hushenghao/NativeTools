@file:JvmName("PreferencesKt")

package com.dede.nativetools.util

import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

fun <T : Preference> PreferenceFragmentCompat.requirePreference(key: CharSequence): T {
    return findPreference(key) as? T
        ?: throw NullPointerException("Preference not found, key: $key")
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
