package parcaudiovisual.terrassaontour.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class InfoWindowImageViewPager(fragmentManager: FragmentManager, private var listFragment: List<Fragment>): FragmentPagerAdapter(fragmentManager) {
    override fun getItem(p0: Int): Fragment {
        return listFragment[p0]
    }

    override fun getCount(): Int {
        return listFragment.size
    }
}