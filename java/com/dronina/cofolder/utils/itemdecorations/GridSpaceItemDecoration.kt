package com.dronina.cofolder.utils.itemdecorations

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class GridSpaceItemDecoration(private val spanCount: Int, private val spacing: Int) :
    ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        parent.adapter?.let { adapter ->
            val layoutParams =
                view.layoutParams as StaggeredGridLayoutManager.LayoutParams
            if (layoutParams.isFullSpan) {
                outRect[0, 0, 0] = 0
            } else {
                val spanIndex = layoutParams.spanIndex
                val layoutPosition = layoutParams.viewLayoutPosition
                val itemCount = adapter.itemCount
                val leftEdge = spanIndex == 0
                val rightEdge = spanIndex == spanCount - 1
                val topEdge = spanIndex < spanCount
                val bottomEdge = layoutPosition >= itemCount - spanCount
                val halfSpacing = spacing / 2
                outRect[if (leftEdge) spacing else halfSpacing, if (topEdge) spacing else halfSpacing, if (rightEdge) spacing else halfSpacing] =
                    if (bottomEdge) spacing else 0
            }
        }
    }

}

