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
import androidx.preference.PreferenceManager
import androidx.preference.SeekBarPreference
import com.dede.nativetools.R
import com.dede.nativetools.netspeed.NetSpeedService.Companion.MODE_ALL
import com.dede.nativetools.util.checkAppOps
import com.dede.nativetools.util.dp
import com.dede.nativetools.util.safeInt

class NetSpeedFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener,
    ServiceConnection {

    companion object {
        const val KEY_NET_SPEED_STATUS = "net_speed_status"
        const val KEY_NET_SPEED_INTERVAL = "net_speed_interval"
        const val KEY_NET_SPEED_COMPATIBILITY_MODE = "net_speed_locked_hide"
        const val KEY_NET_SPEED_NOTIFY_CLICKABLE = "net_speed_notify_clickable"
        const val KEY_NET_SPEED_AUTO_START = "net_speed_auto_start"
        const val KEY_NET_SPEED_MODE = "net_speed_mode"
        const val KEY_NET_SPEED_SCALE = "net_speed_scale"
        private const val KEY_OPS_DONT_ASK = "ops_dont_ask"

        private const val DEFAULT_SCALE_INT = 100
        private const val SCALE_DIVISOR = 100f

        fun createServiceIntent(context: Context): Intent {
            val preference: SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context)
            val intent = Intent(context, NetSpeedService::class.java)
            val interval =
                preference.getString(KEY_NET_SPEED_INTERVAL, null)
                    .safeInt(NetSpeedService.DEFAULT_INTERVAL)
            val compatibilityMode = preference.getBoolean(KEY_NET_SPEED_COMPATIBILITY_MODE, false)
            val clickable = preference.getBoolean(KEY_NET_SPEED_NOTIFY_CLICKABLE, true)
            val mode = preference.getString(KEY_NET_SPEED_MODE, NetSpeedService.MODE_DOWN)
            val scaleInt = preference.getInt(KEY_NET_SPEED_SCALE, DEFAULT_SCALE_INT)
            intent.putExtra(NetSpeedService.EXTRA_INTERVAL, interval)
            intent.putExtra(NetSpeedService.EXTRA_COMPATIBILITY_MODE, compatibilityMode)
            intent.putExtra(NetSpeedService.EXTRA_NOTIFY_CLICKABLE, clickable)
            intent.putExtra(NetSpeedService.EXTRA_MODE, mode)
            intent.putExtra(NetSpeedService.EXTRA_SCALE, scaleInt / SCALE_DIVISOR)
            return intent
        }
    }

    private val preference by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchService()

        if (!preference.getBoolean(KEY_OPS_DONT_ASK, false) &&
            !requireContext().checkAppOps()
        ) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.usage_states_title)
                .setMessage(R.string.usage_stats_msg)
                .setPositiveButton(R.string.access) { _, _ ->
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    startActivity(intent)
                }
                .setNeutralButton(R.string.dont_ask) { _, _ ->
                    preference.edit().putBoolean(KEY_OPS_DONT_ASK, true).apply()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun launchService() {
        val b = preference.getBoolean(KEY_NET_SPEED_STATUS, false)
        if (!b) return

        val context = requireContext()
        val intent = createServiceIntent(context)
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

    private fun toggleService() {
        val status = preference.getBoolean(KEY_NET_SPEED_STATUS, false)
        if (status) {
            launchService()
        } else {
            stopService()
        }
    }

    private lateinit var scaleSeekBarPreference: SeekBarPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.net_speed_preference)
        scaleSeekBarPreference = findPreference(KEY_NET_SPEED_SCALE)!!
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
        preference.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        preference.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            KEY_NET_SPEED_STATUS -> {
                toggleService()
            }
            KEY_NET_SPEED_INTERVAL -> {
                val interval =
                    sharedPreferences.getString(key, null).safeInt(NetSpeedService.DEFAULT_INTERVAL)
                netSpeedBinder?.setInterval(interval)
            }
            KEY_NET_SPEED_COMPATIBILITY_MODE -> {
                val compatibilityMode = sharedPreferences.getBoolean(key, false)
                netSpeedBinder?.compatibilityMode(compatibilityMode)
            }
            KEY_NET_SPEED_NOTIFY_CLICKABLE -> {
                val notifyClickable = sharedPreferences.getBoolean(key, false)
                netSpeedBinder?.setNotifyClickable(notifyClickable)
            }
            KEY_NET_SPEED_MODE, KEY_NET_SPEED_SCALE -> {
                setModeOrScale()
            }
        }
    }

    private fun setModeOrScale() {
        val scaleInt = preference.getInt(KEY_NET_SPEED_SCALE, DEFAULT_SCALE_INT)
        val mode = preference.getString(KEY_NET_SPEED_MODE, NetSpeedService.MODE_DOWN)
        var scale = scaleInt / SCALE_DIVISOR
        netSpeedBinder?.setScale(scale)
        netSpeedBinder?.setMode(mode ?: NetSpeedService.MODE_DOWN)

        val size = PercentSeekBarPreference.ICON_SIZE.dp// 最大48dp
        val padding = (size * 0.08f + 0.5f).toInt()
        // 多缩放padding*2个像素，添加边距
        scale = (size * scale - padding * 2) / size
        val bitmap = when (mode) {
            MODE_ALL -> {
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