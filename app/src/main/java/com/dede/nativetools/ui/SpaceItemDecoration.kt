package com.dede.nativetools.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

typealias OverrideItemOffsets = (outRect: Rect) -> Unit

class SpaceItemDecoration(
    private val offset: Int,
    private val orientation: Int = RecyclerView.VERTICAL,
    var overrideFirstItemOffsets: OverrideItemOffsets? = null,
    var overrideLastItemOffsets: OverrideItemOffsets? = null,
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val position = parent.getChildAdapterPosition(view)
        val spanCount = getSpanCount(parent)
        var left = 0
        var top = 0
        if (orientation == RecyclerView.VERTICAL) {
            if (spanCount == 1) {
                left = offset
                if (position == 0) {
                    top = offset
                }
            } else {
                val i = position % spanCount
                if (i == 0) {
                    left = offset
                }
                if (position < spanCount) {
                    top = offset
                }
            }
        } else {
            if (spanCount == 1) {
                if (position == 0) {
                    left = offset
                }
                top = offset
            } else {
                val i = position % spanCount
                if (i == 0) {
                    top = offset
                }
                if (position < spanCount) {
                    left = offset
                }
            }
        }
        outRect.set(left, top, offset, offset)

        val itemCount = parent.adapter?.itemCount ?: Int.MAX_VALUE
        if (position == 0) {
            overrideFirstItemOffsets?.invoke(outRect)
        } else if (position == itemCount - 1) {
            overrideLastItemOffsets?.invoke(outRect)
        }
    }

    private fun getSpanCount(recyclerView: RecyclerView): Int {
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is GridLayoutManager) {
            return layoutManager.spanCount
        }
        return 1
    }
}