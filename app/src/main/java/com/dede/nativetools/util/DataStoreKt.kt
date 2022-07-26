package com.dede.nativetools.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val dataStoreScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

private val Context.dataStore: DataStore<Preferences> by
    preferencesDataStore(
        name = "settings",
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        scope = dataStoreScope
    )

val globalDataStore: DataStore<Preferences>
    get() = globalContext.dataStore

fun DataStore<Preferences>.load(): Preferences {
    return runBlocking(dataStoreScope.coroutineContext) { this@load.data.first() }
}

fun <T> DataStore<Preferences>.get(key: Preferences.Key<T>, defValue: T): T {
    return runBlocking(dataStoreScope.coroutineContext) {
        this@get.data.map { it[key] }.firstOrNull()
    }
        ?: defValue
}

fun <T> DataStore<Preferences>.get(key: Preferences.Key<T>): T? {
    return runBlocking(dataStoreScope.coroutineContext) {
        this@get.data.map { it[key] }.firstOrNull()
    }
}

fun <T> DataStore<Preferences>.set(key: Preferences.Key<T>, value: T?) {
    dataStoreScope.launch {
        this@set.edit {
            if (value == null) {
                it.remove(key)
            } else {
                it[key] = value
            }
        }
    }
}

fun Preferences.get(key: String, defValue: Int): Int {
    return this[intPreferencesKey(key)] ?: defValue
}

fun Preferences.get(key: String, defValue: Long): Long {
    return this[longPreferencesKey(key)] ?: defValue
}

fun Preferences.get(key: String, defValue: Boolean): Boolean {
    return this[booleanPreferencesKey(key)] ?: defValue
}

fun Preferences.get(key: String, defValue: Float): Float {
    return this[floatPreferencesKey(key)] ?: defValue
}

fun Preferences.get(key: String, defValue: String): String {
    return this[stringPreferencesKey(key)] ?: defValue
}

class DataStorePreference(context: Context) : PreferenceDataStore() {

    private val dataStore = context.applicationContext.dataStore

    override fun getInt(key: String, defValue: Int): Int {
        return dataStore.get(intPreferencesKey(key), defValue)
    }

    override fun getLong(key: String, defValue: Long): Long {
        return dataStore.get(longPreferencesKey(key), defValue)
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return dataStore.get(booleanPreferencesKey(key), defValue)
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return dataStore.get(floatPreferencesKey(key), defValue)
    }

    override fun getString(key: String, defValue: String?): String? {
        return dataStore.get(stringPreferencesKey(key)) ?: defValue
    }

    override fun putInt(key: String, value: Int) {
        dataStore.set(intPreferencesKey(key), value)
    }

    override fun putLong(key: String, value: Long) {
        dataStore.set(longPreferencesKey(key), value)
    }

    override fun putBoolean(key: String, value: Boolean) {
        dataStore.set(booleanPreferencesKey(key), value)
    }

    override fun putFloat(key: String, value: Float) {
        dataStore.set(floatPreferencesKey(key), value)
    }

    override fun putString(key: String, value: String?) {
        dataStore.set(stringPreferencesKey(key), value)
    }
}
