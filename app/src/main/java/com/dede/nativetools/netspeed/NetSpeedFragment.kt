package com.dede.nativetools.netspeed

import android.content.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import com.dede.nativetools.BuildConfig
import com.dede.nativetools.R
import com.dede.nativetools.netspeed.NetSpeedConfiguration.Companion.defaultSharedPreferences
import com.dede.nativetools.util.*

/**
 * 网速指示器设置页
 */
class NetSpeedFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener,
    ServiceConnection {

    private val configuration by lazy {
        NetSpeedConfiguration.initialize()
           // .also { it.onSharedPreferenceChangeListener = this }
    }

    private var netSpeedBinder: INetSpeedInterface? = null

    private lateinit var scaleSeekBarPreference: SeekBarPreference
    private lateinit var statusSwitchPreference: SwitchPreference

    private val closeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            stopService()
            statusSwitchPreference.isChecked = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchService()

        checkOps()
        checkNotification()

        val intentFilter = IntentFilter(NetSpeedService.ACTION_CLOSE)
        requireContext().registerReceiver(closeReceiver, intentFilter)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.net_speed_preference)
        scaleSeekBarPreference = requirePreference(NetSpeedConfiguration.KEY_NET_SPEED_SCALE)
        statusSwitchPreference = requirePreference(NetSpeedConfiguration.KEY_NET_SPEED_STATUS)
        setScalePreferenceIcon()
        requirePreference<Preference>(KEY_ABOUT).let {
            it.summary = getString(
                R.string.summary_about_version,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE
            )
            it.setOnPreferenceClickListener {
                requireContext().browse(getString(R.string.url_github))
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
        // defaultSharedPreferences.registerOnSharedPreferenceChangeListener(configuration)
        // Crash!
        // SharedPreferencesImpl#mListener is a WeakHashMap.
        // data class NetSpeedConfiguration override hashCode method, unregisterOnSharedPreferenceChangeListener may fail.
        // Reference chain: SharedPreferencesImpl#mListener -weak-> configuration --> fragment, fragment is leaked.
        // if Activity recreated, edit shared preferences,
        // Crash when methods such as getContext are called in the onSharedPreferenceChanged method.
        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        // defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(configuration)
        defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        configuration.updateOnSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            NetSpeedConfiguration.KEY_NET_SPEED_STATUS -> {
                val status = sharedPreferences.getBoolean(key, false)
                if (status) startService() else stopService()
            }
            NetSpeedConfiguration.KEY_NET_SPEED_INTERVAL,
            NetSpeedConfiguration.KEY_NET_SPEED_COMPATIBILITY_MODE,
            NetSpeedConfiguration.KEY_NET_SPEED_QUICK_CLOSEABLE,
            NetSpeedConfiguration.KEY_NET_SPEED_NOTIFY_CLICKABLE -> {
                updateConfiguration()
            }
            NetSpeedConfiguration.KEY_NET_SPEED_MODE,
            NetSpeedConfiguration.KEY_NET_SPEED_SCALE,
            NetSpeedConfiguration.KEY_NET_SPEED_BACKGROUND -> {
                updateConfiguration()
                setScalePreferenceIcon()
            }
        }
    }

    private fun updateConfiguration() {
        try {
            netSpeedBinder?.updateConfiguration(configuration)
        } catch (e: RemoteException) {
            Toast.makeText(requireContext(), "error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setScalePreferenceIcon() {
        scaleSeekBarPreference.icon = createScalePreferenceIcon()
    }

    private fun createScalePreferenceIcon(): Drawable {
        val size = resources.getDimensionPixelSize(R.dimen.percent_preference_icon_size)
        val speed: Long = if (configuration.mode == NetSpeedConfiguration.MODE_ALL) {
            MODE_ALL_BYTES
        } else {
            MODE_SINGLE_BYTES
        }
        val bitmap = NetTextIconFactory.createIconBitmap(speed, speed, configuration, size, false)
        val layerDrawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.layer_icon_mask) as LayerDrawable
        layerDrawable.setDrawableByLayerId(R.id.icon_frame, bitmap.toDrawable(resources))
        return layerDrawable
    }

    override fun onDestroy() {
        requireContext().unregisterReceiver(closeReceiver)
        unbindService()
        super.onDestroy()
    }

    private fun checkOps() {
        val dontAskOps =
            defaultSharedPreferences.getBoolean(NetSpeedConfiguration.KEY_OPS_DONT_ASK, false)
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
                defaultSharedPreferences
                    .putBoolean(NetSpeedConfiguration.KEY_OPS_DONT_ASK, true)
            }
            negativeButton(R.string.cancel)
        }
    }

    private fun checkNotification() {
        val context = requireContext()
        val areNotificationsEnabled = NetSpeedNotificationHelp.areNotificationEnabled(context)
        val dontAskNotify = defaultSharedPreferences
            .getBoolean(NetSpeedConfiguration.KEY_NOTIFICATION_DONT_ASK, false)
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
                defaultSharedPreferences
                    .putBoolean(NetSpeedConfiguration.KEY_NOTIFICATION_DONT_ASK, true)
            }
            negativeButton(R.string.cancel, null)
        }
    }

    private fun launchService() {
        val status =
            defaultSharedPreferences.getBoolean(NetSpeedConfiguration.KEY_NET_SPEED_STATUS, false)
        if (!status) return
        startService()
    }

    private fun startService() {
        val context = requireContext()
        val intent = NetSpeedService.createServiceIntent(context)
        context.startService(intent)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    private fun stopService() {
        val requireContext = requireContext()
        val intent = Intent(requireContext, NetSpeedService::class.java)
        unbindService()
        requireContext.stopService(intent)
    }

    private fun unbindService() {
        if (netSpeedBinder == null) {
            return
        }
        requireContext().unbindService(this)
        netSpeedBinder = null
    }

    companion object {
        private const val TAG = "NetSpeedFragment"

        // 888M 931135488L
        private const val MODE_ALL_BYTES = (2 shl 19) * 888L

        // 88.8M 93113549L
        private const val MODE_SINGLE_BYTES = ((2 shl 19) * 88.8).toLong()

        private const val KEY_ABOUT = "about"
    }

}