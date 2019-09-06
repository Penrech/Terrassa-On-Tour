package parcaudiovisual.terrassaontour

import android.content.Context
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.multiple_audiovisual_cell.view.*

class AudiovisualsListAdapter(val context: Context, var audiovisualList: ArrayList<AudiovisualParcelable>, private var ruteAudiovisualList: Array<String>, val listener: OnMaClickListener): androidx.recyclerview.widget.RecyclerView.Adapter<AudiovisualsListAdapter.AudiovisualVH>() {

    interface OnMaClickListener{
        fun onMaClickListener(audiovisual: AudiovisualParcelable)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): AudiovisualVH {
        val celda = LayoutInflater.from(context).inflate(R.layout.multiple_audiovisual_cell,p0,false)
        return AudiovisualVH(celda)
    }

    override fun getItemCount(): Int {
        return audiovisualList.size
    }

    override fun onBindViewHolder(p0: AudiovisualVH, p1: Int) {
        p0.setAudiovisualData(audiovisualList[p1], p1)
    }

    override fun onViewRecycled(holder: AudiovisualVH) {
        super.onViewRecycled(holder)

        val position = holder.position ?: return

        val audId = audiovisualList[position].id ?: return

        setBackgroundToGray(audId,holder.background)
    }

    fun setBackgroundToGray(audiovisualID: String, background: View) {
        if (ruteAudiovisualList.isNotEmpty()) {
            if (!ruteAudiovisualList.contains(audiovisualID)) {
                background.background.setColorFilter(context.getColor(R.color.googleBlueLight),PorterDuff.Mode.SRC_IN)
            } else {
                background.background.setColorFilter(context.getColor(R.color.colorAccent),PorterDuff.Mode.SRC_IN)
            }
        } else {
            background.background.setColorFilter(context.getColor(R.color.colorAccent),PorterDuff.Mode.SRC_IN)
        }
    }

    inner class AudiovisualVH(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        val image = itemView.MaImage
        val title = itemView.MaAudiovisualTitleLabel
        val rutes = itemView.MaAudiovisualRutesLabel
        val background = itemView.MaLabelBackground
        var position: Int? = null

        fun setAudiovisualData(audiovisual: AudiovisualParcelable, position: Int) {
            title.text = audiovisual.title
            Log.i("AudiovisualesMA","$audiovisual")
            if (audiovisual.rutas_audiovisual.isNotEmpty()) {
                rutes.text = "Ruta " + audiovisual.rutas_audiovisual.joinToString()
            }

            this.position = position

            setBackgroundToGray(audiovisualID = audiovisual.id!!,background = background)

            itemView.MARoot.setOnClickListener {
                listener.onMaClickListener(audiovisual)
            }


            Picasso.get().load(audiovisual.img_cabecera_thumbnail)
                .noFade()
                .placeholder(R.drawable.placeholder_loading)
                .into(image)
        }
    }
}