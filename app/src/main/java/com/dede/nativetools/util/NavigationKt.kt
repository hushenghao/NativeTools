@file:JvmName("NavControllerKt")

package com.dede.nativetools.util

import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.Navigator
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.navigationrail.NavigationRailView
import kotlin.properties.ReadOnlyProperty

/** NavController */
fun FragmentActivity.navController(
    @IdRes viewId: Int
): ReadOnlyProperty<FragmentActivity, NavController> {
    return ReadOnlyProperty { _, _ -> this.findNavControllerCompat(viewId) }
}

private fun FragmentActivity.findNavControllerCompat(@IdRes viewId: Int): NavController {
    val navHostFragment = this.supportFragmentManager.findFragmentById(viewId) as? NavHostFragment
    if (navHostFragment != null) {
        return navHostFragment.navController
    }
    return this.findNavController(viewId)
}

fun NavDestination.matchDestinations(destinationIds: IntArray): Boolean =
    hierarchy.any { destinationIds.contains(it.id) }

internal inline fun <reified T : Navigator<*>> NavController.getNavigator(): T {
    return navigatorProvider.getNavigator(T::class.java)
}

/** NavigationBars */
object NavigationBars {

    fun setupWithNavController(
        navController: NavController,
        bottomNavigationView: BottomNavigationView? = null,
        navigationRailView: NavigationRailView? = null,
        navigationView: NavigationView? = null,
    ) {
        bottomNavigationView.setup(navController)
        navigationRailView.setup(navController)
        navigationView.setup(navController)
    }
}

private fun View?.setup(navController: NavController) {
    when (this) {
        null -> return
        is NavigationBarView -> {
            NavigationUI.setupWithNavController(this, navController)
        }
        is NavigationView -> {
            NavigationUI.setupWithNavController(this, navController)
        }
        else -> throw IllegalArgumentException("${this.javaClass} don`t impl")
    }
}
