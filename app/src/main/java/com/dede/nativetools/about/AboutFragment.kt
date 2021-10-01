package com.dede.nativetools.about

import android.animation.Animator
import android.animation.FloatEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.core.animation.addListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.BuildConfig
import com.dede.nativetools.R
import com.dede.nativetools.databinding.FragmentAboutBinding
import com.dede.nativetools.donate.DonateDialogFragment
import com.dede.nativetools.util.*

/**
 * 关于项目
 */
class AboutFragment : Fragment(R.layout.fragment_about) {

    companion object {
        private const val MAX_FOLLOW_COUNT = 8
        private const val ENABLE_FOLLOW_COUNT = 2
    }

    private val binding by viewBinding(FragmentAboutBinding::bind)
    private val viewModel by viewModels<AboutViewModel>()
    private var toasted = false

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
            requireContext().browse(R.string.url_github)
        }
        binding.tvOpenSource.setOnClickListener {
            findNavController().navigate(R.id.action_about_to_openSource)
        }
        binding.ivGithub.enableFeedback = false

        val followViews = ArrayList<ImageView>()
        for (i in followViews.size until viewModel.followCount.value!!) {
            appendFollowView(followViews, binding.ivLogoTemplate, false)
        }
        binding.ivLogo.setOnClickListener {
            appendFollowView(followViews, binding.ivLogoTemplate)
        }
        setFollowView(followViews)

        if (!viewModel.animatored) {
            playAnimator()
        }
    }

    private fun createFollowView(template: ImageView): ImageView {
        return AppCompatImageView(requireContext()).apply {
            elevation = 1.dpf
            hide()
            setImageResource(R.mipmap.ic_launcher_round)
            layoutParams = LayoutParams(template.layoutParams as LayoutParams)
        }
    }

    private fun appendFollowView(
        followViews: ArrayList<ImageView>,
        template: ImageView,
        animator: Boolean = true
    ) {
        val count = followViews.size
        if (count >= MAX_FOLLOW_COUNT) {
            if (!toasted) {
                toast("BZZZTT!!1!💥")
                if (animator) {
                    playAnimator()
                }
                toasted = true
            }
            return
        }
        val insert = if (count == 0) template else {
            createFollowView(template).apply {
                binding.container.addView(this, binding.container.indexOfChild(template) + 1)
            }
        }
        followViews.add(insert)
        setFollowView(followViews)
        if (animator) {
            playAnimator()
        }
    }

    private fun setFollowView(followViews: List<ImageView>) {
        val floatEvaluator = FloatEvaluator()
        val count = followViews.size
        for (i in 0 until count) {
            val value = floatEvaluator.evaluate((i + 1f) / count, 1f, 0.6f)
            followViews[i].apply {
                scaleX = value
                scaleY = value
                alpha = value
            }
        }
        viewModel.setFollowCount(count)
        binding.ivLogo.dragEnable = count >= ENABLE_FOLLOW_COUNT
        binding.ivLogo.followViews = followViews.toTypedArray()
    }

    private fun playAnimator() {
        viewModel.animatored = true
        lifecycleAnimator(binding.ivLogo, ScaleProperty(), 1f, 1.3f, 0.7f)
            .apply {
                duration = 200
                startDelay = 300
                repeatMode = ValueAnimator.REVERSE
                repeatCount = 1
                val feedback: (Animator) -> Unit = {
                    // BZZZTT!!1!
                    binding.ivLogo.performHapticFeedback(
                        HapticFeedbackConstants.CONTEXT_CLICK,
                        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                    )
                }
                addListener(onStart = feedback, onRepeat = feedback)
                start()
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> {
                requireContext().share(R.string.share_text)
            }
            R.id.action_get_beta -> {
                requireContext().browse(getString(R.string.url_pgyer))
            }
            R.id.action_like -> {
                requireContext().market(requireContext().packageName)
            }
            R.id.action_feedback -> {
                requireContext().emailTo(R.string.email)
            }
            R.id.action_donate -> {
                DonateDialogFragment.show(childFragmentManager)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_about, menu)
    }

}