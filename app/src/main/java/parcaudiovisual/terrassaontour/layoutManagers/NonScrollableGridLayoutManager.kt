package parcaudiovisual.terrassaontour.layoutManagers

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager

class NonScrollableGridLayoutManager(context: Context, spanCount: Int) : androidx.recyclerview.widget.GridLayoutManager(context, spanCount) {
    private var isScrollEnabled = false

    fun setScrollEnabled(flag: Boolean) {
        this.isScrollEnabled = flag
    }

    override fun canScrollVertically(): Boolean {
        return false
    }

    override fun canScrollHorizontally(): Boolean {
        return false
    }
}