package com.dede.nativetools.main

import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.dede.nativetools.R
import com.dede.nativetools.ui.EdgeItemDecoration

typealias OnWindowInsetsListener = (insets: WindowInsetsCompat) -> Unit

fun WindowInsetsCompat.stableInsets(): Insets =
    this.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())

fun View.onWindowInsetsApply(listener: OnWindowInsetsListener) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets: WindowInsetsCompat ->
        listener.invoke(insets)
        return@setOnApplyWindowInsetsListener insets
    }
}

fun applyBottomBarsInsets(recyclerView: RecyclerView) {
    recyclerView.onWindowInsetsApply {
        val old = recyclerView.getTag(R.id.tag_recycler_view) as? RecyclerView.ItemDecoration
        if (old != null) {
            recyclerView.removeItemDecoration(old)
        }

        val insets = it.stableInsets()
        val itemDecoration = EdgeItemDecoration(bottom = insets.bottom)
        recyclerView.addItemDecoration(itemDecoration)
        recyclerView.setTag(R.id.tag_recycler_view, itemDecoration)
        recyclerView.requestLayout()
    }
}

fun applyBarsInsets(
    root: View,
    left: View? = null,
    top: View? = null,
    right: View? = null,
    bottom: View? = null,
    listener: OnWindowInsetsListener? = null,
) {
    if (left == null && top == null && right == null && bottom == null && listener == null) {
        return
    }
    root.onWindowInsetsApply {
        val insets = it.stableInsets()
        left?.updatePadding(left = insets.left)
        top?.updatePadding(top = insets.top)
        right?.updatePadding(right = insets.right)
        bottom?.updatePadding(bottom = insets.bottom)
        root.requestLayout()
        listener?.invoke(it)
    }
}
