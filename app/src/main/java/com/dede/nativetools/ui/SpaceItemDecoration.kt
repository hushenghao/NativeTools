package com.dede.nativetools.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

typealias OverrideItemOffsets = (outRect: Rect) -> Unit

class SpaceItemDecoration(
    private val offset: Int,
    private val orientation: Int = LinearLayoutManager.VERTICAL,
    var overrideFirstItemOffsets: OverrideItemOffsets? = null,
    var overrideLastItemOffsets: OverrideItemOffsets? = null,
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount ?: Int.MAX_VALUE
        if (orientation == LinearLayoutManager.VERTICAL) {
            outRect.set(offset, if (position == 0) offset else 0, offset, offset)
        } else {
            outRect.set(if (position == 0) offset else 0, offset, offset, offset)
        }
        if (position == 0) {
            overrideFirstItemOffsets?.invoke(outRect)
        } else if (position == itemCount - 1) {
            overrideLastItemOffsets?.invoke(outRect)
        }
    }
}