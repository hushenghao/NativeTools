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
import com.dede.nativetools.util.*

/**
 * 关于项目
 */
class AboutFragment : Fragment(R.layout.fragment_about) {

    companion object {
        private var animatored = false
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
        binding.tvLikeApp.setOnClickListener {
            requireContext().market(requireContext().packageName)
        }
        val email = getString(R.string.email)
        binding.tvEmail.text = getString(R.string.label_email, email)
        binding.tvEmail.setOnClickListener {
            requireContext().copy(email)
            toast(R.string.toast_copyed)
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

        if (!animatored) {
            playAnimator()
        }
    }

    private fun createFollowView(template: ImageView): ImageView {
        val insert = AppCompatImageView(requireContext()).apply {
            elevation = 1.dpf
            hide()
            setImageResource(R.mipmap.ic_launcher_round)
        }
        binding.container.addView(
            insert,
            binding.container.indexOfChild(template) + 1,
            LayoutParams(template.layoutParams as LayoutParams)
        )
        return insert
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
        val insert = if (count == 0) template else createFollowView(template)
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
            val evaluate = floatEvaluator.evaluate((i + 1f) / count, 1f, 0.6f)
            followViews[i].apply {
                scaleX = evaluate
                scaleY = evaluate
                alpha = evaluate
            }
        }
        viewModel.setFollowCount(count)
        binding.ivLogo.dragEnable = count >= ENABLE_FOLLOW_COUNT
        binding.ivLogo.followViews = followViews.toTypedArray()
    }

    private fun playAnimator() {
        animatored = true
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
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_about, menu)
    }

}