package parcaudiovisual.terrassaontour.adapters

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_audiovisual_info_details.view.*
import kotlinx.android.synthetic.main.section_custom_row_layout.view.*
import parcaudiovisual.terrassaontour.R
import parcaudiovisual.terrassaontour.SectionModel
import parcaudiovisual.terrassaontour.interfaces.SendLinkToOpenInBrowser

class SectionRecyclerViewAdapter(
    private val context: Context,
    private val sectionModelArrayList: ArrayList<SectionModel>
    ): RecyclerView.Adapter<SectionRecyclerViewAdapter.SectionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.section_custom_row_layout, parent, false)
        return SectionViewHolder(view)
    }

    override fun getItemCount(): Int {
      return sectionModelArrayList.size
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        val sectionModel = sectionModelArrayList[position]
        holder.sectionLabel.text = sectionModel.sectionLabel

       // holder.itemRecyclerView.setHasFixedSize(true)
        holder.itemRecyclerView.isNestedScrollingEnabled = false

        val orientation = context.resources.configuration.orientation
        var spanCount = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 5 else 3

        if (sectionModel.itemArrayList.size < spanCount) spanCount = sectionModel.itemArrayList.size

        val gridLayoutManager = GridLayoutManager(context,spanCount)
        val adapter = ItemRecyclerViewAdapter(context as SendLinkToOpenInBrowser,sectionModel.itemArrayList)
        holder.itemRecyclerView.adapter = adapter
        holder.itemRecyclerView.layoutManager = gridLayoutManager
    }


    inner class SectionViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val sectionLabel = itemView.section_title
        val itemRecyclerView = itemView.section_recyclerview
    }
}