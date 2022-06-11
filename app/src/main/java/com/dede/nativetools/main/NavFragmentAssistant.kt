package com.dede.nativetools.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.transition.MaterialFadeThrough

/**
 * NavFragment管理扩展
 */
class NavFragmentAssistant(private val fragmentManager: FragmentManager) :
    FragmentManager.FragmentLifecycleCallbacks() {

    fun setupWithNavFragment(resId: Int) {
        val navHostFragment = fragmentManager.findFragmentById(resId) ?: return
        navHostFragment.childFragmentManager
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
        val view = fragment.view ?: return
        materialFadeThrough.removeTarget(view)
    }

    override fun onFragmentDestroyed(manager: FragmentManager, fragment: Fragment) {
        fragment.enterTransition = null
    }
}