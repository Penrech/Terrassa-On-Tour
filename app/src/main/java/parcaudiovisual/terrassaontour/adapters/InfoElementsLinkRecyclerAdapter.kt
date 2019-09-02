package parcaudiovisual.terrassaontour.adapters

import android.content.Context
import android.graphics.Paint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import parcaudiovisual.terrassaontour.ClienteProductoraParcelable
import parcaudiovisual.terrassaontour.R
import android.graphics.Paint.UNDERLINE_TEXT_FLAG
import android.util.Log
import kotlinx.android.synthetic.main.equipo_info_element_audiovisual_cell.view.*


class InfoElementsLinkRecyclerAdapter(private val context: Context, private var list: List<ClienteProductoraParcelable>, val listener: OnClickLink) : RecyclerView.Adapter<InfoElementsLinkRecyclerAdapter.ViewHolder>() {

    interface OnClickLink{
        fun onClickLink(urlString: String)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val vistaCelda = LayoutInflater.from(context).inflate(R.layout.equipo_info_element_audiovisual_cell,p0,false)

        return ViewHolder(vistaCelda)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        Log.i("ClienteProductora","ClienteProductoraRecibido: ${list[p1]}")
        p0.setTextAndLink(list[p1])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text = itemView.ElementName

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
