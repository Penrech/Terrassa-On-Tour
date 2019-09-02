package parcaudiovisual.terrassaontour.layoutManagers

import android.content.Context
import android.support.v7.widget.GridLayoutManager

class NonScrollableGridLayoutManager(context: Context, spanCount: Int) : GridLayoutManager(context, spanCount) {
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