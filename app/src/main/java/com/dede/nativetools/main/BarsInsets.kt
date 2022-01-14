package com.dede.nativetools.main

import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.dede.nativetools.R
import com.dede.nativetools.ui.SpaceItemDecoration
import com.dede.nativetools.util.annotation
import com.dede.nativetools.util.smallestScreenWidthDp

const val SW600DP = 600
const val SW720DP = 720

@Target(AnnotationTarget.CLASS)
annotation class BarInsets(
    val left: Boolean = false, val leftSmallestScreenWidthDp: Int = 0,
    val top: Boolean = false, val topSmallestScreenWidthDp: Int = 0,
    val right: Boolean = false, val rightSmallestScreenWidthDp: Int = 0,
    val bottom: Boolean = false, val bottomSmallestScreenWidthDp: Int = 0
)

typealias OnWindowInsetsListener = (insets: WindowInsetsCompat) -> Unit

fun WindowInsetsCompat.systemBar(): Insets =
    this.getInsets(WindowInsetsCompat.Type.systemBars())

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
        val systemBar = it.systemBar()
        itemDecoration.overrideLastItemOffsets = { outRect ->
            outRect.bottom = systemBar.bottom
        }
        if (!isAdded()) {
            recyclerView.addItemDecoration(itemDecoration)
            recyclerView.setTag(R.id.tag_recycler_view, true)
            return@onWindowInsetsApply
        }
        recyclerView.requestLayout()
    }
}

fun applyBottomBarInset(root: View, host: Any) {
    applyBarsInsets(root, null, null, null, root, host, null)
}

fun applyBarsInsets(
    root: View,
    left: View? = null,
    top: View? = null,
    right: View? = null,
    bottom: View? = null,
    host: Any,
    listener: OnWindowInsetsListener? = null
) {
    if (left == null && top == null && right == null && bottom == null) {
        return
    }

    if (!WindowPreferencesManager(root.context).isEdgeToEdgeEnabled) {
        return
    }

    val barInsets = host.annotation<BarInsets>()
    val smallestScreenWidthDp = smallestScreenWidthDp
    if (barInsets == null) return

    fun apply(inset: Int, enable: Boolean, targetSmallestScreenWidthDp: Int): Int {
        if (enable && smallestScreenWidthDp >= targetSmallestScreenWidthDp) {
            return inset
        }
        return 0
    }

    root.onWindowInsetsApply { insets ->
        val systemBar = insets.systemBar()
        left?.updatePadding(
            left = apply(systemBar.left, barInsets.left, barInsets.leftSmallestScreenWidthDp)
        )
        top?.updatePadding(
            top = apply(systemBar.top, barInsets.top, barInsets.topSmallestScreenWidthDp)
        )
        right?.updatePadding(
            right = apply(systemBar.right, barInsets.right, barInsets.rightSmallestScreenWidthDp)
        )
        bottom?.updatePadding(
            bottom = apply(
                systemBar.bottom,
                barInsets.bottom,
                barInsets.bottomSmallestScreenWidthDp
            )
        )
        root.requestLayout()
        listener?.invoke(insets)
    }
}
