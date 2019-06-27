package parcaudiovisual.terrassaontour.adapters

import android.content.Context
import android.support.v7.view.menu.ActionMenuItemView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.info_elements_no_link_cell.view.*
import parcaudiovisual.terrassaontour.R

class InfoElementsNoLinkRecyclerAdapter(private val context: Context, private var list: List<String>) : RecyclerView.Adapter<InfoElementsNoLinkRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val vistaCelda = LayoutInflater.from(context).inflate(R.layout.info_elements_no_link_cell,p0,false)
        return ViewHolder(vistaCelda)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.text.text = list[p1]
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text = itemView.InfoElementNoLinkTitle
    }
}