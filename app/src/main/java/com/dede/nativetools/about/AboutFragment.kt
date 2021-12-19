package com.dede.nativetools.about

import android.animation.Animator
import android.animation.FloatEvaluator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.core.animation.addListener
import androidx.core.view.isInvisible
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.R
import com.dede.nativetools.databinding.FragmentAboutBinding
import com.dede.nativetools.util.*
import kotlin.random.Random

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
    private val colorIds: IntArray = intArrayOf(
        R.color.secondaryColor,
        R.color.primaryColor,
        android.R.color.black,
        android.R.color.holo_red_light,
        android.R.color.holo_blue_light,
        android.R.color.holo_green_light,
        android.R.color.holo_orange_light,
        android.R.color.holo_purple,
        android.R.color.darker_gray,
        com.google.android.material.R.color.material_deep_teal_200,
        com.google.android.material.R.color.material_blue_grey_950,
        com.google.android.material.R.color.material_grey_900,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        ChromeTabsBrowser.warmup(requireContext(), Uri.parse(getString(R.string.url_github)))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvVersion.text = requireContext().getVersionSummary()
        binding.ivGithub.setOnClickListener {
            requireContext().browse(R.string.url_github)
        }
        binding.ivGithub.enableFeedback = false
        binding.tvPrivacyAgreement.setOnClickListener {
            requireContext().browse(R.string.url_privacy_agreement)
        }

        val followViews = ArrayList<ImageView>()
        viewModel.followCount.observe(this.viewLifecycleOwner) {
            for (i in followViews.size until it) {
                appendFollowView(followViews, binding.ivLogoTemplate, it)
            }
        }
        binding.ivLogo.dragEnable = false
        binding.ivLogo.setOnClickListener {
            viewModel.addFollowCount()
            playAnimator(true)
        }
        binding.ivLogo.clipToOutline = true
        binding.ivLogo.outlineProvider = ViewOvalOutlineProvider()
        playAnimator(false)
    }

    private fun appendFollowView(
        followViews: ArrayList<ImageView>,
        template: ImageView,
        target: Int
    ) {
        var count = followViews.size
        if (count >= MAX_FOLLOW_COUNT) {
            if (!toasted) {
                toast("BZZZTT!!1!💥")
                toasted = true
            }
            return
        }
        val insert = if (count == 0) template else {
            AppCompatImageView(requireContext()).apply {
                setImageResource(R.mipmap.ic_launcher_round)
                isInvisible = true
                layoutParams = LayoutParams(template.layoutParams as LayoutParams)
                binding.rootAbout.addView(this, binding.rootAbout.indexOfChild(template))
            }
        }
        followViews.add(insert)
        binding.ivLogo.followViews = followViews.toTypedArray()

        val floatEvaluator = FloatEvaluator()
        for (i in 0 until ++count) {
            val value = floatEvaluator.evaluate((i + 1f) / count, 1f, 0.6f)
            followViews[i].apply {
                scaleX = value
                scaleY = value
                alpha = value
                setTintColor(colorIds[Random.nextInt(colorIds.size)])
            }
        }
        if (count == ENABLE_FOLLOW_COUNT) {
            if (target == ENABLE_FOLLOW_COUNT) {
                toast("BZZZTT!!1!🥚")
            }
            binding.ivLogo.dragEnable = true
        }
    }

    private fun ImageView.setTintColor(@ColorRes colorId: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val color = requireContext().getColor(colorId)
        ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(color))
        ImageViewCompat.setImageTintMode(this, PorterDuff.Mode.ADD)
    }

    private fun playAnimator(feedback: Boolean) {
        binding.ivLogo.clearAnimation()
        lifecycleAnimator(binding.ivLogo, ScaleProperty(), 1f, 1.3f, 0.9f) {
            duration = 200
            startDelay = 300
            repeatMode = ValueAnimator.REVERSE
            repeatCount = 1
            if (feedback) {
                val feedbackCallback: (Animator) -> Unit = {
                    // BZZZTT!!1!
                    binding.ivLogo.performHapticFeedback(
                        HapticFeedbackConstants.CONTEXT_CLICK,
                        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                    )
                }
                addListener(onStart = feedbackCallback, onRepeat = feedbackCallback)
            }
            start()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> {
                requireContext().share(R.string.share_text)
            }
            R.id.action_get_beta -> {
                requireContext().browse(R.string.url_pgyer)
            }
            R.id.action_rate -> {
                requireContext().market(requireContext().packageName)
            }
            R.id.action_feedback -> {
                requireContext().emailTo(R.string.email)
            }
            R.id.action_about_to_openSource,
            R.id.action_about_to_dialogDonate -> {
                // item.onNavDestinationSelected(findNavController())
                findNavController().navigate(item.itemId)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_about, menu)
    }

}