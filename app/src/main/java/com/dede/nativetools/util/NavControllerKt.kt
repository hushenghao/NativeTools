@file:JvmName("NavControllerKt")

package com.dede.nativetools.util

import android.app.Activity
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.NavDestination
import androidx.navigation.Navigator
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigationrail.NavigationRailView
import java.lang.ref.WeakReference
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

internal inline fun <reified T : Navigator<*>> NavController.getNavigator(): T {
    return navigatorProvider.getNavigator(T::class.java)
}

fun setupWithNavController(
    navigationRailView: NavigationRailView,
    navController: NavController
) {
    navigationRailView.setOnItemSelectedListener { item ->
        NavigationUI.onNavDestinationSelected(item, navController)
    }
    val weakReference = WeakReference(navigationRailView)
    navController.addOnDestinationChangedListener(
        object : OnDestinationChangedListener {
            override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: Bundle?
            ) {
                val view = weakReference.get()
                if (view == null) {
                    navController.removeOnDestinationChangedListener(this)
                    return
                }
                val menu = view.menu
                var h = 0
                val size = menu.size()
                while (h < size) {
                    val item = menu.getItem(h)
                    if (matchDestination(destination, item.itemId)) {
                        item.isChecked = true
                    }
                    h++
                }
            }
        })
}

private fun matchDestination(destination: NavDestination, @IdRes destId: Int): Boolean {
    var currentDestination: NavDestination? = destination
    while (currentDestination!!.id != destId && currentDestination.parent != null) {
        currentDestination = currentDestination.parent
    }
    return currentDestination.id == destId
}
