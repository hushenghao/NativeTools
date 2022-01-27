package com.dede.nativetools.netspeed

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.dede.nativetools.R
import com.dede.nativetools.main.applyBottomBarsInsets
import com.dede.nativetools.netspeed.service.NetSpeedNotificationHelper
import com.dede.nativetools.netspeed.service.NetSpeedServiceController
import com.dede.nativetools.ui.CustomWidgetLayoutSwitchPreference
import com.dede.nativetools.util.*

/**
 * 网速指示器设置页
 */
class NetSpeedFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val configuration = NetSpeedConfiguration.initialize()

    private val controller by lazy { NetSpeedServiceController(requireContext()) }

    private lateinit var usageSwitchPreference: SwitchPreferenceCompat
    private lateinit var statusSwitchPreference: SwitchPreferenceCompat

    private val activityResultLauncherCompat =
        ActivityResultLauncherCompat(this, ActivityResultContracts.StartActivityForResult())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (NetSpeedPreferences.status) {
            checkNotificationEnable()
        }
        controller.startService(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller.bindService(onCloseCallback = {
            statusSwitchPreference.isChecked = false
        })
        statusSwitchPreference.isChecked = NetSpeedPreferences.status
        if (!Logic.checkAppOps(requireContext())) {
            usageSwitchPreference.isChecked = false
        }

        if (UI.isWideSize()) {
            applyBottomBarsInsets(listView)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.net_speed_preference)
        initGeneralPreferenceGroup()
        initNotificationPreferenceGroup()
    }

    private fun initGeneralPreferenceGroup() {
        statusSwitchPreference =
            requirePreference(NetSpeedPreferences.KEY_NET_SPEED_STATUS)
    }

    private fun initNotificationPreferenceGroup() {
        usageSwitchPreference = requirePreference(NetSpeedPreferences.KEY_NET_SPEED_USAGE)

        updateNotificationPreferenceVisible()

        requirePreference<CustomWidgetLayoutSwitchPreference>(NetSpeedPreferences.KEY_NET_SPEED_HIDE_LOCK_NOTIFICATION)
            .bindCustomWidget = {
            it.findViewById(R.id.iv_preference_help)?.setOnClickListener {
                requireContext().showHideLockNotificationDialog()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        globalPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        globalPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        configuration.updateOnPreferenceChanged(key)
        when (key) {
            NetSpeedPreferences.KEY_NET_SPEED_STATUS -> {
                val status = NetSpeedPreferences.status
                if (status) controller.startService(true) else controller.stopService()
            }
            NetSpeedPreferences.KEY_NET_SPEED_INTERVAL,
            NetSpeedPreferences.KEY_NET_SPEED_QUICK_CLOSEABLE,
            NetSpeedPreferences.KEY_NET_SPEED_NOTIFY_CLICKABLE,
            NetSpeedPreferences.KEY_NET_SPEED_HIDE_LOCK_NOTIFICATION,
            NetSpeedPreferences.KEY_NET_SPEED_HIDE_NOTIFICATION -> {
                updateConfiguration()
            }
            NetSpeedPreferences.KEY_NET_SPEED_USAGE -> {
                updateConfiguration()
                checkOpsPermission()
            }
        }
    }

    private fun updateNotificationPreferenceVisible() {
        if (!NetSpeedNotificationHelper.itSSAbove(requireContext())) {
            return
        }
        val keys = arrayOf(
            NetSpeedPreferences.KEY_NET_SPEED_HIDE_NOTIFICATION,
            NetSpeedPreferences.KEY_NET_SPEED_USAGE,
            NetSpeedPreferences.KEY_NET_SPEED_NOTIFY_CLICKABLE,
            NetSpeedPreferences.KEY_NET_SPEED_QUICK_CLOSEABLE
        )
        for (key in keys) {
            requirePreference<Preference>(key).isVisible = false
        }
    }

    private fun updateConfiguration() {
        controller.updateConfiguration(configuration)
    }

    override fun onDestroyView() {
        controller.unbindService()
        super.onDestroyView()
    }

    private fun checkOpsPermission() {
        if (!configuration.usage) {
            return
        }
        Logic.requestOpsPermission(requireContext(), activityResultLauncherCompat, {
            usageSwitchPreference.isChecked = true
        }) {
            usageSwitchPreference.isChecked = false
        }
    }

    private fun checkNotificationEnable() {
        val context = requireContext()
        val areNotificationsEnabled = NetSpeedNotificationHelper.areNotificationEnabled(context)
        val dontAskNotify = NetSpeedPreferences.dontAskNotify
        if (dontAskNotify || areNotificationsEnabled) {
            return
        }
        context.showNotificationDisableDialog()
    }

}