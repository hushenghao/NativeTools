package com.dede.nativetools.netspeed

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.dede.nativetools.R
import com.dede.nativetools.databinding.LayoutNetSpeedAdvancedPreviewBinding
import com.dede.nativetools.main.applyBarsInsets
import com.dede.nativetools.main.applyBottomBarsInsets
import com.dede.nativetools.netspeed.service.NetSpeedServiceController
import com.dede.nativetools.netspeed.utils.NetTextIconFactory
import com.dede.nativetools.ui.SliderPreference
import com.dede.nativetools.util.*
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.flow.firstOrNull
import kotlin.math.roundToInt

/**
 * 高级设置
 */
class NetSpeedAdvancedFragment : PreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener, Slider.OnChangeListener {

    private val configuration = NetSpeedConfiguration()
    private val controller by later { NetSpeedServiceController(requireContext()) }

    private lateinit var binding: LayoutNetSpeedAdvancedPreviewBinding

    private fun SliderPreference.initialize(
        listener: NetSpeedAdvancedFragment,
        labelFormatter: LabelFormatter,
    ) {
        this.onChangeListener = listener
        this.onPreferenceChangeListener = listener
        this.sliderLabelFormatter = labelFormatter
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            binding =
                LayoutNetSpeedAdvancedPreviewBinding.inflate(LayoutInflater.from(this.context))
            @SuppressLint("InlinedApi")
            val listContainer = this.findViewById<FrameLayout>(android.R.id.list_container)
            val viewGroup = listContainer.parent as ViewGroup
            val index = viewGroup.indexOfChild(listContainer)
            viewGroup.removeViewInLayout(listContainer)
            viewGroup.addView(createPairView(listContainer, binding.root), index)
        }
    }

    private fun createPairView(list: View, preview: View): View {
        val linearLayout = LinearLayout(list.context)
        val previewParams: LinearLayout.LayoutParams
        val listParams: LinearLayout.LayoutParams
        if (UI.isWideSize()) {
            linearLayout.orientation = LinearLayout.HORIZONTAL
            previewParams = LinearLayout.LayoutParams(0, matchParent, 2f)
            listParams = LinearLayout.LayoutParams(0, matchParent, 3f)
        } else {
            linearLayout.orientation = LinearLayout.VERTICAL
            previewParams = LinearLayout.LayoutParams(matchParent, 0, 1f)
            listParams = LinearLayout.LayoutParams(matchParent, 0, 3f)
        }
        linearLayout.addView(preview, previewParams)
        linearLayout.addView(list, listParams)
        return linearLayout
    }

    private val labelFormatterPercent = LabelFormatter { "%d %%".format((it * 100).roundToInt()) }
    private val labelFormatterRatio = LabelFormatter {
        val denominator = (it * 100).roundToInt()
        val molecular = 100 - denominator
        "$denominator : $molecular"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        lifecycleScope.launchWhenCreated {
            val preferences = globalDataStore.data.firstOrNull() ?: return@launchWhenCreated
            configuration.updateFrom(preferences)
        }
        preferenceManager.preferenceDataStore = DataStorePreference(requireContext())
        addPreferencesFromResource(R.xml.preference_net_speed_advanced)
        bindPreferenceChangeListener(
            this,
            NetSpeedPreferences.KEY_NET_SPEED_FONT,
            NetSpeedPreferences.KEY_NET_SPEED_TEXT_STYLE,
            NetSpeedPreferences.KEY_NET_SPEED_MODE
        )

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
        if (NetSpeedPreferences.status) {
            controller.bindService()
        }

        applyBottomBarsInsets(listView)
        if (UI.isWideSize()) {
            applyBarsInsets(view, bottom = binding.root)
        }
        binding.ivPreview.post {
            updatePreview(configuration)
        }
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

    @SuppressLint("RestrictedApi")
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
        updateConfigurationFloatValue(key, config, value)
        updatePreview(config)
    }

    private fun updateConfigurationFloatValue(
        key: String,
        config: NetSpeedConfiguration,
        value: Float,
    ) {
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
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            NetSpeedPreferences.KEY_NET_SPEED_FONT -> {
                configuration.font = newValue as String
                event(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_NAME, configuration.font)
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "字体")
                }
            }
            NetSpeedPreferences.KEY_NET_SPEED_TEXT_STYLE -> {
                configuration.textStyle = (newValue as String).toInt()
                event(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_NAME, configuration.textStyle.toLong())
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "字体样式")
                }
            }
            NetSpeedPreferences.KEY_NET_SPEED_MODE -> {
                configuration.mode = newValue as String
                event(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_NAME, configuration.mode)
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "显示模式")
                }
            }
            NetSpeedPreferences.KEY_NET_SPEED_VERTICAL_OFFSET,
            NetSpeedPreferences.KEY_NET_SPEED_HORIZONTAL_OFFSET,
            NetSpeedPreferences.KEY_NET_SPEED_RELATIVE_RATIO,
            NetSpeedPreferences.KEY_NET_SPEED_RELATIVE_DISTANCE,
            NetSpeedPreferences.KEY_NET_SPEED_TEXT_SCALE,
            NetSpeedPreferences.KEY_NET_SPEED_HORIZONTAL_SCALE,
            -> {
                updateConfigurationFloatValue(preference.key, configuration, newValue as Float)
            }
            else -> return true
        }
        updatePreview(configuration)
        controller.updateConfiguration(configuration)
        return true
    }

    override fun onDestroyView() {
        controller.unbindService()
        super.onDestroyView()
    }

}