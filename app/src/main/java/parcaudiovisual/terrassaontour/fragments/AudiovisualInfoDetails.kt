package parcaudiovisual.terrassaontour.fragments


import android.accounts.AuthenticatorDescription
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_audiovisual_info_details.view.*
import parcaudiovisual.terrassaontour.Audiovisual
import parcaudiovisual.terrassaontour.ClienteProductoraParcelable

import parcaudiovisual.terrassaontour.R
import parcaudiovisual.terrassaontour.adapters.InfoElementsPagerAdapter
import parcaudiovisual.terrassaontour.adapters.InfoWindowImageViewPager
import parcaudiovisual.terrassaontour.realm.DBRealmHelper

private const val TITLE = "title"
private const val YEAR = "year"
private const val DESCRIPTION = "description"
private const val IMG = "image"
private const val ACTOR = "actores"
private const val DIRECTOR = "directores"
private const val PRODUCTOR = "productores"
private const val CLIENT = "clientes"

class AudiovisualInfoDetails : Fragment() {
    private var rootView: View? = null
    private var mInfoElementsPagerAdapter: InfoElementsPagerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_audiovisual_info_details, container, false)

        loadData()

        return rootView
    }

    private fun loadData(){
        rootView!!.AudiovisualInfoTitleLabel.text = arguments?.getString(TITLE)
        rootView!!.AudiovisualInfoYearLabel.text = arguments?.getString(YEAR)

        val actoresList = (arguments?.getStringArray(ACTOR) ?: emptyArray()).toList()
        val directoresList = (arguments?.getStringArray(DIRECTOR) ?: emptyArray()).toList()
        @Suppress("UNCHECKED_CAST")
        val productorasArray: Array<ClienteProductoraParcelable> = arguments?.getParcelableArray(PRODUCTOR) as Array<ClienteProductoraParcelable>
        @Suppress("UNCHECKED_CAST")
        val clienteList: Array<ClienteProductoraParcelable> = arguments?.getParcelableArray(CLIENT) as Array<ClienteProductoraParcelable>

        mInfoElementsPagerAdapter = InfoElementsPagerAdapter(fragmentManager!!,actoresList,directoresList,productorasArray.toList(),clienteList.toList())

        rootView!!.ElementsInfoViewPager.adapter = mInfoElementsPagerAdapter

        rootView!!.ElementsInfoViewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(rootView!!.ElementsInfoTabs))
        rootView!!.ElementsInfoTabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(rootView!!.ElementsInfoViewPager))

        Picasso.get()
            .load(arguments?.getString(IMG))
            .noFade()
            .placeholder(R.drawable.placeholder_loading_big)
            .into(rootView!!.headerInfoImageView)
    }

    companion object {
        fun newInstance(title: String?,
                        year: String?,
                        description: String?,
                        img: String?,
                        actores: List<String>,
                        directores: List<String>,
                        productoras: List<ClienteProductoraParcelable>,
                        clientes: List<ClienteProductoraParcelable>): AudiovisualInfoDetails {
            val fragment = AudiovisualInfoDetails()
            val args = Bundle()

            args.putString(TITLE,title)
            args.putString(YEAR,year)
            args.putString(DESCRIPTION,description)
            args.putString(IMG,img)
            args.putStringArray(ACTOR,actores.toTypedArray())
            args.putStringArray(DIRECTOR,directores.toTypedArray())
            args.putParcelableArray(PRODUCTOR,productoras.toTypedArray())
            args.putParcelableArray(CLIENT,clientes.toTypedArray())
            fragment.arguments = args
            return fragment
        }
    }
}
