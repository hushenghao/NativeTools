package com.dede.nativetools.main

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.dede.nativetools.R
import com.dede.nativetools.ui.SpaceItemDecoration
import com.dede.nativetools.util.annotation
import com.dede.nativetools.util.smallestScreenWidthDp

const val SW600DP = 600
const val SW720DP = 720

@Target(AnnotationTarget.CLASS)
annotation class StatusBarInsets(val smallestScreenWidthDp: Int = 0)

@Target(AnnotationTarget.CLASS)
annotation class NavigationBarInsets(val smallestScreenWidthDp: Int = 0)

typealias OnWindowInsetsListener = (insets: WindowInsetsCompat) -> Unit

fun WindowInsetsCompat.statusBar(): Int =
    this.getInsets(WindowInsetsCompat.Type.statusBars()).top

fun WindowInsetsCompat.navigationBar(): Int =
    this.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

fun View.onWindowInsetsApply(listener: OnWindowInsetsListener) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
        listener.invoke(insets)
        return@setOnApplyWindowInsetsListener insets
    }
}

fun applyRecyclerViewInsets(recyclerView: RecyclerView) {
    fun isAdded() = (recyclerView.getTag(R.id.tag_recycler_view) as? Boolean) ?: false
    if (isAdded()) return
    val itemDecoration = SpaceItemDecoration(0)
    recyclerView.onWindowInsetsApply {
        itemDecoration.overrideLastItemOffsets = { outRect ->
            outRect.bottom = it.navigationBar()
        }
        if (!isAdded()) {
            recyclerView.addItemDecoration(itemDecoration)
            recyclerView.setTag(R.id.tag_recycler_view, true)
            return@onWindowInsetsApply
        }
        recyclerView.requestLayout()
    }
}

fun applyBarsInsets(root: View, host: Any) {
    applyBarsInsets(root, root, host, null)
}

fun applyBarsInsets(
    root: View,
    target: View,
    host: Any,
    listener: OnWindowInsetsListener? = null
) {
    val context = root.context
    if (!WindowPreferencesManager(context).isEdgeToEdgeEnabled) {
        return
    }
    var statusBarInsets: StatusBarInsets?
    var navigationBarInsets: NavigationBarInsets?

    statusBarInsets = host.annotation()
    val smallestScreenWidthDp = smallestScreenWidthDp
    if (statusBarInsets != null) {
        if (smallestScreenWidthDp < statusBarInsets.smallestScreenWidthDp) {
            statusBarInsets = null
        }
    }
    navigationBarInsets = host.annotation()
    if (navigationBarInsets != null) {
        if (smallestScreenWidthDp < navigationBarInsets.smallestScreenWidthDp) {
            navigationBarInsets = null
        }
    }
    if (navigationBarInsets == null && statusBarInsets == null) return

    root.onWindowInsetsApply { insets ->
        var top = 0
        var bottom = 0
        if (statusBarInsets != null) {
            top = insets.statusBar()
        }
        if (navigationBarInsets != null) {
            bottom = insets.navigationBar()
        }
        target.setPadding(0, top, 0, bottom)
        root.requestLayout()
        listener?.invoke(insets)
    }
}
