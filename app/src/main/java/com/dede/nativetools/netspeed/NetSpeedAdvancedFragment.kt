package com.dede.nativetools.netspeed

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import com.dede.nativetools.R
import com.dede.nativetools.databinding.LayoutNetSpeedAdvancedHeaderBinding
import com.dede.nativetools.main.applyRecyclerViewInsets
import com.dede.nativetools.netspeed.service.NetSpeedServiceController
import com.dede.nativetools.netspeed.utils.NetTextIconFactory
import com.dede.nativetools.ui.ScrollVerticalChangeableLinearLayoutManager
import com.dede.nativetools.ui.SliderPreference
import com.dede.nativetools.util.globalPreferences
import com.dede.nativetools.util.requirePreference
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider

/**
 * 高级设置
 */
class NetSpeedAdvancedFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener, Slider.OnChangeListener {

    private val configuration = NetSpeedConfiguration.initialize()
    private val controller by lazy { NetSpeedServiceController(requireContext()) }

    private lateinit var binding: LayoutNetSpeedAdvancedHeaderBinding

    private val layoutManager by lazy { ScrollVerticalChangeableLinearLayoutManager(requireContext()) }

    private fun SliderPreference.initialize(
        listener: NetSpeedAdvancedFragment,
        labelFormatter: LabelFormatter
    ) {
        this.onChangeListener = listener
        this.sliderLabelFormatter = labelFormatter
        //this.scrollVerticalChangeable = layoutManager
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
        requirePreference<SliderPreference>(NetSpeedPreferences.KEY_NET_SPEED_HORIZONTAL_SCALE)
            .initialize(this, labelFormatterPercent)
    }

    override fun onCreateLayoutManager(): RecyclerView.LayoutManager {
        return layoutManager
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller.bindService()

        applyRecyclerViewInsets(listView)
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
            NetSpeedPreferences.KEY_NET_SPEED_HORIZONTAL_SCALE -> {
                config.horizontalScale = value
            }
            else -> return
        }
        updatePreview(config.apply { cachedBitmap = configuration.cachedBitmap })
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
            NetSpeedPreferences.KEY_NET_SPEED_TEXT_SCALE,
            NetSpeedPreferences.KEY_NET_SPEED_HORIZONTAL_SCALE -> {
                updatePreview(configuration)
                controller.updateConfiguration(configuration)
            }
        }
    }

    override fun onDestroyView() {
        controller.unbindService()
        super.onDestroyView()
    }

}