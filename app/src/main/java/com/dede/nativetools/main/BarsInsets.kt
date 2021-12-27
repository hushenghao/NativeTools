package com.dede.nativetools.main

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dede.nativetools.util.smallestScreenWidthDp

@Target(AnnotationTarget.CLASS)
annotation class StatusBarInsets(val smallestScreenWidthDp: Int = 0)

@Target(AnnotationTarget.CLASS)
annotation class NavigationBarInsets(val smallestScreenWidthDp: Int = 0)

typealias OnBarsInsetsListener = () -> Unit

fun applyBarsInsets(
    root: View,
    target: View,
    fragment: Any,
    listener: OnBarsInsetsListener? = null
) {
    val context = root.context
    if (!WindowPreferencesManager(context).isEdgeToEdgeEnabled) {
        return
    }
    var statusBarInsets: StatusBarInsets?
    var navigationBarInsets: NavigationBarInsets?

    statusBarInsets = fragment.javaClass.getAnnotation(StatusBarInsets::class.java)
    val smallestScreenWidthDp = context.smallestScreenWidthDp
    if (statusBarInsets != null) {
        if (smallestScreenWidthDp < statusBarInsets.smallestScreenWidthDp) {
            statusBarInsets = null
        }
    }
    navigationBarInsets = fragment.javaClass.getAnnotation(NavigationBarInsets::class.java)
    if (navigationBarInsets != null) {
        if (smallestScreenWidthDp < navigationBarInsets.smallestScreenWidthDp) {
            navigationBarInsets = null
        }
    }
    if (navigationBarInsets == null && statusBarInsets == null) return

    ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
        var top = 0
        var bottom = 0
        if (statusBarInsets != null) {
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            top = statusBar.top
        }
        if (navigationBarInsets != null) {
            val navigationBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            bottom = navigationBar.bottom
        }
        target.setPadding(0, top, 0, bottom)
        listener?.invoke()
        return@setOnApplyWindowInsetsListener insets
    }
}
