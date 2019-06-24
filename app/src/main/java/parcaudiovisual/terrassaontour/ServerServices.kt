package parcaudiovisual.terrassaontour

import android.net.Uri
import android.util.Log
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.lang.StringBuilder
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection

class ServerServices {

    private val poiURL = "https://citmalumnes.upc.es/~pauel/TOT_Test/phpApp/getMarkers.php"
    private val rutesURL = "https://citmalumnes.upc.es/~pauel/TOT_Test/phpApp/getRoutes.php"
    private val audiovisualsURL = "https://citmalumnes.upc.es/~pauel/TOT_Test/phpApp/getAllAudiovisuals.php"
    private val insertUser = "https://citmalumnes.upc.es/~pauel/TOT_Test/phpApp/insertUser.php"
    private val insertAllStatics = "https://citmalumnes.upc.es/~pauel/TOT_Test/phpApp/InsertAllStatistics.php"

    fun insertUserIfNeeded(userID: String, userDeviceMode: String, userDeviceName: String, userDeviceType: String): Boolean{
        val url = insertUser + "?id=$userID&model=$userDeviceMode&name=$userDeviceName&type=$userDeviceType"
        try {
            val json = JSONObject(getJson(url))
            val success = json.getString("result") == "success"
            return success
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun insertPeriodicallyStatics(postData: JSONObject): InsertStaticsResponse {
        val response = InsertStaticsResponse()

        try {
            val json = JSONObject(getJsonPOST(insertAllStatics,postData))
            val appState = json.getJSONObject("appState")
            val audiovisuals = json.getJSONObject("audiovisuals")
            val successAudiovisuals = audiovisuals.getJSONArray("success")
            val points = json.getJSONObject("points")
            val successPoints = points.getJSONArray("success")
            val rutes = json.getJSONObject("rutes")
            val successRutes = rutes.getJSONArray("success")

            response.appStateError = appState.getBoolean("error")
            response.appActive = appState.getBoolean("appActive")
            response.message = appState.getString("message")
            response.isDayTime = json.getBoolean("isDayTime")

            for (i: Int in 0 until successAudiovisuals.length()){
                response.audiovisualsToDelete.add(successAudiovisuals.getString(i))
            }

            for (i: Int in 0 until successPoints.length()){
                response.pointsToDelete.add(successPoints.getString(i))
            }

            for (i: Int in 0 until successRutes.length()) {
                response.rutesToDelete.add(successRutes.getString(i))
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }

        return response
    }

    fun getPOIS(): Pair<Boolean,ArrayList<PuntoInteres>> {
        val arrayPois = ArrayList<PuntoInteres>()

        try {
            val json = JSONObject(getJson(poiURL))
            val array = json.getJSONArray("puntos")
            for (i:Int in 0 until array.length()) {
                val puntoInteres = PuntoInteres.jsonAReferencia(array.get(i) as JSONObject)
                if (puntoInteres != null) arrayPois.add(puntoInteres)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return Pair(false, arrayPois)
        }
        return Pair(true,arrayPois)
    }

    fun getAudiovisuals(): Pair<Boolean, ArrayList<Audiovisual>> {
        val arrayAudiovisuals = ArrayList<Audiovisual>()

        try {
            val json = JSONObject(getJson(audiovisualsURL))
            val array = json.getJSONArray("audiovisuales")
            for (i: Int in 0 until  array.length()){
                val audiovisual = Audiovisual.jsonAReferencia(array.get(i) as JSONObject)
                if (audiovisual != null) arrayAudiovisuals.add(audiovisual)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return Pair(false,arrayAudiovisuals)
        }

        return Pair(true, arrayAudiovisuals)
    }

    fun getRoutes(): Pair<Boolean, ArrayList<Ruta>> {
        val arrayRutes = ArrayList<Ruta>()

        try {
            val json = JSONObject(getJson(rutesURL))
            val array = json.getJSONArray("rutas")

            for (i: Int in 0 until array.length()) {
                val ruta = Ruta.jsonAReferencia(array.get(i) as JSONObject)
                if (ruta != null) arrayRutes.add(ruta)
            }

        } catch (e: Exception){
            e.printStackTrace()
            return Pair(false,arrayRutes)
        }

        return Pair(true,arrayRutes)
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
        }
        return contenidoRespuesta.toString()
    }

    private fun getJsonPOST(url: String, postData: JSONObject): String {
        val contenidoRespuesta = StringBuilder()
        try {

            val builder = Uri.Builder()
            builder.encodedPath(url)
            builder.appendQueryParameter("id",postData.getString("id"))


            if (postData.has("points")) {
                builder.appendQueryParameter("points",postData.getJSONArray("points").toString())
            }

            if (postData.has("audiovisuals")) {
                builder.appendQueryParameter("audiovisuals",postData.getJSONArray("audiovisuals").toString())
            }

            if (postData.has("rutes")) {
                builder.appendQueryParameter("rutes",postData.getJSONArray("rutes").toString())
            }

            val query = builder.build().toString()
            Log.i("Statics","$query")
            val urlConnection = URL(query).openConnection()
            val bufferedReader = BufferedReader(InputStreamReader(urlConnection.getInputStream()))
            var line: String?
            do {
                line = bufferedReader.readLine()
                contenidoRespuesta.append(line + "\n")
            } while (line != null)
            bufferedReader.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return contenidoRespuesta.toString()
    }


}


