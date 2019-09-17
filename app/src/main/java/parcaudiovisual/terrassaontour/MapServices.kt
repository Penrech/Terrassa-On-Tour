package parcaudiovisual.terrassaontour

import android.content.Context
import com.google.android.material.snackbar.Snackbar
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

    //todo extraer
    private val directionsURL = "https://maps.googleapis.com/maps/api/directions/json?"

    fun getRoutePath(rutePoints: ArrayList<pointLocationNotRealm>): ArrayList<List<LatLng>> {
        val path:ArrayList<List<LatLng>> = arrayListOf()

        val normalizedArray = rutePoints.take(10) as ArrayList<pointLocationNotRealm>

        try {
            val json = JSONObject(getJson(createDirectionsURL(normalizedArray)))

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

    private fun createDirectionsURL(pointsArray : ArrayList<pointLocationNotRealm> ): String {
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
        val snackbar = Snackbar.make(rootView,"No hay conexión a internet para realizar la acción",
            Snackbar.LENGTH_LONG)
        snackbar.show()
    }

}


