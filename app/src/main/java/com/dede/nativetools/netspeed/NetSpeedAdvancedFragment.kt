package com.dede.nativetools.netspeed

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.R
import com.dede.nativetools.databinding.FragmentNetSpeedAdvancedBinding
import com.dede.nativetools.main.NavigationBarInsets
import com.dede.nativetools.netspeed.service.NetSpeedService
import com.dede.nativetools.netspeed.utils.NetTextIconFactory
import com.dede.nativetools.util.globalPreferences
import com.dede.nativetools.util.toast
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider

@NavigationBarInsets
class NetSpeedAdvancedFragment : Fragment(R.layout.fragment_net_speed_advanced),
    SharedPreferences.OnSharedPreferenceChangeListener,
    ServiceConnection, LabelFormatter, Slider.OnChangeListener, Slider.OnSliderTouchListener {

    private val binding by viewBinding(FragmentNetSpeedAdvancedBinding::bind)

    private val configuration = NetSpeedConfiguration.initialize()
    private var netSpeedBinder: INetSpeedInterface? = null

    class NetSpeedAdvancedPreferences : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.net_speed_advanced_preference)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindService()

        binding.sliderVerticalOffset.value = configuration.verticalOffset
        binding.sliderRelativeRatio.value = configuration.relativeRatio
        binding.sliderRelativeDistance.value = configuration.relativeDistance
        binding.sliderTextScale.value = configuration.textScale
        updatePreview()
        initSlider(
            binding.sliderVerticalOffset,
            binding.sliderRelativeRatio,
            binding.sliderRelativeDistance,
            binding.sliderTextScale
        )
    }

    private fun initSlider(vararg sliders: Slider) {
        for (slider in sliders) {
            slider.setLabelFormatter(this)
            slider.addOnChangeListener(this)
            slider.addOnSliderTouchListener(this)
        }
    }

    private fun updatePreview() {
        val configuration = NetSpeedConfiguration.initialize().copy(
            verticalOffset = binding.sliderVerticalOffset.value,
            relativeRatio = binding.sliderRelativeRatio.value,
            relativeDistance = binding.sliderRelativeDistance.value,
            textScale = binding.sliderTextScale.value
        ).apply {
            cachedBitmap = this@NetSpeedAdvancedFragment.configuration.cachedBitmap
        }
        binding.ivPreview.setImageBitmap(
            NetTextIconFactory.create(
                0,
                0,
                configuration,
                512,
                true
            )
        )
    }

    override fun getFormattedValue(value: Float): String {
        return "$value %"
    }

    override fun onStartTrackingTouch(slider: Slider) {
    }

    override fun onStopTrackingTouch(slider: Slider) {
        NetSpeedPreferences.verticalOffset = binding.sliderVerticalOffset.value
        NetSpeedPreferences.relativeRatio = binding.sliderRelativeRatio.value
        NetSpeedPreferences.relativeDistance = binding.sliderRelativeDistance.value
        NetSpeedPreferences.textScale = binding.sliderTextScale.value
        updateConfiguration()
    }

    override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
        slider.performHapticFeedback(
            HapticFeedbackConstants.CLOCK_TICK,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
        updatePreview()
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
            NetSpeedPreferences.KEY_NET_SPEED_BOLD,
            NetSpeedPreferences.KEY_NET_SPEED_MODE -> {
                updatePreview()
                updateConfiguration()
            }
            NetSpeedPreferences.KEY_NET_SPEED_VERTICAL_OFFSET,
            NetSpeedPreferences.KEY_NET_SPEED_RELATIVE_RATIO,
            NetSpeedPreferences.KEY_NET_SPEED_RELATIVE_DISTANCE,
            NetSpeedPreferences.KEY_NET_SPEED_TEXT_SCALE -> {
                // nothing to do
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
        super.onDestroy()
    }

    private fun bindService() {
        val status = NetSpeedPreferences.status
        if (!status) return
        val context = requireContext()
        val intent = NetSpeedService.createIntent(context)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    private fun unbindService() {
        if (netSpeedBinder == null) {
            return
        }
        requireContext().unbindService(this)
        netSpeedBinder = null
    }

}