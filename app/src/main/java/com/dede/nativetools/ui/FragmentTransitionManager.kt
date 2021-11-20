package com.dede.nativetools.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.transition.MaterialFadeThrough

/**
 * 全局Fragment转场动画处理
 */
class FragmentTransitionManager : FragmentManager.FragmentLifecycleCallbacks() {

    fun attach(fragment: Fragment?) {
        (fragment ?: return).childFragmentManager
            .registerFragmentLifecycleCallbacks(this, true)
    }

    private val materialFadeThrough = MaterialFadeThrough()

    override fun onFragmentCreated(
        manager: FragmentManager,
        fragment: Fragment,
        savedInstanceState: Bundle?
    ) {
        if (fragment is DialogFragment) {
            return
        }
        fragment.enterTransition = materialFadeThrough
    }

    override fun onFragmentViewCreated(
        manager: FragmentManager,
        fragment: Fragment,
        view: View,
        savedInstanceState: Bundle?
    ) {
        if (fragment is DialogFragment) {
            return
        }
        materialFadeThrough.addTarget(view)
    }

    override fun onFragmentViewDestroyed(manager: FragmentManager, fragment: Fragment) {
        if (fragment is DialogFragment) {
            return
        }
        val view = fragment.requireView()
        materialFadeThrough.removeTarget(view)
    }

    override fun onFragmentDestroyed(manager: FragmentManager, fragment: Fragment) {
        fragment.enterTransition = null
    }
}