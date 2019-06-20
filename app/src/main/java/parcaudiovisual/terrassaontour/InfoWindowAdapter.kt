package parcaudiovisual.terrassaontour

import com.google.android.gms.maps.model.Marker
import com.squareup.picasso.Picasso
import  com.squareup.picasso.Callback
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Point
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import kotlinx.android.synthetic.main.custom_info_window.view.*
import java.lang.Exception
import android.view.WindowManager
import kotlin.math.round


class InfoWindowAdapter(
    private val ctxt: Context, val inflater: LayoutInflater,
    private val marcadores: HashMap<String,  Pair<Marker,PuntoInteres>>
) : InfoWindowAdapter {

    private var popup: View? = null
    private var lastMarker: Marker? = null

    init {
        popup = inflater.inflate(R.layout.custom_info_window, null)
    }

    override fun getInfoWindow(marker: Marker): View? {
        setInfoData(marker)

        return popup
    }

    @SuppressLint("InflateParams")
    override fun getInfoContents(marker: Marker): View {

        Log.i("Success","info Contents Marker")
        setInfoData(marker)
        return popup!!
    }


    private fun setInfoData(marker: Marker){
        if (lastMarker == null || lastMarker!!.id != marker.id) {
            lastMarker = marker

            val deviceHeight = ctxt.resources.displayMetrics.heightPixels
            val popUpWidth = deviceHeight / 2

            popup!!.infoRoot.layoutParams.width = popUpWidth
            var tv = popup!!.poiTitle
            tv.text = marker.title
            tv = popup!!.locationLabel
            tv.text = marker.snippet

            val allMarkerInfo = marcadores[marker.id]?.second

            if (!allMarkerInfo?.exterior!!) popup!!.locationColor.backgroundTintList = ColorStateList.valueOf(ctxt.getColor(R.color.interiorYellow))
            else popup!!.locationColor.backgroundTintList = ColorStateList.valueOf(ctxt.getColor(R.color.exteriorBlue))

            val image = allMarkerInfo.img_url

            val icon = popup!!.poiImage

            if (image == null) {
                icon.visibility = View.GONE
            } else {
                Log.i("Success","Inicio picasso")
                icon.visibility = View.VISIBLE
                Picasso.get().load(image)
                    .noFade()
                    .placeholder(R.drawable.placeholder_loading)
                    .into(icon, MarkerCallback(marker))
            }
        }
    }

    fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }

     class MarkerCallback(private var marker: Marker?) : Callback {

        override fun onError(e: Exception?) {
            Log.i("ERROR","Error loading thumbnail : $e")
        }

        override fun onSuccess() {
            Log.i("Success","Success marker")
            if (marker != null && marker!!.isInfoWindowShown) {
                Log.i("Success","marker not null")
                marker!!.showInfoWindow()
            }
        }
    }
}