package parcaudiovisual.terrassaontour

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View

class MaRecyclerViewItemDecoration(val padding: Int, val itemCount: Int, val columnNumber: Int) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State) {

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