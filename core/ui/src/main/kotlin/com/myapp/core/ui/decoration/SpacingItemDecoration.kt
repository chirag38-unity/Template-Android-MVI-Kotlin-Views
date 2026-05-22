package com.myapp.core.ui.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SpacingItemDecoration(
    private val spacingPx: Int,
    private val includeEdge: Boolean = false,
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val layoutManager = parent.layoutManager
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount

        if (layoutManager is GridLayoutManager) {
            applyGridSpacing(outRect, position, itemCount, layoutManager.spanCount)
        } else {
            applyLinearSpacing(outRect, position, itemCount)
        }
    }

    private fun applyGridSpacing(outRect: Rect, position: Int, itemCount: Int, spanCount: Int) {
        val column = position % spanCount
        if (includeEdge) {
            outRect.left = spacingPx - column * spacingPx / spanCount
            outRect.right = (column + 1) * spacingPx / spanCount
            if (position < spanCount) outRect.top = spacingPx
            outRect.bottom = spacingPx
        } else {
            outRect.left = column * spacingPx / spanCount
            outRect.right = spacingPx - (column + 1) * spacingPx / spanCount
            if (position >= spanCount) outRect.top = spacingPx
        }
    }

    private fun applyLinearSpacing(outRect: Rect, position: Int, itemCount: Int) {
        if (includeEdge) {
            outRect.top = if (position == 0) spacingPx else spacingPx / 2
            outRect.bottom = if (position == itemCount - 1) spacingPx else spacingPx / 2
        } else {
            outRect.top = if (position == 0) 0 else spacingPx / 2
            outRect.bottom = if (position == itemCount - 1) 0 else spacingPx / 2
        }
    }
}
