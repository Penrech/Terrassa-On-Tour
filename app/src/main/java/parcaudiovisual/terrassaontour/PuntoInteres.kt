package parcaudiovisual.terrassaontour

import org.json.JSONException
import org.json.JSONObject
import java.net.URL

class PuntoInteres {

    //Propiedades
    var id: String? = null
    var title: String? = null
    var latitud: Double = 0.0
    var longitud: Double = 0.0
    var img_url: URL? = null
    var img_url_big: URL? = null
    var img_url_big_secundary: URL? = null
    var exterior: Boolean? = null
    var deDia: Boolean? = null

    companion object {
        fun jsonAReferencia(referencia: JSONObject) : PuntoInteres? {
            try {
                val resultado = PuntoInteres()
                val id = referencia.getString("id")
                val title = referencia.getString("title")

                val latitud = referencia.getString("lat").toDouble()
                val longitud = referencia.getString("lon").toDouble()

                val smallURL = URL(referencia.getString("img_url"))
                val bigURL = URL(referencia.getString("img_url_big"))
                val bigURLSecundary = URL(referencia.getString("img_url_big_secundary"))

                val exterior = referencia.getString("exterior") == "1"
                val deDia = referencia.getBoolean("dia")

                resultado.id = id
                resultado.title = title
                resultado.latitud = latitud
                resultado.longitud = longitud
                resultado.img_url = smallURL
                resultado.img_url_big = bigURL
                resultado.img_url_big_secundary = bigURLSecundary
                resultado.exterior = exterior
                resultado.deDia = deDia

                return resultado
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return null
        }
    }
}
