package com.dede.nativetools.netspeed

import android.content.*
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.provider.Settings
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.fragment.findNavController
import androidx.preference.*
import com.dede.nativetools.BuildConfig
import com.dede.nativetools.R
import com.dede.nativetools.util.*

/**
 * 网速指示器设置页
 */
class NetSpeedFragment : PreferenceFragmentCompat(),
    NetSpeedPreferences.OnPreferenceChangeListener,
    ServiceConnection {

    companion object {
        private const val TAG = "NetSpeedFragment"

        // 888M 931135488L
        private const val MODE_ALL_BYTES = (2 shl 19) * 888L

        // 88.8M 93113549L
        private const val MODE_SINGLE_BYTES = ((2 shl 19) * 88.8).toLong()

        private const val KEY_ABOUT = "about"
        private const val KEY_LOCKED_HIDE = "net_speed_locked_hide"
    }

    private val configuration by lazy { NetSpeedConfiguration.initialize() }

    private var netSpeedBinder: INetSpeedInterface? = null

    private lateinit var scaleSeekBarPreference: SeekBarPreference
    private lateinit var statusSwitchPreference: SwitchPreference
    private lateinit var usageSwitchPreference: SwitchPreferenceCompat

    private lateinit var closeReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchService()

        checkOps()
        checkNotification()

        if (!::closeReceiver.isInitialized) {
            closeReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    stopService()
                    statusSwitchPreference.isChecked = false
                }
            }
        }

        val intentFilter = IntentFilter(NetSpeedService.ACTION_CLOSE)
        requireContext().registerReceiver(closeReceiver, intentFilter)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.net_speed_preference)
        scaleSeekBarPreference = requirePreference(NetSpeedPreferences.KEY_NET_SPEED_SCALE)
        statusSwitchPreference = requirePreference(NetSpeedPreferences.KEY_NET_SPEED_STATUS)
        updateScalePreferenceIcon()
        usageSwitchPreference = requirePreference(NetSpeedPreferences.KEY_NET_SPEED_USAGE)
        requirePreference<SwitchPreferenceCompat>(NetSpeedPreferences.KEY_V28_NIGHT_MODE_TOGGLE).also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it.isVisible = false
            } else {
                it.isVisible = true
                it.setOnPreferenceChangeListener { _, newValue ->
                    setNightMode(newValue == true)
                    return@setOnPreferenceChangeListener true
                }
            }
        }
        requirePreference<Preference>(KEY_LOCKED_HIDE).also {
            it.setOnPreferenceClickListener {
                NetSpeedNotificationHelp.goLockHideNotificationSetting(requireContext())
                return@setOnPreferenceClickListener true
            }
        }
        requirePreference<Preference>(KEY_ABOUT).also {
            it.summary = getString(
                R.string.summary_about_version,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE
            )

            it.setOnPreferenceClickListener {
                findNavController().navigate(R.id.action_netSpeed_to_about)
                return@setOnPreferenceClickListener true
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
        NetSpeedPreferences.registerPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        NetSpeedPreferences.unregisterPreferenceChangeListener(this)
    }

    override fun onPreferenceChanged(key: String) {
        configuration.updateOnPreferenceChanged(key)
        when (key) {
            NetSpeedPreferences.KEY_NET_SPEED_STATUS -> {
                val status = NetSpeedPreferences.status
                if (status) startService() else stopService()
            }
            NetSpeedPreferences.KEY_NET_SPEED_INTERVAL,
            NetSpeedPreferences.KEY_NET_SPEED_QUICK_CLOSEABLE,
            NetSpeedPreferences.KEY_NET_SPEED_NOTIFY_CLICKABLE -> {
                updateConfiguration()
            }
            NetSpeedPreferences.KEY_NET_SPEED_USAGE -> {
                updateConfiguration()
                checkOps()
            }
            NetSpeedPreferences.KEY_NET_SPEED_MODE,
            NetSpeedPreferences.KEY_NET_SPEED_SCALE,
            NetSpeedPreferences.KEY_NET_SPEED_BACKGROUND -> {
                updateConfiguration()
                updateScalePreferenceIcon()
            }
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
        val drawable = scaleSeekBarPreference.icon

        val size = resources.getDimensionPixelSize(R.dimen.percent_preference_icon_size)
        val speed: Long = if (configuration.mode == NetSpeedConfiguration.MODE_ALL) {
            MODE_ALL_BYTES
        } else {
            MODE_SINGLE_BYTES
        }
        val bitmap = NetTextIconFactory.createIconBitmap(speed, speed, configuration, size, false)
        val layerDrawable =
            drawable as LayerDrawable
        layerDrawable.setDrawableByLayerId(R.id.icon_frame, bitmap.toDrawable(resources))
    }

    override fun onDestroy() {
        if (::closeReceiver.isInitialized) {
            requireContext().unregisterReceiver(closeReceiver)
        }
        unbindService()
        super.onDestroy()
    }

    private fun checkOps() {
        if (!configuration.usage) {
            return
        }
        val dontAskOps = NetSpeedPreferences.dontAskOps
        val context = requireContext()
        if (dontAskOps || context.checkAppOps()) {
            return
        }
        context.alert(R.string.usage_states_title, R.string.usage_stats_msg) {
            positiveButton(R.string.access) {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                requireContext().safelyStartActivity(intent)
            }
            neutralButton(R.string.dont_ask) {
                NetSpeedPreferences.dontAskOps = true
            }
            negativeButton(R.string.cancel) {
                usageSwitchPreference.isChecked = false
            }
        }
    }

    private fun checkNotification() {
        val context = requireContext()
        val areNotificationsEnabled = NetSpeedNotificationHelp.areNotificationEnabled(context)
        val dontAskNotify = NetSpeedPreferences.dontAskNotify
        if (dontAskNotify || areNotificationsEnabled) {
            return
        }
        context.alert(
            R.string.alert_title_notification_disable,
            R.string.alert_msg_notification_disable
        ) {
            positiveButton(R.string.access) {
                NetSpeedNotificationHelp.goNotificationSetting(context)
            }
            neutralButton(R.string.dont_ask) {
                NetSpeedPreferences.dontAskNotify = true
            }
            negativeButton(R.string.cancel, null)
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