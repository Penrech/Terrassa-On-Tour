package parcaudiovisual.terrassaontour

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.realm.RealmList
import kotlinx.android.synthetic.main.ruta_cell.view.*
import kotlinx.android.synthetic.main.rute_cell_loading.view.*

class RutasListAdapter(private val context: Context, private var list: List<Ruta>, private var listener: LoadRuteUtils): androidx.recyclerview.widget.RecyclerView.Adapter<RutasListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {

        return if (list.isEmpty()) {
            val vistaCelda = LayoutInflater.from(context)
                .inflate(R.layout.rute_cell_loading, p0, false)
            ViewHolder(vistaCelda)
        } else {
            val vistaCelda = LayoutInflater.from(context)
                .inflate(R.layout.ruta_cell, p0, false)
            ViewHolder(vistaCelda)
        }
    }

    fun update(data: List<Ruta>) {
        list = listOf()
        list = data
        notifyDataSetChanged()
    }

    interface LoadRuteUtils{
        fun loadRuteWithPoints(ruta: Ruta)
    }

    override fun getItemCount(): Int {
        return if (list.isEmpty()) {
            3
        } else {
            list.size
        }

    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        if (!list.isEmpty()) p0.setRuteData(list[p1])
        else {
            startAnimations(p0)
        }
    }

    fun startAnimations(viewHolder: ViewHolder){
        val drawable1 = viewHolder.itemView.loadingColorView.drawable
        val drawable2 = viewHolder.itemView.loadingTitleView.drawable
        val drawable3 = viewHolder.itemView.loadingSubtitleView.drawable
        if (drawable1 is Animatable) {
            (drawable1 as Animatable).start()
        }
        if (drawable2 is Animatable) {
            (drawable2 as Animatable).start()
        }
        if (drawable3 is Animatable) {
            (drawable3 as Animatable).start()
        }
    }

    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        var titleView = itemView.ruteTitle
        var colorView = itemView.ruteColor
        var caracteristicas = itemView.ruteCaracteristicas
        var rutePoints = RealmList<pointLocation>()

        fun setRuteData(ruta: Ruta){
            titleView.text = String.format("%s. %s",ruta.id,ruta.title)
            val color: Int = if (ruta.color != null) ruta.color!! else Color.DKGRAY
            colorView.background = ColorDrawable(color)
            caracteristicas.text = ruta.caracteristicas.joinToString()

            rutePoints = ruta.puntos

            itemView.ruteCellRoot.setOnClickListener {
                listener.loadRuteWithPoints(ruta)
            }
        }
    }
}
