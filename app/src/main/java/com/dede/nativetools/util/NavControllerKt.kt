@file:JvmName("NavControllerKt")

package com.dede.nativetools.util

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.Navigator
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import kotlin.properties.ReadOnlyProperty


fun FragmentActivity.navController(@IdRes viewId: Int): ReadOnlyProperty<FragmentActivity, NavController> {
    return ReadOnlyProperty { _, _ -> this.findNavControllerCompat(viewId) }
}

private fun FragmentActivity.findNavControllerCompat(@IdRes viewId: Int): NavController {
    val navHostFragment =
        this.supportFragmentManager.findFragmentById(viewId) as? NavHostFragment
    if (navHostFragment != null) {
        return navHostFragment.navController
    }
    return this.findNavController(viewId)
}

internal inline fun <reified T : Navigator<*>> NavController.getNavigator(): T {
    return navigatorProvider.getNavigator(T::class.java)
}
