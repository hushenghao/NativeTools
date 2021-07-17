package com.dede.nativetools.netspeed

import android.content.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import com.dede.nativetools.R
import com.dede.nativetools.netspeed.NetSpeedConfiguration.Companion.defaultSharedPreferences
import com.dede.nativetools.util.checkAppOps
import com.dede.nativetools.util.dp

/**
 * 网速指示器设置页
 */
class NetSpeedFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener,
    ServiceConnection {

    private val configuration by lazy(LazyThreadSafetyMode.NONE) {
        NetSpeedConfiguration.create().also { it.onSharedPreferenceChangeListener = this }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchService()

        val dontAskOps =
            !defaultSharedPreferences.getBoolean(NetSpeedConfiguration.KEY_OPS_DONT_ASK, false)
        if (dontAskOps && !requireContext().checkAppOps()) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.usage_states_title)
                .setMessage(R.string.usage_stats_msg)
                .setPositiveButton(R.string.access) { _, _ ->
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    startActivity(intent)
                }
                .setNeutralButton(R.string.dont_ask) { _, _ ->
                    defaultSharedPreferences.edit()
                        .putBoolean(NetSpeedConfiguration.KEY_OPS_DONT_ASK, true)
                        .apply()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
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

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.net_speed_preference)
        scaleSeekBarPreference = findPreference(NetSpeedConfiguration.KEY_NET_SPEED_SCALE)!!
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
        netSpeedBinder?.updateConfiguration(configuration)
    }

    private fun setModeOrScale() {
        val scaleInt = defaultSharedPreferences.getInt(
            NetSpeedConfiguration.KEY_NET_SPEED_SCALE,
            NetSpeedConfiguration.DEFAULT_SCALE_INT
        )
        val mode = defaultSharedPreferences.getString(
            NetSpeedConfiguration.KEY_NET_SPEED_MODE,
            NetSpeedConfiguration.MODE_DOWN
        ) ?: NetSpeedConfiguration.MODE_DOWN
        var scale = scaleInt / NetSpeedConfiguration.SCALE_DIVISOR
        updateConfiguration()

        val size = PercentSeekBarPreference.ICON_SIZE.dp// 最大48dp
        val padding = (size * 0.08f + 0.5f).toInt()
        // 多缩放padding*2个像素，添加边距
        scale = (size * scale - padding * 2) / size
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
        scaleSeekBarPreference.icon = layerDrawable
    }

    override fun onDestroy() {
        unbindService()
        super.onDestroy()
    }

}