@file:JvmName("NavControllerKt")

package com.dede.nativetools.util

import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.Navigator
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
import kotlin.properties.ReadOnlyProperty

/** NavController */

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

/** NavigationBars */

object NavigationBars {

    interface NavigationItemSelectedListener : NavigationBarView.OnItemSelectedListener,
        NavigationView.OnNavigationItemSelectedListener

    fun setupWithNavController(
        navController: NavController,
        listener: NavigationItemSelectedListener,
        vararg navigationViews: View?
    ) {
        for (navigationView in navigationViews) {
            navigationView.setup(navController, listener)
        }
    }
}

private fun View?.setup(
    navController: NavController,
    listener: NavigationBars.NavigationItemSelectedListener
) = when (this) {
    null -> null
    is NavigationBarView -> {
        this.setOnItemSelectedListener(listener)
        NavigationUI.setupWithNavController(this, navController)
    }
    is NavigationView -> {
        this.setNavigationItemSelectedListener(listener)
        NavigationUI.setupWithNavController(this, navController)
    }
    else -> throw IllegalArgumentException("${this.javaClass} don`t impl")
}