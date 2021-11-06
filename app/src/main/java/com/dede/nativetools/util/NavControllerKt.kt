@file:JvmName("NavControllerKt")

package com.dede.nativetools.util

import android.app.Activity
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.Navigator
import androidx.navigation.findNavController
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import kotlin.properties.ReadOnlyProperty


fun Activity.navController(@IdRes viewId: Int): ReadOnlyProperty<Activity, NavController> {
    return ReadOnlyProperty { _, _ -> this.findNavControllerCompat(viewId) }
}

private fun Activity.findNavControllerCompat(@IdRes viewId: Int): NavController {
    if (this is FragmentActivity) {
        val navHostFragment =
            this.supportFragmentManager.findFragmentById(viewId) as? NavHostFragment
        if (navHostFragment != null) {
            return navHostFragment.navController
        }
    }
    return this.findNavController(viewId)
}

inline fun <reified T : DialogFragment> DialogFragmentNavigator.navigate() {
    val destination = this.createDestination().apply {
        className = T::class.java.name
    }
    this.navigate(destination, null, null, null)
}

internal inline fun <reified T : Navigator<*>> NavController.getNavigator(): T {
    val navigator = this.navigatorProvider.getNavigator(T::class.java)
    if (navigator is DialogFragmentNavigator) {
        fixPopBackStack(navigator)
    }
    return navigator
}

/**
 * Fixed Fragments stack popup multiple times.
 * https://issuetracker.google.com/issues/205238165
 *
 * <pre>
 * class MyFragment : Fragment() {
 *      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *          val navigator = findNavController().navigatorProvider
 *              .getNavigator(DialogFragmentNavigator::class.java)
 *          val destination = navigator.createDestination().apply {
 *              className = MyDialogFragment::class.java.name
 *          }
 *          // show MyDialogFragment.
 *          navigator.navigate(destination, null, null, null)
 *          // onBackPressed. MyDialogFragment closes, but MyFragment also popup.
 *      }
 * }
 * </pre>
 *
 * 1. [DialogFragment.dismissInternal], remove current dialog fragment.
 * 2. [DialogFragmentNavigator.mObserver], call [DialogFragmentNavigator.popBackStack].
 * Causes the Fragments stack to popup twice.
 */
private fun fixPopBackStack(navigator: DialogFragmentNavigator) {
    val clazz = DialogFragmentNavigator::class.java
    try {
        clazz.declaredField("mObserver")
            .set(navigator, LifecycleEventObserver { _, _ -> /*ignore state change*/ })
    } catch (e: Exception) {
        e.printStackTrace()
    }
}