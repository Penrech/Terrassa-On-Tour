package parcaudiovisual.terrassaontour

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.multiple_audiovisual_cell.view.*

class AudiovisualsListAdapter(val context: Context, var audiovisualList: List<Audiovisual>, val listener: OnMaClickListener): RecyclerView.Adapter<AudiovisualsListAdapter.AudiovisualVH>() {

    interface OnMaClickListener{
        fun onMaClickListener(idAudiovisual: String)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): AudiovisualVH {
        val celda = LayoutInflater.from(context).inflate(R.layout.multiple_audiovisual_cell,p0,false)
        return AudiovisualVH(celda)
    }

    override fun getItemCount(): Int {
        return audiovisualList.size
    }

    override fun onBindViewHolder(p0: AudiovisualVH, p1: Int) {
        p0.setAudiovisualData(audiovisualList[p1])
    }

    inner class AudiovisualVH(itemView: View) : RecyclerView.ViewHolder(itemView){
        val image = itemView.MaImage
        val title = itemView.MaAudiovisualTitleLabel
        val rutes = itemView.MaAudiovisualRutesLabel
        val background = itemView.MaLabelBackground

        fun setAudiovisualData(audiovisual: Audiovisual) {
            title.text = audiovisual.title
            if (audiovisual.rutas_audiovisual.isNotEmpty()) {
                rutes.text = "Ruta " + audiovisual.rutas_audiovisual.joinToString()
            }

            itemView.MARoot.setOnClickListener {
                listener.onMaClickListener(audiovisual.id!!)
            }


            Picasso.get().load(audiovisual.img_cabecera_thumbnail)
                .noFade()
                .placeholder(R.drawable.placeholder_loading)
                .into(image)
        }
    }
}