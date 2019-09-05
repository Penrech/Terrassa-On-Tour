package parcaudiovisual.terrassaontour.layoutManagers

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class NoScrollLinearLayoutManager(context: Context) : androidx.recyclerview.widget.LinearLayoutManager(context) {
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