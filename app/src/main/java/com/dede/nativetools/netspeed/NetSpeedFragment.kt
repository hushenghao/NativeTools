package com.dede.nativetools.netspeed

import android.content.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import com.dede.nativetools.R
import com.dede.nativetools.netspeed.NetSpeedConfiguration.Companion.defaultSharedPreferences
import com.dede.nativetools.netspeed.NetSpeedConfiguration.Companion.getMode
import com.dede.nativetools.netspeed.NetSpeedConfiguration.Companion.getScale
import com.dede.nativetools.util.checkAppOps
import com.dede.nativetools.util.dp
import com.dede.nativetools.util.putBoolean
import com.dede.nativetools.util.safelyStartActivity

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
        setModeOrScale()
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
            NetSpeedConfiguration.KEY_NET_SPEED_SCALE -> {
                setModeOrScale()
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

    private fun setModeOrScale() {
        var scale = defaultSharedPreferences.getScale()
        val mode = defaultSharedPreferences.getMode()
        updateConfiguration()

        val size = PercentSeekBarPreference.ICON_SIZE.dp// 最大48dp
        val padding = (size * 0.08f + 0.5f).toInt()
        // 多缩放padding*2个像素，添加边距
        scale = (size * scale - padding * 2) / size
        scaleSeekBarPreference.icon = createScalePreferenceIcon(mode, scale, size, padding)
    }

    private fun createScalePreferenceIcon(
        mode: String,
        scale: Float,
        size: Int,
        padding: Int
    ): Drawable {
        val bitmap = when (mode) {
            NetSpeedConfiguration.MODE_ALL -> {
                NetTextIconFactory.createDoubleIcon("888M", "888M", scale, size, false)
            }
            else -> {
                NetTextIconFactory.createSingleIcon("88.8", "MB/s", scale, size, false)
            }
        }
        val bitmapDrawable = BitmapDrawable(resources, bitmap)
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.layer_icon_mask)
        val layerDrawable = drawable as LayerDrawable
        layerDrawable.setDrawableByLayerId(R.id.icon_frame, bitmapDrawable)
        val mask = ContextCompat.getDrawable(requireContext(), R.drawable.shape_icon_mask)
        val insetDrawable = InsetDrawable(mask, padding)
        layerDrawable.setDrawableByLayerId(R.id.icon_mask, insetDrawable)
        return layerDrawable
    }

    override fun onDestroy() {
        requireContext().unregisterReceiver(closeReceiver)
        unbindService()
        super.onDestroy()
    }

}