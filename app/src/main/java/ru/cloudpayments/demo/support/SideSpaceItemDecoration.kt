package ru.cloudpayments.demo.support

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class SideSpaceItemDecoration(context: Context, spacing: Int, private val spanCount: Int, private val includeEdge: Boolean) : ItemDecoration() {
	private val spacingInDP: Float = spacing * context.resources.displayMetrics.density

	override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
		val position = parent.getChildAdapterPosition(view)
		val column = position % spanCount
		if (includeEdge) {
			outRect.left = (spacingInDP - column * spacingInDP / spanCount).toInt()
			outRect.right = ((column + 1) * spacingInDP / spanCount).toInt()
			if (position < spanCount) {
				outRect.top = spacingInDP.toInt()
			}
			outRect.bottom = spacingInDP.toInt()
		} else {
			outRect.left = (column * spacingInDP / spanCount).toInt()
			outRect.right = (spacingInDP - (column + 1) * spacingInDP / spanCount).toInt()
			if (position >= spanCount) {
				outRect.top = spacingInDP.toInt()
			}
		}
	}
}