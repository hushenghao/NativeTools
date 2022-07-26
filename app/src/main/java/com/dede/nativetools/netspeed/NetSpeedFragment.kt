package com.dede.nativetools.netspeed

import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.*
import com.dede.nativetools.R
import com.dede.nativetools.main.applyBottomBarsInsets
import com.dede.nativetools.netspeed.service.NetSpeedNotificationHelper
import com.dede.nativetools.netspeed.service.NetSpeedService
import com.dede.nativetools.netspeed.service.NetSpeedServiceController
import com.dede.nativetools.netspeed.utils.NetFormatter
import com.dede.nativetools.ui.CustomWidgetLayoutSwitchPreference
import com.dede.nativetools.util.*
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.flow.firstOrNull

/** 网速指示器设置页 */
class NetSpeedFragment :
    PreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener,
    Preference.SummaryProvider<EditTextPreference> {

    private val configuration = NetSpeedConfiguration()

    private val controller by later { NetSpeedServiceController(requireContext()) }

    private lateinit var usageSwitchPreference: SwitchPreferenceCompat
    private lateinit var statusSwitchPreference: SwitchPreferenceCompat
    private lateinit var thresholdEditTextPreference: EditTextPreference
    private lateinit var intervalPreference: DropDownPreference

    private val activityResultLauncherCompat =
        ActivityResultLauncherCompat(this, ActivityResultContracts.StartActivityForResult())

    private val permissionLauncherCompat =
        ActivityResultLauncherCompat(this, ActivityResultContracts.RequestPermission())

    private val powerManager by later { requireContext().requireSystemService<PowerManager>() }
    private val broadcastHelper =
        BroadcastHelper(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED, NetSpeedService.ACTION_CLOSE)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = DataStorePreference(requireContext())
        addPreferencesFromResource(R.xml.preference_net_speed)
        initGeneralPreferenceGroup()
        initNotificationPreferenceGroup()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenCreated {
            val preferences = globalDataStore.data.firstOrNull() ?: return@launchWhenCreated
            configuration.updateFrom(preferences)

            val status = NetSpeedPreferences.status
            if (status) {
                startService()
            }
            statusSwitchPreference.isChecked = status
        }

        if (!Logic.checkAppOps(requireContext())) {
            usageSwitchPreference.isChecked = false
        }

        if (UI.isWideSize()) {
            applyBottomBarsInsets(listView)
        }

        broadcastHelper.register(requireContext()) { action, _ ->
            when (action) {
                PowerManager.ACTION_POWER_SAVE_MODE_CHANGED -> {
                    intervalPreference.isEnabled = !powerManager.isPowerSaveMode
                }
                NetSpeedService.ACTION_CLOSE -> {
                    statusSwitchPreference.isChecked = false
                }
            }
        }
    }

    private fun initGeneralPreferenceGroup() {
        statusSwitchPreference = requirePreference(NetSpeedPreferences.KEY_NET_SPEED_STATUS)
        statusSwitchPreference.onPreferenceChangeListener = this

        thresholdEditTextPreference =
            requirePreference<EditTextPreference>(NetSpeedPreferences.KEY_NET_SPEED_HIDE_THRESHOLD)
                .also {
                    it.summaryProvider = this
                    it.onPreferenceChangeListener = this
                }

        bindPreferenceChangeListener(this, NetSpeedPreferences.KEY_NET_SPEED_MIN_UNIT)

        intervalPreference =
            requirePreference<DropDownPreference>(NetSpeedPreferences.KEY_NET_SPEED_INTERVAL).also {
                it.setSummaryProvider { _ ->
                    if (powerManager.isPowerSaveMode) {
                        getString(R.string.label_power_save_mode)
                    } else {
                        ListPreference.SimpleSummaryProvider.getInstance().provideSummary(it)
                    }
                }
                it.isEnabled = !powerManager.isPowerSaveMode
                it.onPreferenceChangeListener = this
            }
    }

    override fun provideSummary(preference: EditTextPreference): CharSequence {
        val bytes = preference.text?.toLongOrNull()
        return if (bytes != null) {
            if (bytes > 0) {
                val threshold =
                    NetFormatter.format(bytes, NetFormatter.FLAG_FULL, NetFormatter.ACCURACY_EXACT)
                        .splicing()
                getString(R.string.summary_net_speed_hide_threshold, threshold)
            } else {
                getString(R.string.summary_net_speed_unhide)
            }
        } else {
            getString(R.string.summary_threshold_error)
        }
    }

    private fun initNotificationPreferenceGroup() {
        usageSwitchPreference =
            requirePreference<CustomWidgetLayoutSwitchPreference>(
                NetSpeedPreferences.KEY_NET_SPEED_USAGE
            )
                .also {
                    it.onPreferenceChangeListener = this
                    it.bindCustomWidget = { holder ->
                        val imageView = holder.findViewById(R.id.iv_preference_help) as ImageView
                        imageView.setImageResource(R.drawable.ic_round_settings)
                        imageView.setOnClickListener {
                            findNavController()
                                .navigate(R.id.action_netSpeed_to_netUsageConfigFragment)
                        }
                    }
                }

        requirePreference<CustomWidgetLayoutSwitchPreference>(
            NetSpeedPreferences.KEY_NET_SPEED_HIDE_LOCK_NOTIFICATION
        )
            .let {
                it.onPreferenceChangeListener = this
                it.bindCustomWidget = { holder ->
                    holder.findViewById(R.id.iv_preference_help)?.setOnClickListener {
                        requireContext().showHideLockNotificationDialog()
                    }
                }
            }
        bindPreferenceChangeListener(
            this,
            NetSpeedPreferences.KEY_NET_SPEED_NOTIFY_CLICKABLE,
            NetSpeedPreferences.KEY_NET_SPEED_QUICK_CLOSEABLE,
        )
    }

    private fun startService() {

        fun startServiceInternal(check: Boolean = true) {
            controller.startService(true)
            miuiNotificationAlert()
            if (check) {
                checkNotificationEnable()
            }
        }

        if (Build.VERSION.SDK_INT < 33/*Build.VERSION_CODES.T*/) {
            startServiceInternal()
        } else if (checkPermissions("android.permission.POST_NOTIFICATIONS")) {
            startServiceInternal()
        } else {
            permissionLauncherCompat.launch("android.permission.POST_NOTIFICATIONS") {
                startServiceInternal(!it)
            }
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            NetSpeedPreferences.KEY_NET_SPEED_STATUS -> {
                val status = newValue as Boolean
                if (status) {
                    startService()
                } else controller.stopService()
            }
            NetSpeedPreferences.KEY_NET_SPEED_INTERVAL -> {
                configuration.interval = (newValue as String).toInt()
                event(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_NAME, configuration.interval.toLong())
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "刷新间隔")
                }
            }
            NetSpeedPreferences.KEY_NET_SPEED_HIDE_THRESHOLD -> {
                val strValue = (newValue as String)
                val hideThreshold = if (strValue.isEmpty()) 0 else strValue.toLongOrNull()
                if (hideThreshold == null) {
                    toast(R.string.summary_threshold_error)
                    return false
                }
                configuration.hideThreshold = hideThreshold
                event(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_NAME, configuration.hideThreshold)
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "隐藏阈值")
                }

                val hideThresholdStr = hideThreshold.toString()
                if (hideThresholdStr != newValue) {
                    // Check input text, make sure it's all numbers.
                    // post set value, the value is saved only after method onPreferenceChange is
                    // called.
                    uiHandler.post { thresholdEditTextPreference.text = hideThresholdStr }
                }
            }
            NetSpeedPreferences.KEY_NET_SPEED_HIDE_LOCK_NOTIFICATION -> {
                configuration.hideLockNotification = newValue as Boolean
            }
            NetSpeedPreferences.KEY_NET_SPEED_USAGE -> {
                configuration.usage = newValue as Boolean
                checkOpsPermission()
            }
            NetSpeedPreferences.KEY_NET_SPEED_NOTIFY_CLICKABLE -> {
                configuration.notifyClickable = newValue as Boolean
            }
            NetSpeedPreferences.KEY_NET_SPEED_QUICK_CLOSEABLE -> {
                configuration.quickCloseable = newValue as Boolean
            }
            NetSpeedPreferences.KEY_NET_SPEED_MIN_UNIT -> {
                configuration.minUnit = (newValue as String).toInt()
            }
            else -> return true
        }
        controller.updateConfiguration(configuration)
        return true
    }

    override fun onDestroyView() {
        controller.unbindService()
        broadcastHelper.unregister(requireContext())
        super.onDestroyView()
    }

    private fun checkOpsPermission() {
        Logic.requestOpsPermission(
            requireContext(),
            activityResultLauncherCompat,
            { usageSwitchPreference.isChecked = true }
        ) { usageSwitchPreference.isChecked = false }
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

    private fun miuiNotificationAlert() {
        if (!Logic.isXiaomi() || NetSpeedPreferences.miuiAlerted) {
            return
        }
        val context = requireContext()
        context.alert(android.R.string.dialog_alert_title, R.string.alert_msg_miui_notification) {
            positiveButton(R.string.settings) {
                val intent = android.content.Intent(Settings.ACTION_SETTINGS)
                intent.newTask().launchActivity(context)
                NetSpeedPreferences.miuiAlerted = true
            }
            neutralButton(R.string.i_know) { NetSpeedPreferences.miuiAlerted = true }
        }
    }
}
