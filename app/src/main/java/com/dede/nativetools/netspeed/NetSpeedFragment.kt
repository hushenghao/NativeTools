package com.dede.nativetools.netspeed

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.dede.nativetools.R
import com.dede.nativetools.main.applyBottomBarsInsets
import com.dede.nativetools.netspeed.service.NetSpeedNotificationHelper
import com.dede.nativetools.netspeed.service.NetSpeedServiceController
import com.dede.nativetools.netspeed.utils.NetFormatter
import com.dede.nativetools.ui.CustomWidgetLayoutSwitchPreference
import com.dede.nativetools.util.*

/**
 * 网速指示器设置页
 */
class NetSpeedFragment : PreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener,
    Preference.SummaryProvider<EditTextPreference> {

    private val configuration = NetSpeedConfiguration.initialize()

    private val controller by later { NetSpeedServiceController(requireContext()) }

    private lateinit var usageSwitchPreference: SwitchPreferenceCompat
    private lateinit var statusSwitchPreference: SwitchPreferenceCompat
    private lateinit var thresholdEditTextPreference: EditTextPreference

    private val activityResultLauncherCompat =
        ActivityResultLauncherCompat(this, ActivityResultContracts.StartActivityForResult())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (NetSpeedPreferences.status) {
            checkNotificationEnable()
            controller.startService(false)
        }
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
        statusSwitchPreference = requirePreference(NetSpeedPreferences.KEY_NET_SPEED_STATUS)
        statusSwitchPreference.onPreferenceChangeListener = this
        requirePreference<Preference>(NetSpeedPreferences.KEY_NET_SPEED_INTERVAL)
            .onPreferenceChangeListener = this

        thresholdEditTextPreference =
            requirePreference<EditTextPreference>(NetSpeedPreferences.KEY_NET_SPEED_HIDE_THRESHOLD).also {
                it.summaryProvider = this
                it.onPreferenceChangeListener = this
            }
    }

    override fun provideSummary(preference: EditTextPreference): CharSequence {
        val bytes = preference.text.toLongOrNull()
        return if (bytes != null) {
            if (bytes > 0) {
                val threshold = NetFormatter.format(
                    bytes,
                    NetFormatter.FLAG_FULL,
                    NetFormatter.ACCURACY_EXACT
                ).splicing()
                getString(R.string.summary_net_speed_hide_threshold, threshold)
            } else {
                getString(R.string.summary_net_speed_unhide)
            }
        } else {
            getString(R.string.summary_threshold_error)
        }
    }

    private fun initNotificationPreferenceGroup() {
        usageSwitchPreference = requirePreference(NetSpeedPreferences.KEY_NET_SPEED_USAGE)
        usageSwitchPreference.onPreferenceChangeListener = this

        requirePreference<CustomWidgetLayoutSwitchPreference>(NetSpeedPreferences.KEY_NET_SPEED_HIDE_LOCK_NOTIFICATION).let {
            it.onPreferenceChangeListener = this
            it.bindCustomWidget = { holder ->
                holder.findViewById(R.id.iv_preference_help)?.setOnClickListener {
                    requireContext().showHideLockNotificationDialog()
                }
            }
        }
        bindPreferenceChangeListener(
            this,
            NetSpeedPreferences.KEY_NET_SPEED_USAGE_JUST_MOBILE,
            NetSpeedPreferences.KEY_NET_SPEED_NOTIFY_CLICKABLE,
            NetSpeedPreferences.KEY_NET_SPEED_QUICK_CLOSEABLE,
            NetSpeedPreferences.KEY_NET_SPEED_HIDE_NOTIFICATION
        )

        updateNotificationPreferenceVisible()
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            NetSpeedPreferences.KEY_NET_SPEED_STATUS -> {
                val status = newValue as Boolean
                if (status) controller.startService(true) else controller.stopService()
            }
            NetSpeedPreferences.KEY_NET_SPEED_INTERVAL -> {
                configuration.interval = (newValue as String).toInt()
            }
            NetSpeedPreferences.KEY_NET_SPEED_HIDE_THRESHOLD -> {
                val hideThreshold = (newValue as String).toLongOrNull()
                if (hideThreshold == null) {
                    toast(R.string.summary_threshold_error)
                    return false
                }
                configuration.hideThreshold = hideThreshold

                val hideThresholdStr = hideThreshold.toString()
                if (hideThresholdStr != newValue) {
                    thresholdEditTextPreference.text = hideThresholdStr
                    return false
                }
            }

            NetSpeedPreferences.KEY_NET_SPEED_HIDE_LOCK_NOTIFICATION -> {
                configuration.hideLockNotification = newValue as Boolean
            }
            NetSpeedPreferences.KEY_NET_SPEED_USAGE -> {
                configuration.usage = newValue as Boolean
                checkOpsPermission()
            }
            NetSpeedPreferences.KEY_NET_SPEED_USAGE_JUST_MOBILE -> {
                configuration.justMobileUsage = newValue as Boolean
            }
            NetSpeedPreferences.KEY_NET_SPEED_NOTIFY_CLICKABLE -> {
                configuration.notifyClickable = newValue as Boolean
            }
            NetSpeedPreferences.KEY_NET_SPEED_QUICK_CLOSEABLE -> {
                configuration.quickCloseable = newValue as Boolean
            }
            NetSpeedPreferences.KEY_NET_SPEED_HIDE_NOTIFICATION -> {
                configuration.hideNotification = newValue as Boolean
            }
            else -> return true
        }
        controller.updateConfiguration(configuration)
        return true
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

    override fun onDestroyView() {
        controller.unbindService()
        super.onDestroyView()
    }

    private fun checkOpsPermission() {
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