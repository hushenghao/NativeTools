package com.dede.nativetools.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EdgeItemDecoration(private val top: Int = 0, private val bottom: Int = 0) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val spanCount = getSpanCount(parent)
        val position = parent.getChildAdapterPosition(view)
        if (position < spanCount) { // top edge
            outRect.top = top
        }
        val itemCount = parent.adapter?.itemCount ?: return
        var surplus = itemCount % spanCount
        if (surplus == 0) {
            surplus = spanCount
        }
        if ((itemCount - 1 - position) < surplus) {
            outRect.bottom = bottom // bottom edge
        }
    }

    private fun getSpanCount(recyclerView: RecyclerView): Int {
        var spanCount = 1
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is GridLayoutManager) {
            spanCount = layoutManager.spanCount
        }
        return spanCount
    }
}

class GridItemDecoration(
    private val spacing: Int,
    private val includeEdge: Boolean = true,
    private val spanCount: Int = 1
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount // item column
        if (includeEdge) {
            // spacing - column * ((1f / spanCount) * spacing)
            outRect.left = spacing - column * spacing / spanCount
            // (column + 1) * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * spacing / spanCount
            if (position < spanCount) { // top edge
                outRect.top = spacing
            }
            outRect.bottom = spacing // item bottom
        } else {
            // column * ((1f / spanCount) * spacing)
            outRect.left = column * spacing / spanCount
            // spacing - (column + 1) * ((1f / spanCount) * spacing)
            outRect.right = spacing - (column + 1) * spacing / spanCount
            if (position >= spanCount) {
                outRect.top = spacing // item top
            }
        }
    }
}
