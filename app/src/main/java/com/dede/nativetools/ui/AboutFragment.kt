package com.dede.nativetools.ui

import android.os.Bundle
import android.view.View
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.BuildConfig
import com.dede.nativetools.R
import com.dede.nativetools.databinding.FragmentAboutBinding
import com.dede.nativetools.util.browse
import com.dede.nativetools.util.market
import com.dede.nativetools.util.setDisplayHomeAsUpEnabled

/**
 * 关于项目
 */
@Keep
class AboutFragment : Fragment(R.layout.fragment_about) {

    private val binding: FragmentAboutBinding by viewBinding(FragmentAboutBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().also {
            it.setTitle(R.string.label_about)
            it.setDisplayHomeAsUpEnabled(true)
        }
        binding.tvVersion.text = getString(
            R.string.summary_about_version,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        )
        binding.ivGithub.setOnClickListener {
            requireContext().browse(getString(R.string.url_github))
        }
        binding.tvLikeApp.setOnClickListener {
            requireContext().market(requireContext().packageName)
        }
        binding.tvOpenSource.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val fragment = OpenSourceFragment()
            fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

}