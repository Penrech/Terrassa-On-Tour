package parcaudiovisual.terrassaontour

import android.content.Context
import android.content.res.Resources
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.net.URL

class MapServices(var context: Context, private var rootView: View) {


    private val poiURL = "https://citmalumnes.upc.es/~pauel/TOT_Test/phpApp/getMarkers.php"
    private val rutesURL = "https://citmalumnes.upc.es/~pauel/TOT_Test/phpApp/getRoutes.php"
    private val pointsOfRutesURL = "https://citmalumnes.upc.es/~pauel/TOT_Test/phpApp/getRoutePoints.php?ruta="
    //private val directionsURL = "https://maps.googleapis.com/maps/api/directions/json?origin=41.525286,0.347285&destination=41.524210,0.343122&key=${context.getString(R.string.google_maps_key)}"
    private val directionsURL = "https://maps.googleapis.com/maps/api/directions/json?"

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

    fun getRoutePath(rutePoints: ArrayList<Ruta.pointLocation>): ArrayList<List<LatLng>> {
        val path:ArrayList<List<LatLng>> = arrayListOf()

        val normalizedArray = rutePoints.take(10) as ArrayList<Ruta.pointLocation>

        try {
            val json = JSONObject(getJson(createDirectionsURL(normalizedArray)))
            Log.i("ServerResponse","${json.toString()}")
            val routes = json.getJSONArray("routes")
            val legs = routes.getJSONObject(0).getJSONArray("legs")
            val steps = legs.getJSONObject(0).getJSONArray("steps")

            for (i: Int in 0 until steps.length()) {
                val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                path.add(PolyUtil.decode(points))
            }

        } catch (e: Exception){
            e.printStackTrace()
        }

        return path
    }

    private fun createDirectionsURL(pointsArray : ArrayList<Ruta.pointLocation> ): String {
        var resultUrl = directionsURL
        val init = pointsArray.first()
        val end = pointsArray.last()

        resultUrl += "origin=${init.lat},${init.lon}&destination=${end.lat},${end.lon}&mode=walking"

        val waypoints = pointsArray
        waypoints.removeAt(0)
        waypoints.removeAt(waypoints.size - 1)

        if (waypoints.isNotEmpty()) {
            var formatedWaypoints = "&waypoints="
            waypoints.forEachIndexed { index, waypoint ->
                formatedWaypoints += "via:${waypoint.lat}%2C${waypoint.lon}"
                if (waypoints.size - 1 != index ) formatedWaypoints += "%7C"
            }

            resultUrl += formatedWaypoints
        }

        resultUrl += "&key=${context.getString(R.string.google_maps_key)}"

        return resultUrl
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
        val snackbar = Snackbar.make(rootView,"No hay conexión a internet para realizar la acción",Snackbar.LENGTH_LONG)
        snackbar.show()
    }

}


