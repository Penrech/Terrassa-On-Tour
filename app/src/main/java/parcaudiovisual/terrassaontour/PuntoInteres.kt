package parcaudiovisual.terrassaontour

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.json.JSONException
import org.json.JSONObject
import java.net.URL

open class PuntoInteres: RealmObject() {

    //Propiedades
    @PrimaryKey var id: String? = null
    var id_target_dia: String? = null
    var id_target_noche: String? = null
    var title: String? = null
    var latitud: Double = 0.0
    var longitud: Double = 0.0
    var img_url: String? = null
    var img_url_big: String? = null
    var img_url_big_secundary: String? = null
    var exterior: Boolean? = null
    var deDia: Boolean? = null

    companion object {
        fun jsonAReferencia(referencia: JSONObject) : PuntoInteres? {
            try {
                val resultado = PuntoInteres()

                resultado.id = referencia.getString("id")
                resultado.id_target_dia = referencia.getString("id_target_dia")
                resultado.id_target_noche = referencia.getString("id_target_noche")
                resultado.title = referencia.getString("title")
                resultado.latitud = referencia.getString("lat").toDouble()
                resultado.longitud = referencia.getString("lon").toDouble()
                resultado.img_url = referencia.getString("img_url")
                resultado.img_url_big = referencia.getString("img_url_big")
                resultado.img_url_big_secundary = referencia.getString("img_url_big_secundary")
                resultado.exterior = referencia.getString("exterior") == "1"
                resultado.deDia = referencia.getBoolean("dia")

                return resultado
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return null
        }
    }
}
