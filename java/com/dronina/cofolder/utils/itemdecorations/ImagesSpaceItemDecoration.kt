package com.dronina.cofolder.utils.itemdecorations

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class ImagesSpaceItemDecoration(
    private val spanCount: Int,
    private val spacing: Int
) :
    ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount
        outRect.left = column * spacing / spanCount
        outRect.right =
            spacing - (column + 1) * spacing / spanCount
        if (position >= spanCount) {
            outRect.top = spacing
        }
    }
}