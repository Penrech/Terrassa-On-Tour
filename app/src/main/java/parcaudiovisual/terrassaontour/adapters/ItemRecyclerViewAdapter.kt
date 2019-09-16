package parcaudiovisual.terrassaontour.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.equipo_info_element_audiovisual_cell.view.*
import parcaudiovisual.terrassaontour.R
import parcaudiovisual.terrassaontour.interfaces.SendLinkToOpenInBrowser

class ItemRecyclerViewAdapter(
    private val linkInterface: SendLinkToOpenInBrowser,
    private val arrayList: ArrayList<Pair<String,String?>>
) : RecyclerView.Adapter<ItemRecyclerViewAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.equipo_info_element_audiovisual_cell, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.itemLabel.text = arrayList[position].first

        if (arrayList[position].second != null) {
            holder.itemLabel.paintFlags = holder.itemLabel.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            var link = arrayList[position].second

            if (link!!.substring(0..3) != "http") {
                link = "http://$link"
            }

            holder.itemLabel.setOnClickListener {
                linkInterface.openInBrowser(link)
            }

        } else {
            holder.itemLabel.background = null
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemLabel = itemView.ElementName
    }


}