package com.dede.nativetools.ui

import android.animation.Animator
import android.animation.FloatEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Property
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.addListener
import androidx.fragment.app.Fragment
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
    }

    private val binding: FragmentAboutBinding by viewBinding(FragmentAboutBinding::bind)
    private var animator: Animator? = null
    private var toasted = false

    private val scaleProperty = object : Property<View, Float>(Float::class.java, "scale") {
        override fun get(view: View): Float {
            return view.scaleX
        }

        override fun set(view: View, value: Float) {
            view.scaleX = value
            view.scaleY = value
        }
    }

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

        val followViews = arrayListOf(binding.ivLogo1, binding.ivLogo2)
        binding.ivLogo.setOnClickListener {
            appendFollowView(followViews)
        }
        setFollowView(followViews)
        if (animatored) {
            return
        }
        playAnimator()
    }

    private fun appendFollowView(followViews: ArrayList<ImageView>) {
        if (animator?.isRunning == true) {
            return
        }
        val count = followViews.size
        if (count >= MAX_FOLLOW_COUNT) {
            if (!toasted) {
                toast("BZZZTT!!1!💥")
                playAnimator()
                toasted = true
            }
            return
        }
        val insert = AppCompatImageView(requireContext()).apply {
            elevation = 1.dpf
            visibility = View.INVISIBLE
            setImageResource(R.mipmap.ic_launcher_round)
        }
        val last = followViews[count - 1]
        binding.container.addView(
            insert,
            binding.container.indexOfChild(last) + 1,
            ConstraintLayout.LayoutParams(last.layoutParams as ConstraintLayout.LayoutParams)
        )
        followViews.add(insert)
        setFollowView(followViews)
        playAnimator()
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
        binding.ivLogo.followViews = followViews.toTypedArray()
    }

    private fun playAnimator() {
        animatored = true
        animator = ObjectAnimator.ofFloat(binding.ivLogo, scaleProperty, 1f, 1.3f, 0.7f)
            .apply {
                duration = 200
                startDelay = 300
                repeatMode = ValueAnimator.REVERSE
                repeatCount = 1
                val feedback: (Animator) -> Unit = {
                    // BZZZTT!!1!
                    binding.ivLogo.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                }
                addListener(onStart = feedback, onRepeat = feedback)
                start()
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                requireActivity().share(R.string.share_text)
                true
            }
            R.id.action_get_beta -> {
                requireActivity().browse(getString(R.string.url_pgyer))
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