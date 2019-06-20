package parcaudiovisual.terrassaontour

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.net.URL

class ServerServices {

    private val poiURL = "https://citmalumnes.upc.es/~pauel/TOT_Test/phpApp/getMarkers.php"
    private val rutesURL = "https://citmalumnes.upc.es/~pauel/TOT_Test/phpApp/getRoutes.php"
    private val insertUser = "https://citmalumnes.upc.es/~pauel/TOT_Test/phpApp/insertUser.php"

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


}


