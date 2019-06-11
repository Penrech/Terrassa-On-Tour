package parcaudiovisual.terrassaontour

import android.graphics.Color
import org.json.JSONException
import org.json.JSONObject

class Ruta {

    //Propiedades
    var id: String? = null
    var title: String? = null
    var color: Int? = null
    var caracteristicas = ArrayList<String>()
    var puntos = ArrayList<pointLocation>()

    companion object {
        fun jsonAReferencia(referencia: JSONObject) : Ruta? {
            try {
                val resultado = Ruta()
                val id = referencia.getString("id_ruta")
                val title = referencia.getString("title")
                val color = Color.parseColor(referencia.getString("color"))
                val caracteristicas = referencia.getJSONObject("caracteristicas")
                val puntos = referencia.getJSONArray("puntos")
                val puntosArray = ArrayList<pointLocation>()

                for (i: Int in 0 until puntos.length()) {
                    val punto = pointLocation()
                    val puntoInfo = puntos.getJSONObject(i)
                    punto.lat =  puntoInfo.getString("lat").toDouble()
                    punto.lon = puntoInfo.getString("lon").toDouble()
                    puntosArray.add(punto)
                }

                if (caracteristicas["guiada"] == true) resultado.caracteristicas.add("Guiada")
                if (caracteristicas["exterior"] == true) resultado.caracteristicas.add("Exterior")
                if (caracteristicas["interior"] == true) resultado.caracteristicas.add("Interior")

                resultado.id = id
                resultado.title = title
                resultado.color = color
                resultado.puntos = puntosArray

                return resultado
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return null
        }    }

    class pointLocation {

        //Propiedades
        var lat: Double? = null
        var lon: Double? = null
    }
}