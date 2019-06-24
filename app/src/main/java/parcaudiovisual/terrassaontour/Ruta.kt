package parcaudiovisual.terrassaontour

import android.graphics.Color
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.json.JSONException
import org.json.JSONObject

open class Ruta: RealmObject() {

    //Propiedades
    @PrimaryKey var id: String? = null
    var title: String? = null
    var color: Int? = null
    var caracteristicas = RealmList<String>()
    var puntos = RealmList<pointLocation>()
    var idAudiovisuales = RealmList<String>()

    fun getPointsInNotRealmClass(): ArrayList<pointLocationNotRealm>{
        val result = ArrayList<pointLocationNotRealm>()
        puntos.forEach{ pointLocation ->
            result.add(pointLocationNotRealm(pointLocation.lat!!,pointLocation.lon!!))
        }

        return result
    }

    companion object {
        fun jsonAReferencia(referencia: JSONObject) : Ruta? {
            try {
                val resultado = Ruta()
                val caracteristicas = referencia.getJSONObject("caracteristicas")
                val puntos = referencia.getJSONArray("puntos")
                val audiovisualesArray = referencia.getJSONArray("id_audiovisuales")

                for (i: Int in 0 until puntos.length()) {
                    val punto = pointLocation()
                    val puntoInfo = puntos.getJSONObject(i)
                    punto.lat =  puntoInfo.getString("lat").toDouble()
                    punto.lon = puntoInfo.getString("lon").toDouble()
                    resultado.puntos.add(punto)
                }

                for (i: Int in 0 until audiovisualesArray.length()){
                    val id_audiovisual = audiovisualesArray.getString(i)
                    resultado.idAudiovisuales.add(id_audiovisual)
                }

                if (caracteristicas["guiada"] == true) resultado.caracteristicas.add("Guiada")
                if (caracteristicas["exterior"] == true) resultado.caracteristicas.add("Exterior")
                if (caracteristicas["interior"] == true) resultado.caracteristicas.add("Interior")

                resultado.id = referencia.getString("id_ruta")
                resultado.title = referencia.getString("title")
                resultado.color = Color.parseColor(referencia.getString("color"))

                return resultado
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return null
        }    }

}
open class pointLocation: RealmObject() {

    //Propiedades
    var lat: Double? = null
    var lon: Double? = null
}
class pointLocationNotRealm(val lat: Double, val lon: Double)