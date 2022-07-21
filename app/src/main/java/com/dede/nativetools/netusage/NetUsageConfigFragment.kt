package com.dede.nativetools.netusage

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.dede.nativetools.R
import com.dede.nativetools.netspeed.NetSpeedConfiguration
import com.dede.nativetools.netspeed.NetSpeedPreferences
import com.dede.nativetools.netspeed.service.NetSpeedServiceController
import com.dede.nativetools.ui.CustomWidgetLayoutSwitchPreference
import com.dede.nativetools.ui.MaterialEditTextPreference
import com.dede.nativetools.util.*
import kotlinx.coroutines.flow.firstOrNull

/** 配置SIM卡IMSI */
class NetUsageConfigFragment : PreferenceFragmentCompat() {

    private lateinit var simCardCategory: PreferenceCategory
    private lateinit var addSimCardConfigPreference: MaterialEditTextPreference

    private val netUsageConfigs: NetUsageConfigs by later { NetUsageConfigs(requireContext()) }
    private val controller by later { NetSpeedServiceController(requireContext()) }
    private val configuration = NetSpeedConfiguration()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = DataStorePreference(requireContext())
        addPreferencesFromResource(R.xml.preference_net_usage_config)

        requirePreference<SwitchPreferenceCompat>(NetUsageConfigs.KEY_NET_USAGE_WIFI)
            .onPreferenceChangeListener<Boolean> { _, newValue ->
                configuration.enableWifiUsage = newValue
                controller.updateConfiguration(configuration)
                true
            }
        requirePreference<SwitchPreferenceCompat>(NetUsageConfigs.KEY_NET_USAGE_MOBILE)
            .onPreferenceChangeListener<Boolean> { _, newValue ->
                configuration.enableMobileUsage = newValue
                controller.updateConfiguration(configuration)
                true
            }

        simCardCategory = requirePreference(NetUsageConfigs.KEY_IMSI_CONFIG_GROUP)
        addSimCardConfigPreference = requirePreference(NetUsageConfigs.KEY_ADD_IMSI_CONFIG)
        addSimCardConfigPreference.onPreferenceChangeListener<String> { _, newValue ->
            if (newValue.isNotEmpty()) {
                addSimCardConfigPreference(newValue)
            }
            return@onPreferenceChangeListener false
        }

        if (NetSpeedPreferences.status) {
            controller.bindService()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenCreated {
            val preferences = globalDataStore.data.firstOrNull() ?: return@launchWhenCreated
            configuration.updateFrom(preferences)
            initSimCardCategory()
        }
    }

    override fun onDestroyView() {
        controller.unbindService()
        super.onDestroyView()
    }

    private fun updateImsiConfig() {
        configuration.updateImsi(netUsageConfigs.getEnabledIMSI())
        controller.updateConfiguration(configuration)
    }

    private fun initSimCardCategory() {
        simCardCategory.removeAll()
        var index = 1
        for (imsi in netUsageConfigs.getAllIMSI()) {
            simCardCategory.addPreference(
                createSimCardConfigPreference(index++, imsi, netUsageConfigs.isEnabled(imsi)))
        }
    }

    private fun addSimCardConfigPreference(imsi: String) {
        if (!netUsageConfigs.addIMSI(imsi)) return
        val simCardConfigPreference =
            createSimCardConfigPreference(simCardCategory.preferenceCount + 1, imsi, false)
        simCardCategory.addPreference(simCardConfigPreference)
    }

    private fun removeSimCardConfigPreference(imsi: String) {
        netUsageConfigs.deleteIMSI(imsi)
        for (i in 0 until simCardCategory.preferenceCount) {
            val preference = simCardCategory.getPreference(i)
            if (preference.key == imsi) {
                simCardCategory.removePreference(preference)
                break
            }
        }
        for (i in 0 until simCardCategory.preferenceCount) {
            val preference = simCardCategory.getPreference(i)
            preference.title = "SIM ${i + 1}"
        }
        updateImsiConfig()
    }

    private fun createSimCardConfigPreference(
        index: Int,
        imsi: String,
        isChecked: Boolean,
    ): SwitchPreferenceCompat {

        fun String.privateIMSI(): String {
            val size = this.length
            if (size <= 4) return this

            val arr = this.toCharArray()
            for (i in 2 until size - 2) {
                arr[i] = '*'
            }
            return String(arr)
        }

        return CustomWidgetLayoutSwitchPreference(requireContext(), null).apply {
            this.widgetLayoutResource = R.layout.override_preference_widget_switch_compat
            this.bindCustomWidget = {
                val imageView = it.findViewById(R.id.iv_preference_help) as ImageView
                imageView.setImageResource(R.drawable.ic_baseline_remove_circle)
                imageView.setOnClickListener { removeSimCardConfigPreference(imsi) }
            }
            this.isPersistent = false // 不保存
            this.title = "SIM $index"
            this.key = imsi
            this.summary = imsi.privateIMSI()
            this.setIcon(R.drawable.ic_outline_sim_card)
            this.setDefaultValue(isChecked) // 设置默认值，这时候还未onAttachedToHierarchy
            this.isChecked = isChecked
            this.onPreferenceChangeListener<Boolean> { _, newValue ->
                netUsageConfigs.setIMSIEnabled(imsi, newValue)
                updateImsiConfig()
                return@onPreferenceChangeListener true
            }
        }
    }
}
