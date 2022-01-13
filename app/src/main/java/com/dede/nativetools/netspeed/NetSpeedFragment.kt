package com.dede.nativetools.netspeed

import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.dede.nativetools.R
import com.dede.nativetools.main.SW600DP
import com.dede.nativetools.main.applyRecyclerViewInsets
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
        checkNotificationEnable()
        controller.startService(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller.bindService()
        controller.onCloseCallback = {
            statusSwitchPreference.isChecked = false
        }
        statusSwitchPreference.isChecked = NetSpeedPreferences.status
        if (!requireContext().checkAppOps()) {
            usageSwitchPreference.isChecked = false
        }

        if (smallestScreenWidthDp < SW600DP) return
        applyRecyclerViewInsets(listView)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.net_speed_preference)
        initGeneralPreferenceGroup()
        initNotificationPreferenceGroup()
    }

    private fun initGeneralPreferenceGroup() {
        statusSwitchPreference =
            requirePreference(NetSpeedPreferences.KEY_NET_SPEED_STATUS)
        requirePreference<Preference>(NetSpeedPreferences.KEY_NET_SPEED_ADVANCED)
            .onPreferenceClickListener {
                findNavController().navigate(R.id.action_netSpeed_to_netSpeedAdvanced)
            }
    }

    private fun initNotificationPreferenceGroup() {
        usageSwitchPreference = requirePreference(NetSpeedPreferences.KEY_NET_SPEED_USAGE)

        updateNotificationPreferenceVisible()

        requirePreference<CustomWidgetLayoutSwitchPreference>(NetSpeedPreferences.KEY_NET_SPEED_HIDE_LOCK_NOTIFICATION)
            .bindCustomWidget = {
            it.findViewById(R.id.iv_preference_help)?.setOnClickListener {
                showHideLockNotificationDialog()
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
        controller.onCloseCallback = null
        super.onDestroyView()
    }

    private fun showHideLockNotificationDialog() {
        requireContext().alert(
            R.string.label_net_speed_hide_lock_notification,
            R.string.alert_msg_hide_lock_notification
        ) {
            positiveButton(R.string.settings) {
                NetSpeedNotificationHelper.goLockHideNotificationSetting(requireContext())
            }
            negativeButton(R.string.i_know)
            neutralButton(R.string.help) {
                requireContext().browse(R.string.url_hide_lock_notification)
            }
        }
    }

    private fun checkOpsPermission() {
        if (!configuration.usage) {
            return
        }
        val context = requireContext()
        if (context.checkAppOps()) {
            return
        }
        context.alert(R.string.usage_states_title, R.string.usage_stats_msg) {
            positiveButton(R.string.access) {
                val intent = Intent(
                    Settings.ACTION_USAGE_ACCESS_SETTINGS, "package:${context.packageName}"
                )
                if (!intent.queryImplicitActivity(context)) {
                    intent.data = null
                }
                activityResultLauncherCompat.launch(intent) {
                    usageSwitchPreference.isChecked = requireContext().checkAppOps()
                }
            }
            negativeButton(android.R.string.cancel) {
                usageSwitchPreference.isChecked = false
            }
        }
    }

    private fun checkNotificationEnable() {
        val context = requireContext()
        val areNotificationsEnabled = NetSpeedNotificationHelper.areNotificationEnabled(context)
        val dontAskNotify = NetSpeedPreferences.dontAskNotify
        if (dontAskNotify || areNotificationsEnabled) {
            return
        }
        context.alert(
            R.string.alert_title_notification_disable,
            R.string.alert_msg_notification_disable
        ) {
            positiveButton(R.string.settings) {
                NetSpeedNotificationHelper.goNotificationSetting(context)
            }
            neutralButton(R.string.dont_ask) {
                NetSpeedPreferences.dontAskNotify = true
            }
            negativeButton(android.R.string.cancel, null)
        }
    }

}