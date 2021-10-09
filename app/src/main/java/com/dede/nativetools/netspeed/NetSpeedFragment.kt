package com.dede.nativetools.netspeed

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.os.RemoteException
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.fragment.findNavController
import androidx.preference.*
import com.dede.nativetools.BuildConfig
import com.dede.nativetools.R
import com.dede.nativetools.ui.CustomWidgetLayoutSwitchPreference
import com.dede.nativetools.ui.SliderPreference
import com.dede.nativetools.util.*
import java.util.*

/**
 * 网速指示器设置页
 */
class NetSpeedFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener,
    ServiceConnection {

    companion object {
        private const val TAG = "NetSpeedFragment"

        // 888M 931135488L
        private const val MODE_ALL_BYTES = (2 shl 19) * 888L

        // 88.8M 93113549L
        private const val MODE_SINGLE_BYTES = ((2 shl 19) * 88.8).toLong()

        private const val KEY_ABOUT = "about"
        private const val KEY_IGNORE_BATTERY_OPTIMIZE = "ignore_battery_optimize"
    }

    private val configuration by lazy { NetSpeedConfiguration.initialize() }

    private var netSpeedBinder: INetSpeedInterface? = null

    private lateinit var scaleSliderPreference: SliderPreference
    private lateinit var statusSwitchPreference: SwitchPreferenceCompat
    private lateinit var usageSwitchPreference: SwitchPreferenceCompat

    private lateinit var opsResultLauncher: ActivityResultLauncher<Intent>

    private val closeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            stopService()
            statusSwitchPreference.isChecked = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchService()
        checkNotificationEnable()

        requireContext().registerReceiver(closeReceiver, IntentFilter(NetSpeedService.ACTION_CLOSE))
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.net_speed_preference)
        initGeneralPreferenceGroup()
        initNotificationPreferenceGroup()
        initOtherPreferenceGroup()
    }

    private fun initGeneralPreferenceGroup() {
        scaleSliderPreference = requirePreference(NetSpeedPreferences.KEY_NET_SPEED_SCALE)
        statusSwitchPreference = requirePreference(NetSpeedPreferences.KEY_NET_SPEED_STATUS)

        updateScalePreferenceIcon()
    }

    private fun initNotificationPreferenceGroup() {
        usageSwitchPreference = requirePreference(NetSpeedPreferences.KEY_NET_SPEED_USAGE)
        opsResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                usageSwitchPreference.isChecked = requireContext().checkAppOps()
            }
        if (!requireContext().checkAppOps()) {
            usageSwitchPreference.isChecked = false
        }

        updateNotificationPreferenceEnable()

        requirePreference<CustomWidgetLayoutSwitchPreference>(NetSpeedPreferences.KEY_NET_SPEED_HIDE_LOCK_NOTIFICATION)
            .bindCustomWidget = {
            it.findViewById(R.id.iv_preference_help)?.setOnClickListener {
                showHideLockNotificationDialog()
            }
        }
    }

    private fun initOtherPreferenceGroup() {
        requirePreference<Preference>(KEY_ABOUT).also {
            it.summary = getString(
                R.string.summary_about_version,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE
            )

            it.onPreferenceClickListener {
                findNavController().navigate(R.id.action_netSpeed_to_about)
            }
        }
        requirePreference<Preference>(KEY_IGNORE_BATTERY_OPTIMIZE).also {
            val context = requireContext()
            val packageName = context.packageName

            fun setVisible() {
                val powerManager = context.getSystemService<PowerManager>() ?: return
                it.isVisible = !powerManager.isIgnoringBatteryOptimizations(packageName)
            }

            setVisible()

            if (!it.isVisible) return@also

            @SuppressLint("BatteryLife")
            val activityResult: ActivityResultLauncher<Unit> =
                registerForActivityResult<Intent, ActivityResult>(
                    ActivityResultContracts.StartActivityForResult(),
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, "package:$packageName"
                    )
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        setVisible()
                    }
                }

            it.onPreferenceClickListener {
                activityResult.safelyLaunch()
            }
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        netSpeedBinder = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        netSpeedBinder = INetSpeedInterface.Stub.asInterface(service)
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
                if (status) startService() else stopService()
            }
            NetSpeedPreferences.KEY_NIGHT_MODE_TOGGLE -> {
                setNightMode(NetSpeedPreferences.isNightMode)
            }
            NetSpeedPreferences.KEY_NET_SPEED_INTERVAL,
            NetSpeedPreferences.KEY_NET_SPEED_QUICK_CLOSEABLE,
            NetSpeedPreferences.KEY_NET_SPEED_NOTIFY_CLICKABLE,
            NetSpeedPreferences.KEY_NET_SPEED_HIDE_LOCK_NOTIFICATION -> {
                updateConfiguration()
            }
            NetSpeedPreferences.KEY_NET_SPEED_USAGE -> {
                updateConfiguration()
                checkOpsPermission()
            }
            NetSpeedPreferences.KEY_NET_SPEED_MODE,
            NetSpeedPreferences.KEY_NET_SPEED_SCALE,
            NetSpeedPreferences.KEY_NET_SPEED_BACKGROUND -> {
                updateConfiguration()
                updateScalePreferenceIcon()
            }
            NetSpeedPreferences.KEY_NET_SPEED_HIDE_NOTIFICATION -> {
                updateNotificationPreferenceEnable()
                updateConfiguration()
            }
        }
    }

    private fun updateNotificationPreferenceEnable() {
        val keys = arrayOf(
            NetSpeedPreferences.KEY_NET_SPEED_USAGE,
            NetSpeedPreferences.KEY_NET_SPEED_NOTIFY_CLICKABLE,
            NetSpeedPreferences.KEY_NET_SPEED_QUICK_CLOSEABLE
        )
        val isEnabled = configuration.hideNotification.not()
        for (key in keys) {
            requirePreference<Preference>(key).isEnabled = isEnabled
        }
    }

    private fun updateConfiguration() {
        try {
            netSpeedBinder?.updateConfiguration(configuration)
        } catch (e: RemoteException) {
            toast("error")
        }
    }

    private fun updateScalePreferenceIcon() {
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.layer_icon_mask)

        val size = resources.getDimensionPixelSize(R.dimen.percent_preference_icon_size)
        val speed: Long = if (configuration.mode == NetSpeedConfiguration.MODE_ALL) {
            MODE_ALL_BYTES
        } else {
            MODE_SINGLE_BYTES
        }
        val bitmap = NetTextIconFactory.createIconBitmap(speed, speed, configuration, size, false)
        val layerDrawable = drawable as LayerDrawable
        layerDrawable.setDrawableByLayerId(R.id.icon_frame, bitmap.toDrawable(resources))
        scaleSliderPreference.setRightIcon(drawable)
    }

    override fun onDestroy() {
        requireContext().unregisterReceiver(closeReceiver)
        unbindService()
        super.onDestroy()
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
                if (!intent.queryActivity(context)) {
                    intent.data = null
                }
                opsResultLauncher.safelyLaunch(intent)
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

    private fun launchService() {
        val status = NetSpeedPreferences.status
        if (!status) return
        startService()
    }

    private fun startService() {
        val context = requireContext()
        val intent = NetSpeedService.createIntent(context)
        context.startService(intent)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    private fun stopService() {
        val context = requireContext()
        val intent = Intent<NetSpeedService>(context)
        unbindService()
        context.stopService(intent)
    }

    private fun unbindService() {
        if (netSpeedBinder == null) {
            return
        }
        requireContext().unbindService(this)
        netSpeedBinder = null
    }

}