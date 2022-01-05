package com.dede.nativetools.netspeed

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceFragmentCompat
import com.dede.nativetools.R
import com.dede.nativetools.databinding.LayoutNetSpeedAdvancedHeaderBinding
import com.dede.nativetools.main.applyRecyclerViewInsets
import com.dede.nativetools.netspeed.service.NetSpeedService
import com.dede.nativetools.netspeed.utils.NetTextIconFactory
import com.dede.nativetools.ui.SliderPreference
import com.dede.nativetools.util.Intent
import com.dede.nativetools.util.globalPreferences
import com.dede.nativetools.util.requirePreference
import com.dede.nativetools.util.toast
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider

/**
 * 高级设置
 */
class NetSpeedAdvancedFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener,
    ServiceConnection, Slider.OnChangeListener {

    private val configuration = NetSpeedConfiguration.initialize()
    private var netSpeedBinder: INetSpeedInterface? = null

    private lateinit var binding: LayoutNetSpeedAdvancedHeaderBinding

    private val closeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            stopService()
        }
    }

    private fun SliderPreference.initialize(
        listener: NetSpeedAdvancedFragment,
        labelFormatter: LabelFormatter
    ) {
        this.onChangeListener = listener
        this.sliderLabelFormatter = labelFormatter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireContext().registerReceiver(closeReceiver, IntentFilter(NetSpeedService.ACTION_CLOSE))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)?.apply {
            binding = LayoutNetSpeedAdvancedHeaderBinding.inflate(LayoutInflater.from(this.context))
            (this as ViewGroup).addView(binding.root, 0)
        }
    }

    private val labelFormatterPercent = LabelFormatter { "%d %%".format((it * 100).toInt()) }
    private val labelFormatterRatio = LabelFormatter {
        val denominator = (it * 100).toInt()
        val molecular = 100 - denominator
        "$denominator : $molecular"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.net_speed_advanced_preference)
        requirePreference<SliderPreference>(NetSpeedPreferences.KEY_NET_SPEED_VERTICAL_OFFSET)
            .initialize(this, labelFormatterPercent)
        requirePreference<SliderPreference>(NetSpeedPreferences.KEY_NET_SPEED_HORIZONTAL_OFFSET)
            .initialize(this, labelFormatterPercent)
        requirePreference<SliderPreference>(NetSpeedPreferences.KEY_NET_SPEED_RELATIVE_RATIO)
            .initialize(this, labelFormatterRatio)
        requirePreference<SliderPreference>(NetSpeedPreferences.KEY_NET_SPEED_RELATIVE_DISTANCE)
            .initialize(this, labelFormatterPercent)
        requirePreference<SliderPreference>(NetSpeedPreferences.KEY_NET_SPEED_TEXT_SCALE)
            .initialize(this, labelFormatterPercent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyRecyclerViewInsets(listView)
        bindService()
        updatePreview(configuration)
    }

    private fun updatePreview(configuration: NetSpeedConfiguration) {
        binding.ivPreview.setImageBitmap(
            NetTextIconFactory.create(0, 0, configuration, 512, true)
        )
    }

    private var tempConfiguration: NetSpeedConfiguration? = null

    override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
        val key = slider.tag as String? ?: return// SliderPreference内设置了tag
        var config = tempConfiguration
        if (config == null) {
            config = configuration.copy()
            tempConfiguration = config
        } else {
            config.updateFrom(configuration)
        }
        when (key) {
            NetSpeedPreferences.KEY_NET_SPEED_VERTICAL_OFFSET -> {
                config.verticalOffset = value
            }
            NetSpeedPreferences.KEY_NET_SPEED_HORIZONTAL_OFFSET -> {
                config.horizontalOffset = value
            }
            NetSpeedPreferences.KEY_NET_SPEED_RELATIVE_RATIO -> {
                config.relativeRatio = value
            }
            NetSpeedPreferences.KEY_NET_SPEED_RELATIVE_DISTANCE -> {
                config.relativeDistance = value
            }
            NetSpeedPreferences.KEY_NET_SPEED_TEXT_SCALE -> {
                config.textScale = value
            }
            else -> return
        }
        updatePreview(config.apply { cachedBitmap = configuration.cachedBitmap })
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
            NetSpeedPreferences.KEY_NET_SPEED_TEXT_STYLE,
            NetSpeedPreferences.KEY_NET_SPEED_FONT,
            NetSpeedPreferences.KEY_NET_SPEED_MODE,
            NetSpeedPreferences.KEY_NET_SPEED_VERTICAL_OFFSET,
            NetSpeedPreferences.KEY_NET_SPEED_HORIZONTAL_OFFSET,
            NetSpeedPreferences.KEY_NET_SPEED_RELATIVE_RATIO,
            NetSpeedPreferences.KEY_NET_SPEED_RELATIVE_DISTANCE,
            NetSpeedPreferences.KEY_NET_SPEED_TEXT_SCALE -> {
                updatePreview(configuration)
                updateConfiguration()
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

    override fun onDestroy() {
        unbindService()
        requireContext().unregisterReceiver(closeReceiver)
        super.onDestroy()
    }

    private fun bindService() {
        val status = NetSpeedPreferences.status
        if (!status) return
        val context = requireContext()
        val intent = NetSpeedService.createIntent(context)
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