package com.dede.nativetools.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SpaceItemDecoration(
    private val offset: Int,
    private val orientation: Int = LinearLayoutManager.VERTICAL
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (orientation == LinearLayoutManager.VERTICAL) {
            outRect.set(offset, if (position == 0) offset else 0, offset, offset)
        } else {
            outRect.set(if (position == 0) offset else 0, offset, offset, offset)
        }
    }
}