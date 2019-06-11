package parcaudiovisual.terrassaontour

import android.content.Context
import android.support.design.widget.Snackbar
import android.view.View
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.net.URL

class MapServices(var context: Context, private var rootView: View) {

    companion object {
        const val poiURL = "https://citmalumnes.upc.es/~pauel/TOT_Test/phpApp/getMarkers.php"
        const val rutesURL = "https://citmalumnes.upc.es/~pauel/TOT_Test/phpApp/getRoutes.php"
        const val pointsOfRutesURL = "https://citmalumnes.upc.es/~pauel/TOT_Test/phpApp/getRoutePoints.php?ruta="
    }

    fun getPOIS(): ArrayList<PuntoInteres?> {
        val arrayPois = ArrayList<PuntoInteres?>()

        try {
            val json = JSONObject(getJson(poiURL))
            val array = json.getJSONArray("puntos")

            for (i:Int in 0 until array.length()) {
                val puntoInteres = PuntoInteres.jsonAReferencia(array.get(i) as JSONObject)
                arrayPois.add(puntoInteres)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return arrayPois
    }

    fun getRoutes(): ArrayList<Ruta?> {
        val arrayRutes = ArrayList<Ruta?>()

        try {
            val json = JSONObject(getJson(rutesURL))
            val array = json.getJSONArray("rutas")

            for (i: Int in 0 until array.length()) {
                val ruta = Ruta.jsonAReferencia(array.get(i) as JSONObject)
                arrayRutes.add(ruta)
            }

        } catch (e: Exception){
            e.printStackTrace()
        }

        return arrayRutes
    }

    private fun getJson(url: String): String {
        val contenidoRespuesta = StringBuilder()
        try {
            val urlConnection = URL(url).openConnection()
            val bufferedReader = BufferedReader(InputStreamReader(urlConnection.getInputStream()))
            var line: String?
            do {
                line = bufferedReader.readLine()
                contenidoRespuesta.append(line + "\n")
            } while (line != null)
            bufferedReader.close()
        } catch (e: Exception) {
            e.printStackTrace()
            printError()
        }
        return contenidoRespuesta.toString()
    }

    private fun printError() {
        val snackbar = Snackbar.make(rootView,"No se han podido recuperar los puntos de interés",Snackbar.LENGTH_LONG)
        snackbar.show()
    }

}


