package parcaudiovisual.terrassaontour.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class InfoWindowImageViewPager(fragmentManager: FragmentManager, private var listFragment: List<Fragment>): FragmentPagerAdapter(fragmentManager) {
    override fun getItem(p0: Int): Fragment {
        return listFragment[p0]
    }

    override fun getCount(): Int {
        return listFragment.size
    }
}