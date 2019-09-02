package parcaudiovisual.terrassaontour.adapters

import android.content.Context
import android.content.res.Configuration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.equipo_info_audiovisual_cell.view.*
import parcaudiovisual.terrassaontour.ClienteProductoraParcelable
import parcaudiovisual.terrassaontour.R
import parcaudiovisual.terrassaontour.layoutManagers.NonScrollableGridLayoutManager

class ElementsLikeActorsRecyclerAdapter(val context: Context, val elements: ArrayList<Pair<String,List<Any>>>) : RecyclerView.Adapter<ElementsLikeActorsRecyclerAdapter.ViewHolder>() {

    private val NUM_OF_COLUMN_LANDSCAPE = 3
    private val NUM_OF_COLUMN_PORTRAIT = 2
    private val NUM_OF_COLUMN_SINGLE = 1

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val vistaCelda = LayoutInflater.from(context).inflate(R.layout.equipo_info_audiovisual_cell, p0,false)
        return ViewHolder(vistaCelda)
    }

    override fun getItemCount(): Int {
        return elements.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.setElement(elements[p1])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerText = itemView.ElementTitle
        val recyclerView = itemView.ElementsAudiovisualRV

        fun setElement(element: Pair<String,List<Any>>){
            headerText.text = element.first
            val numOfColumns = if (element.second.size > 1){
                if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) NUM_OF_COLUMN_LANDSCAPE else NUM_OF_COLUMN_PORTRAIT
            } else NUM_OF_COLUMN_SINGLE

            val layoutManager = NonScrollableGridLayoutManager(context,numOfColumns)

            if (element.second.first() is String) {
                //Adapter string
                @Suppress("UNCHECKED_CAST")
                recyclerView.adapter = InfoElementsNoLinkRecyclerAdapter(context,element.second as List<String>)
            } else {
                //Adapter con click
                @Suppress("UNCHECKED_CAST")
                recyclerView.adapter = InfoElementsLinkRecyclerAdapter(context,element.second as List<ClienteProductoraParcelable>,context as InfoElementsLinkRecyclerAdapter.OnClickLink)
            }

            recyclerView.layoutManager = layoutManager

        }
    }
}