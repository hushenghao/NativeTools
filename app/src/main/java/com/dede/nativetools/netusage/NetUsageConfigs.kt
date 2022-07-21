package com.dede.nativetools.netusage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

class NetUsageConfigs(context: Context) {

    companion object {
        private const val PREF_NAME = "sim_card_config"

        const val KEY_ADD_IMSI_CONFIG = "key_add_imsi_config"
        const val KEY_IMSI_CONFIG_GROUP = "key_imsi_config_group"
        const val KEY_NET_USAGE_WIFI = "key_net_usage_wifi"
        const val KEY_NET_USAGE_MOBILE = "key_net_usage_mobile"

        private const val KEY_ENABLED_IMSI = "key_enable_imsi"
        private const val KEY_ALL_IMSI = "key_all_imsi"
    }

    private val sharedPreferences: SharedPreferences =
        try {
            EncryptedSharedPreferences(
                context,
                PREF_NAME,
                MasterKey(context),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Throwable) {
            Firebase.crashlytics.recordException(e)
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }

    private val allIMSI: LinkedHashSet<String> = LinkedHashSet()
    private val enabledIMSI: LinkedHashSet<String> = LinkedHashSet()

    init {
        sharedPreferences.getStringSet(KEY_ALL_IMSI, null)?.let { allIMSI.addAll(it) }
        sharedPreferences.getStringSet(KEY_ENABLED_IMSI, null)?.let { enabledIMSI.addAll(it) }
    }

    fun getEnabledIMSI(): Set<String> {
        return LinkedHashSet(enabledIMSI)
    }

    fun getAllIMSI(): Set<String> {
        return LinkedHashSet(allIMSI)
    }

    fun deleteIMSI(imsi: String) {
        allIMSI.remove(imsi)
        enabledIMSI.remove(imsi)
        save()
    }

    fun addIMSI(imsi: String): Boolean {
        val r = allIMSI.add(imsi)
        save()
        return r
    }

    fun setIMSIEnabled(imsi: String, enabled: Boolean) {
        allIMSI.add(imsi)
        if (enabled) {
            enabledIMSI.add(imsi)
        } else {
            enabledIMSI.remove(imsi)
        }
        save()
    }

    fun isEnabled(imsi: String): Boolean {
        return enabledIMSI.contains(imsi)
    }

    private fun save() {
        sharedPreferences
            .edit()
            .putStringSet(KEY_ALL_IMSI, allIMSI)
            .putStringSet(KEY_ENABLED_IMSI, enabledIMSI)
            .apply()
    }
}
