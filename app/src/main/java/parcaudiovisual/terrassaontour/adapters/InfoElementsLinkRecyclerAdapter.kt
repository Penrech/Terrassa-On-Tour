package parcaudiovisual.terrassaontour.adapters

import android.content.Context
import android.graphics.Paint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.info_elements_no_link_cell.view.*
import parcaudiovisual.terrassaontour.ClienteProductoraParcelable
import parcaudiovisual.terrassaontour.R
import android.graphics.Paint.UNDERLINE_TEXT_FLAG


class InfoElementsLinkRecyclerAdapter(private val context: Context, private var list: List<ClienteProductoraParcelable>, val listener: OnClickLink) : RecyclerView.Adapter<InfoElementsLinkRecyclerAdapter.ViewHolder>() {

    interface OnClickLink{
        fun onClickLink(urlString: String)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val vistaCelda = LayoutInflater.from(context).inflate(R.layout.info_elements_no_link_cell,p0,false)
        return ViewHolder(vistaCelda)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.setTextAndLink(list[p1])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text = itemView.InfoElementNoLinkTitle

        fun setTextAndLink(clienteProductora: ClienteProductoraParcelable) {
            text.text = clienteProductora.nombre
            text.setTextColor(context.getColor(R.color.exteriorBlue))
            text.paintFlags = text.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            itemView.setOnClickListener {
                listener.onClickLink(clienteProductora.link)
            }
        }
    }
}
