package parcaudiovisual.terrassaontour

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class MaRecyclerViewItemDecoration(val padding: Int, val itemCount: Int, val columnNumber: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

        val actualPosition = parent.getChildAdapterPosition(view)
        val relativePosition = actualPosition % columnNumber

        if (relativePosition == 0) {
            outRect.left = padding
        }
        if (relativePosition == columnNumber - 1) {
            outRect.right = padding
        }

    }
}