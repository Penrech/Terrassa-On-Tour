package parcaudiovisual.terrassaontour

import android.support.v4.view.PagerAdapter
import android.view.View

class DetailImageAdapter(val listOfImages: ArrayList<String>): PagerAdapter() {

    override fun isViewFromObject(p0: View, p1: Any): Boolean {
        return false
    }

    override fun getCount(): Int {
        return listOfImages.size
    }
}