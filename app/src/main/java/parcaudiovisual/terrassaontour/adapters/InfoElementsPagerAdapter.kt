package parcaudiovisual.terrassaontour.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import parcaudiovisual.terrassaontour.Audiovisual
import parcaudiovisual.terrassaontour.ClienteProductora
import parcaudiovisual.terrassaontour.ClienteProductoraParcelable
import parcaudiovisual.terrassaontour.fragments.InfoElementFragment

class InfoElementsPagerAdapter(fm : FragmentManager,
                               val actorList: List<String>,
                               val directorList: List<String>,
                               val productorList: List<ClienteProductoraParcelable>,
                               val clientList: List<ClienteProductoraParcelable>) : FragmentPagerAdapter(fm) {


    private val elementsMatch = HashMap<Int,List<Any>>()

    override fun getItem(p0: Int): Fragment {
        return InfoElementFragment.newInstance(elementsMatch[p0]!!)
    }

    override fun getCount(): Int {
        var count = 0

        if (actorList.isNotEmpty()) {
            elementsMatch.put(count,actorList)
            count++
        }

        if (directorList.isNotEmpty()) {
            elementsMatch.put(count,directorList)
            count++
        }

        if (productorList.isNotEmpty()) {
            elementsMatch.put(count,productorList)
            count++
        }

        if (clientList.isNotEmpty()) {
            elementsMatch.put(count,clientList)
            count++
        }

        return count
    }
}