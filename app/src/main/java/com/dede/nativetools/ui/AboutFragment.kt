package com.dede.nativetools.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Property
import android.view.*
import androidx.core.animation.addListener
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
    private var animator: Animator? = null

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
        binding.ivLogo.followViews =
            arrayOf(binding.ivLogo1, binding.ivLogo2, binding.ivLogo3, binding.ivLogo4)
        binding.ivGithub.enableFeedback = false

        val property = object : Property<View, Float>(Float::class.java, "scale") {
            override fun get(view: View): Float {
                return view.scaleX
            }

            override fun set(view: View, value: Float) {
                view.scaleX = value
                view.scaleY = value
            }
        }
        animator = ObjectAnimator.ofFloat(binding.ivLogo, property, 1f, 1.3f, 0.7f)
            .apply {
                duration = 200
                startDelay = 300
                repeatMode = ValueAnimator.REVERSE
                repeatCount = 1
                start()
                val feedback: (Animator) -> Unit = {
                    // BZZZTT!!1!
                    binding.ivLogo.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                }
                addListener(onStart = feedback, onRepeat = feedback, onEnd = {
                    animator = null
                })
            }
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

    override fun onDestroyView() {
        animator?.cancel()
        super.onDestroyView()
    }

}