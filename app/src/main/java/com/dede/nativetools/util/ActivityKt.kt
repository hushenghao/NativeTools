@file:JvmName("ActivityKt")

package com.dede.nativetools.util

import android.app.Activity
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
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