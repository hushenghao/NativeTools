package com.dede.nativetools.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.BuildConfig
import com.dede.nativetools.R
import com.dede.nativetools.databinding.FragmentAboutBinding
import com.dede.nativetools.util.browse
import com.dede.nativetools.util.market
import com.dede.nativetools.util.share

/**
 * 关于项目
 */
class AboutFragment : Fragment(R.layout.fragment_about) {

    private val binding: FragmentAboutBinding by viewBinding(FragmentAboutBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            findNavController().navigate(R.id.action_about_to_openSource)
        }
        binding.ivLogo.followViews = arrayOf(binding.ivLogo1, binding.ivLogo2, binding.ivLogo3, binding.ivLogo4)
        binding.ivGithub.enableFeedback = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                requireActivity().share(R.string.share_text)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_about, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                requireActivity().share(R.string.share_text)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_about, menu)
    }

}