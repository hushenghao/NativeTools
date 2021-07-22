package com.dede.nativetools.netspeed

import android.content.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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

    private val configuration by lazy(LazyThreadSafetyMode.NONE) {
        NetSpeedConfiguration.initialize().also { it.onSharedPreferenceChangeListener = this }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchService()

        checkOps()
        checkNotification()

        val intentFilter = IntentFilter(NetSpeedService.ACTION_CLOSE)
        requireContext().registerReceiver(closeReceiver, intentFilter)
    }

    private fun checkOps() {
        val dontAskOps =
            defaultSharedPreferences.getBoolean(NetSpeedConfiguration.KEY_OPS_DONT_ASK, false)
        val context = requireContext()
        if (dontAskOps || context.checkAppOps()) {
            return
        }
        AlertDialog.Builder(context)
            .setTitle(R.string.usage_states_title)
            .setMessage(R.string.usage_stats_msg)
            .setPositiveButton(R.string.access) { _, _ ->
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                requireContext().safelyStartActivity(intent)
            }
            .setNeutralButton(R.string.dont_ask) { _, _ ->
                defaultSharedPreferences
                    .putBoolean(NetSpeedConfiguration.KEY_OPS_DONT_ASK, true)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun checkNotification() {
        val context = requireContext()
        val areNotificationsEnabled = NetSpeedNotificationHelp.areNotificationEnabled(context)
        val dontAskNotify = defaultSharedPreferences
            .getBoolean(NetSpeedConfiguration.KEY_NOTIFICATION_DONT_ASK, false)
        if (dontAskNotify || areNotificationsEnabled) {
            return
        }
        AlertDialog.Builder(context)
            .setTitle(R.string.alert_title_notification_disable)
            .setMessage(R.string.alert_msg_notification_disable)
            .setPositiveButton(R.string.access) { _, _ ->
                NetSpeedNotificationHelp.goNotificationSetting(context)
            }
            .setNeutralButton(R.string.dont_ask) { _, _ ->
                defaultSharedPreferences
                    .putBoolean(NetSpeedConfiguration.KEY_NOTIFICATION_DONT_ASK, true)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private val closeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            stopService()
            statusSwitchPreference?.isChecked = false
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
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        context.startService(intent)
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

    private lateinit var scaleSeekBarPreference: SeekBarPreference
    private var statusSwitchPreference: SwitchPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.net_speed_preference)
        scaleSeekBarPreference = findPreference(NetSpeedConfiguration.KEY_NET_SPEED_SCALE)!!
        statusSwitchPreference = findPreference(NetSpeedConfiguration.KEY_NET_SPEED_STATUS)
        setScalePreferenceIcon(false)
        findPreference<Preference>(KEY_ABOUT)?.let {
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

    private var netSpeedBinder: INetSpeedInterface? = null

    override fun onServiceDisconnected(name: ComponentName?) {
        netSpeedBinder = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        netSpeedBinder = INetSpeedInterface.Stub.asInterface(service)
    }

    override fun onStart() {
        super.onStart()
        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(configuration)
    }

    override fun onStop() {
        super.onStop()
        defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(configuration)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
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
                setScalePreferenceIcon(true)
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

    private fun setScalePreferenceIcon(updateConfiguration: Boolean) {
        if (updateConfiguration) {
            updateConfiguration()
        }

        scaleSeekBarPreference.icon = createScalePreferenceIcon()
    }

    private fun createScalePreferenceIcon(): Drawable {
        val size = PercentSeekBarPreference.ICON_SIZE.dp// 最大48dp
        val speed: Long = if (configuration.mode == NetSpeedConfiguration.MODE_ALL) {
            MODE_ALL_BYTES
        } else {
            MODE_SINGLE_BYTES
        }
        val bitmap = NetTextIconFactory.createIconBitmap(speed, speed, configuration, size, false)
        val bitmapDrawable = BitmapDrawable(resources, bitmap)
        val layerDrawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.layer_icon_mask) as LayerDrawable
        layerDrawable.setDrawableByLayerId(R.id.icon_frame, bitmapDrawable)
        return layerDrawable
    }

    override fun onDestroy() {
        requireContext().unregisterReceiver(closeReceiver)
        unbindService()
        super.onDestroy()
    }

    companion object {
        // 888M 931135488L
        private const val MODE_ALL_BYTES = (2 shl 19) * 888L

        // 88.8M 93113549L
        private const val MODE_SINGLE_BYTES = ((2 shl 19) * 88.8).toLong()

        private const val KEY_ABOUT = "about"
    }

}