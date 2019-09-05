package parcaudiovisual.terrassaontour.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_info_element.view.*
import parcaudiovisual.terrassaontour.ClienteProductoraParcelable

import parcaudiovisual.terrassaontour.R
import parcaudiovisual.terrassaontour.adapters.InfoElementsLinkRecyclerAdapter
import parcaudiovisual.terrassaontour.adapters.InfoElementsNoLinkRecyclerAdapter
import java.net.URI
import java.net.URL

private const val ARG_PARAM1 = "param1"
private const val ARG_TYPE = "type"

class InfoElementFragment : Fragment(), InfoElementsLinkRecyclerAdapter.OnClickLink {

    var listener: OnLinkClicked? = null
    private var rootView: View? = null

    private var layoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager? = null

    private var listType: ListType? = null

    enum class ListType (value: Int) {
        LINK(1),
        NOLINK(0)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_info_element, container, false)
        setRecyclerView()
        return rootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
            context,
            androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
            false
        )
        if (context is OnLinkClicked) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    fun setRecyclerView(){
        layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
            context,
            androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
            false
        )
        val type = arguments?.getInt(ARG_TYPE) ?: return

        listType = if (type == 1) ListType.LINK else ListType.NOLINK

        val snapHelper = androidx.recyclerview.widget.PagerSnapHelper()
        snapHelper.attachToRecyclerView(rootView!!.InfoElementRV)

        rootView!!.InfoElementRV.layoutManager = layoutManager

        when (listType!!) {
            ListType.NOLINK -> {
                val arrayOfElements = arguments?.getStringArray(ARG_PARAM1) ?: return
                val adapter = InfoElementsNoLinkRecyclerAdapter(context!!,arrayOfElements.toList())
                rootView!!.InfoElementRV.adapter = adapter
            }
            ListType.LINK -> {
                @Suppress("UNCHECKED_CAST")
                val arrayOfElements = arguments?.getParcelableArray(ARG_PARAM1) as Array<ClienteProductoraParcelable> ?:return
                val adapter = InfoElementsLinkRecyclerAdapter(context!!,arrayOfElements.toList(), this)
                rootView!!.InfoElementRV.adapter = adapter
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onClickLink(urlString: String) {
        val uri = URL(urlString)
        listener?.OnLinkClicked(uri)
    }

    interface OnLinkClicked {
        fun OnLinkClicked(url: URL)
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: List<Any>) =
            InfoElementFragment().apply {
                arguments = Bundle().apply {
                    if (param1.first() is String) {
                        @Suppress("UNCHECKED_CAST")
                        val array = param1 as List<String>
                        putStringArray(ARG_PARAM1,array.toTypedArray())
                        putInt(ARG_TYPE,0)
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        val array = param1 as List<ClienteProductoraParcelable>
                        putParcelableArray(ARG_PARAM1,array.toTypedArray())
                        putInt(ARG_TYPE,1)
                    }


                }
            }
    }
}
