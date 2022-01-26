package com.dede.nativetools.netspeed

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.preference.PreferenceFragmentCompat
import com.dede.nativetools.R
import com.dede.nativetools.databinding.LayoutNetSpeedAdvancedHeaderBinding
import com.dede.nativetools.main.applyBarsInsets
import com.dede.nativetools.main.applyBottomBarsInsets
import com.dede.nativetools.netspeed.service.NetSpeedServiceController
import com.dede.nativetools.netspeed.utils.NetTextIconFactory
import com.dede.nativetools.ui.SliderPreference
import com.dede.nativetools.util.UI
import com.dede.nativetools.util.globalPreferences
import com.dede.nativetools.util.matchParent
import com.dede.nativetools.util.requirePreference
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider
import kotlin.math.roundToInt

/**
 * 高级设置
 */
class NetSpeedAdvancedFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener, Slider.OnChangeListener {

    private val configuration = NetSpeedConfiguration.initialize()
    private val controller by lazy { NetSpeedServiceController(requireContext()) }

    private lateinit var binding: LayoutNetSpeedAdvancedHeaderBinding

    private fun SliderPreference.initialize(
        listener: NetSpeedAdvancedFragment,
        labelFormatter: LabelFormatter
    ) {
        this.onChangeListener = listener
        this.sliderLabelFormatter = labelFormatter
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)?.apply {
            binding = LayoutNetSpeedAdvancedHeaderBinding.inflate(LayoutInflater.from(this.context))
            @SuppressLint("InlinedApi")
            val listContainer = this.findViewById<FrameLayout>(android.R.id.list_container)
            val viewGroup = listContainer.parent as ViewGroup
            val index = viewGroup.indexOfChild(listContainer)
            val linearLayout = LinearLayout(viewGroup.context)
            viewGroup.removeView(listContainer)
            viewGroup.addView(linearLayout, index)
            val headerParams: LinearLayout.LayoutParams
            val listParams: LinearLayout.LayoutParams
            if (UI.isWideSize()) {
                linearLayout.orientation = LinearLayout.HORIZONTAL
                headerParams = LinearLayout.LayoutParams(0, matchParent, 2f)
                listParams = LinearLayout.LayoutParams(0, matchParent, 3f)
            } else {
                linearLayout.orientation = LinearLayout.VERTICAL
                headerParams = LinearLayout.LayoutParams(matchParent, 0, 1f)
                listParams = LinearLayout.LayoutParams(matchParent, 0, 3f)
            }
            linearLayout.addView(binding.root, headerParams)
            linearLayout.addView(listContainer, listParams)
        }
    }

    private val labelFormatterPercent = LabelFormatter { "%d %%".format((it * 100).roundToInt()) }
    private val labelFormatterRatio = LabelFormatter {
        val denominator = (it * 100).roundToInt()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller.bindService()

        applyBottomBarsInsets(listView)
        if (UI.isWideSize()) {
            applyBarsInsets(view, bottom = binding.root)
        }
        updatePreview(configuration)
    }

    private fun updatePreview(configuration: NetSpeedConfiguration) {
        var size = binding.ivPreview.width
        if (size <= 0) {
            size = 512
        }
        binding.ivPreview.setImageBitmap(
            NetTextIconFactory.create(0, 0, configuration, size, true)
        )
    }

    private var tempConfiguration: NetSpeedConfiguration? = null

    override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
        if (!fromUser) return
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
        updatePreview(config)
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
            NetSpeedPreferences.KEY_NET_SPEED_JUST_INTEGER,
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