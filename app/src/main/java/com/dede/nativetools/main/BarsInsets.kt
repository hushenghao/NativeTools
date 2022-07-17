package com.dede.nativetools.main

import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.widget.RecyclerView
import com.dede.nativetools.R
import com.dede.nativetools.ui.EdgeItemDecoration
import com.dede.nativetools.util.isRTL

typealias OnWindowInsetsListener = (insets: WindowInsetsCompat) -> Unit

fun WindowInsetsCompat.stableInsets(): Insets =
    this.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())

fun Insets.start(view: View): Int {
    return if (view.isRTL()) right else left
}

fun Insets.end(view: View): Int {
    return if (view.isRTL()) left else right
}

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
    start: View? = null,
    top: View? = null,
    end: View? = null,
    bottom: View? = null,
    listener: OnWindowInsetsListener? = null,
) {
    if (start == null && top == null && end == null && bottom == null && listener == null) {
        return
    }
    root.onWindowInsetsApply {
        val insets = it.stableInsets()
        start?.updatePaddingRelative(start = insets.start(start))
        top?.updatePaddingRelative(top = insets.top)
        end?.updatePaddingRelative(end = insets.end(end))
        bottom?.updatePaddingRelative(bottom = insets.bottom)
        root.requestLayout()
        listener?.invoke(it)
    }
}
