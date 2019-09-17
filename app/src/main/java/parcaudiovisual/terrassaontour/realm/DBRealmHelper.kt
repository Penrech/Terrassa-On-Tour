package parcaudiovisual.terrassaontour.realm

import android.util.Log
import io.realm.Realm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import parcaudiovisual.terrassaontour.*
import parcaudiovisual.terrassaontour.interfaces.AppStateChange
import parcaudiovisual.terrassaontour.interfaces.DataLoaded
import parcaudiovisual.terrassaontour.interfaces.StaticsUpdateFromServer
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class DBRealmHelper {

    companion object {
        var updateVersion = 0
        const val BROADCAST_CHANGE_DB = "dbUploaded"
        const val BROADCAST_CHANGE_POINTS = "dbPointsUpdated"
        const val BROADCAST_CHANGE_RUTES = "dbRutesUpdated"
        const val BROADCAST_CHANGE_AUDIOVISUALS = "dbAudiovisualsUpdated"
        const val BROADCAST_FIRST_DATA_LOAD = "dbFirstLoad"
    }

    private val serverServices = ServerServices()
    var downloadInterface: DataLoaded? = null
    var appStateInterface: AppStateChange? = null
    var staticsUpdateInterface: StaticsUpdateFromServer? = null

    var actualVersion = 0

    init {
        actualVersion = updateVersion
    }

    fun getPoisFromDB(): List<PuntoInteres>{
        val realm = Realm.getDefaultInstance()
        val puntosDeInteres = realm.where(PuntoInteres::class.java).findAll()

        val copyOfPuntos = realm.copyFromRealm(puntosDeInteres)

        Realm.compactRealm(realm.configuration)
        realm.close()

        return copyOfPuntos
    }

    fun getRandomPoint(): String? {
        val realm = Realm.getDefaultInstance()
        val puntos = realm.where(PuntoInteres::class.java).findAll() ?: return null

        val puntosArray = realm.copyFromRealm(puntos)
        puntosArray.shuffle()

        val randomId = puntosArray.first().id

        Realm.compactRealm(realm.configuration)
        realm.close()
        return randomId
    }

    fun initStatics(): Statics?{
        val realm = Realm.getDefaultInstance()
        var staticsObj = realm.where(Statics::class.java).findFirst()

        if (staticsObj == null) {
            try {
                realm.beginTransaction()
                val startStatics = realm.createObject(Statics::class.java, UUID.randomUUID().toString())
                realm.insertOrUpdate(startStatics)
                realm.commitTransaction()
                staticsObj = startStatics
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ErrorInitStatics","Error iniciando estadísticas: $e")
                return null
            }
        }

        val copyStaticsObj = realm.copyFromRealm(staticsObj)

        Realm.compactRealm(realm.configuration)
        realm.close()

        return copyStaticsObj

    }

    fun removeCurrentPreviousRouteOnAppStart(){
        val realm = Realm.getDefaultInstance()
        val staticsObj = realm.where(Statics::class.java).findFirst()

        try {
            realm.beginTransaction()
            staticsObj?.removeCurrentRoute()
            realm.commitTransaction()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ErrorRemoveRoute","Error borrando ruta: $e")
        }

        Realm.compactRealm(realm.configuration)
        realm.close()
    }

    fun getCurrentStatics(): Statics? {
        val realm = Realm.getDefaultInstance()
        val staticsObj = realm.where(Statics::class.java).findFirst()

        if (staticsObj == null) {
            realm.close()
            return null
        }

        val copyOfStatics = realm.copyFromRealm(staticsObj)

        Realm.compactRealm(realm.configuration)
        realm.close()

        return copyOfStatics
    }

    private fun udpateStaticsInsertion() {
        val realm = Realm.getDefaultInstance()
        try {
            realm.beginTransaction()
            val statics = realm.where(Statics::class.java).findFirst()
            statics?.savedOnRemoteServer = true
            realm.commitTransaction()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ErrorUpdateStatics","Error actualizando inserción de estadísticas: $e")
        }

        Realm.compactRealm(realm.configuration)
        realm.close()
    }

    fun updateStaticsAddPointVisit(pointID: String){
        val realm = Realm.getDefaultInstance()

        realm.executeTransactionAsync({
            val statics = it.where(Statics::class.java).findFirst()
            statics?.addPointVisit(pointID)
        }, {
            Realm.compactRealm(realm.configuration)
            realm.close()

        }, {

            Log.e("ErrorUpdateVisitedPoint","Error actualizando visitas de puntos: $it")
            Realm.compactRealm(realm.configuration)
            realm.close()
        })
    }

    fun updateStaticsAddAudiovisualVisit(audiovisualID: String) {
        val realm = Realm.getDefaultInstance()

        realm.executeTransactionAsync({
            val statics = it.where(Statics::class.java).findFirst()
            statics?.addAudiovisualVisit(audiovisualID)
        }, {

            Realm.compactRealm(realm.configuration)
            realm.close()

        }, {

            Log.e("ErrorUpdateVisitedAud","Error actualizando visitas de audiovisuales: $it")
            Realm.compactRealm(realm.configuration)
            realm.close()
        })


    }

    fun updateStaticsAddCurrentRoute(RutaID: String): Boolean {
        val realm = Realm.getDefaultInstance()
        val ruta = realm.where(Ruta::class.java).equalTo("id",RutaID).findFirst()
        var success = true

        if (ruta == null) {

            Realm.compactRealm(realm.configuration)
            realm.close()

            return false
        }

        try {
            realm.beginTransaction()
            val statics = realm.where(Statics::class.java).findFirst()
            if (statics == null) success = false
            statics?.setCurrentRoute(RutaID,ruta.idAudiovisuales)
            realm.commitTransaction()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ErrorAddCurrentRoute","Error añadiendo ruta actual: $e")
            success = false
        }

        Realm.compactRealm(realm.configuration)
        realm.close()

        return success
    }

    fun getAudiovisualsFromCurrentRoute() : List<String> {
        val realm = Realm.getDefaultInstance()
        val currentStatics = getCurrentStatics() ?: return listOf()
        val currentRoute = currentStatics.getCurrentRoute() ?: return listOf()

        val routeDetails = realm.where(Ruta::class.java).equalTo("id",currentRoute).findFirst() ?: return listOf()

        val copyOfRouteDetails = realm.copyFromRealm(routeDetails)
        val audiovisualList = copyOfRouteDetails.idAudiovisuales

        Realm.compactRealm(realm.configuration)
        realm.close()

        return  audiovisualList
    }

    fun getAudiovisualsFromPoint(pointID: String): List<Audiovisual>? {
        val realm = Realm.getDefaultInstance()
        val audiovisuales = realm.where(Audiovisual::class.java).equalTo("id_punto_audiovisual",pointID).findAll()

        val copyOfAudiovisuales = realm.copyFromRealm(audiovisuales)

        Realm.compactRealm(realm.configuration)
        realm.close()

        return copyOfAudiovisuales
    }

   fun getParcelableAudiovisualsFromPoint(pointID: String): ArrayList<AudiovisualParcelable>{
        val realm = Realm.getDefaultInstance()
        val audiovisuales = realm.where(Audiovisual::class.java).equalTo("id_punto_audiovisual",pointID).findAll()
        val resultado = ArrayList<AudiovisualParcelable>()

        audiovisuales.forEach{
            val audiovisualCopy = realm.copyFromRealm(it)
            val productoras = ArrayList<ClienteProductoraParcelable>()
            val clientes = ArrayList<ClienteProductoraParcelable>()
            val actores = ArrayList(audiovisualCopy.actores)
            val directores = ArrayList(audiovisualCopy.directores)
            audiovisualCopy.productoras.forEach { productora ->
                productoras.add(ClienteProductoraParcelable(productora.nombre!!,productora.link!!))
            }
            audiovisualCopy.clientes.forEach { cliente ->
                clientes.add(ClienteProductoraParcelable(cliente.nombre!!,cliente.link!!))
            }
            resultado.add(
                AudiovisualParcelable(audiovisualCopy.id,audiovisualCopy.id_punto_audiovisual,audiovisualCopy.title,audiovisualCopy.description,audiovisualCopy.img_cabecera,
                    audiovisualCopy.img_cabecera_thumbnail,audiovisualCopy.src,audiovisualCopy.year,audiovisualCopy.tipo_medio,audiovisualCopy.formato,actores,directores,productoras,clientes,audiovisualCopy.rutas_audiovisual)
            )
        }

       Realm.compactRealm(realm.configuration)
       realm.close()

       return resultado
    }


    fun getAudiovisualById(audiovisualID: String) : Audiovisual? {
        val realm = Realm.getDefaultInstance()
        val audiovisual = realm.where(Audiovisual::class.java).equalTo("id",audiovisualID).findFirst()

        val copyOfAudiovisual = realm.copyFromRealm(audiovisual)

        Realm.compactRealm(realm.configuration)
        realm.close()

        return copyOfAudiovisual
    }


    private fun deletePoisFromDB(){
        val realm = Realm.getDefaultInstance()
        val results = realm.where(PuntoInteres::class.java).findAll()

        try {
            realm.beginTransaction()
            results.deleteAllFromRealm()
            realm.commitTransaction()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ErrorRemovingPois","Error borrando puntos de interes de la DB: $e")
        }

        Realm.compactRealm(realm.configuration)
        realm.close()
    }

    fun getRutesFromDB(): List<Ruta> {
        val realm = Realm.getDefaultInstance()
        val rutas = realm.where(Ruta::class.java).findAll()

        val copyOfRutas = realm.copyFromRealm(rutas)

        Realm.compactRealm(realm.configuration)
        realm.close()

        return copyOfRutas
    }

    private fun updateStaticsAfterInsertion(data: InsertStaticsResponse){
        val realm = Realm.getDefaultInstance()
        val staticsObj = realm.where(Statics::class.java).findFirst() ?: return

        try {
            realm.beginTransaction()
            staticsObj.dayTime = data.isDayTime
            staticsObj.cleanAudiovisuals(data.audiovisualsToDelete)
            staticsObj.cleanPoints(data.pointsToDelete)
            staticsObj.cleanRoutes(data.rutesToDelete)
            realm.commitTransaction()

        } catch (e: Exception){
            e.printStackTrace()
            Log.e("ErrorUpdatingStatics","Error actualizando estadísticas despues de la inserción: $e")
        }

        Realm.compactRealm(realm.configuration)
        realm.close()

    }

    private fun deleteRoutesFromDB(){
        val realm = Realm.getDefaultInstance()
        val results = realm.where(Ruta::class.java).findAll()

        try {
            realm.beginTransaction()
            results.deleteAllFromRealm()
            realm.commitTransaction()
        } catch (e: Exception){
            e.printStackTrace()
            Log.e("ErrorDeletingRoutes","Error borrando rutas de la base de datos: $e")
        }

        Realm.compactRealm(realm.configuration)
        realm.close()
    }

    private fun deleteAudiovisualsFromDB(){
        val realm = Realm.getDefaultInstance()
        val results = realm.where(Audiovisual::class.java).findAll()

        try {
            realm.beginTransaction()
            results.deleteAllFromRealm()
            realm.commitTransaction()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ErrorDeletingAud","Error borrando audiovisuales de la base de datos: $e")
        }

        Realm.compactRealm(realm.configuration)
        realm.close()
    }

    fun insertUserOnServerDB(userID: String, userDeviceMode: String, userDeviceName: String, userDeviceType: String){
        CoroutineScope(IO).launch {
            val success = serverServices.insertUserIfNeeded(userID,userDeviceMode,userDeviceName,userDeviceType)
            val currentStatics = getCurrentStatics()

            if (!success || currentStatics == null) return@launch

            withContext(Main){
                udpateStaticsInsertion()
            }

        }
    }

    fun insertStaticsToServer(){
        CoroutineScope(Main).launch {
            val currentStatics = getCurrentStatics() ?: return@launch

            withContext(Default){
                val postData = JSONObject()
                postData.put("id",currentStatics.id)
                val pointsArray = JSONArray()
                val rutesArray = JSONArray()
                val audiovisualsArray = JSONArray()

                currentStatics.getVisitedPoints().forEach { pointsVisited->
                    val pointsVisitedJsonObject = JSONObject()
                    pointsVisitedJsonObject.put("id",pointsVisited.id)
                    pointsVisitedJsonObject.put("date",pointsVisited.date.toString())
                    pointsArray.put(pointsVisitedJsonObject)
                }

                currentStatics.getVisitedAudiovisuals().forEach { audiovisualVisited ->
                    val audiovisualVisitedJsonObject = JSONObject()
                    audiovisualVisitedJsonObject.put("id",audiovisualVisited.id)
                    audiovisualVisitedJsonObject.put("date",audiovisualVisited.date.toString())
                    audiovisualsArray.put(audiovisualVisitedJsonObject)
                }

                currentStatics.getVisitedRoutes().forEach { ruteVisited->
                    val ruteVisitedJsonObject = JSONObject()
                    ruteVisitedJsonObject.put("id",ruteVisited.id)
                    ruteVisitedJsonObject.put("date",ruteVisited.date.toString())
                    rutesArray.put(ruteVisitedJsonObject)
                }

                if (pointsArray.length() > 0) postData.put("points",pointsArray)
                if (audiovisualsArray.length() > 0) postData.put("audiovisuals",audiovisualsArray)
                if (rutesArray.length() > 0) postData.put("rutes", rutesArray)

                withContext(IO){
                    val result = serverServices.insertPeriodicallyStatics(postData)
                    if (!result.appStateError){
                        withContext(Main){
                            appStateInterface?.appStateChange(result.appActive,result.message)

                            if (currentStatics.dayTime != result.isDayTime) appStateInterface?.dayTimeChange()

                            updateStaticsAfterInsertion(result)
                        }
                    }
                }
            }
        }
    }

    fun loadRutes(){
        CoroutineScope(IO).launch {
            val result = serverServices.getRoutes()

            withContext(Main){
                if (!result.first) downloadInterface?.rutesLoaded(false)
                else {
                    if (saveRoutesToLocalDatabase(result.second)) downloadInterface?.rutesLoaded(true)
                    else downloadInterface?.rutesLoaded(false)
                }
            }
        }
    }

    fun loadPois(){
        CoroutineScope(IO).launch {
            val result = serverServices.getPOIS()

            withContext(Main) {
                if (!result.first) downloadInterface?.pointsLoaded(false)
                else {
                    if (savePoisToLocalDatabase(result.second)) downloadInterface?.pointsLoaded(true)
                    else downloadInterface?.pointsLoaded(false)
                }
            }

        }
    }

    fun loadAudiovisuals(){
        CoroutineScope(IO).launch {
            val result = serverServices.getAudiovisuals()

            withContext(Main){
                if (!result.first) downloadInterface?.audiovisualsLoaded(false)
                else {
                    if (saveAudiovisualsToLocalDatabase(result.second)) downloadInterface?.audiovisualsLoaded(true)
                    else downloadInterface?.audiovisualsLoaded(false)
                }
            }
        }
    }

    private fun saveAudiovisualsToLocalDatabase(audiovisualList: ArrayList<Audiovisual>) : Boolean {
        val realm = Realm.getDefaultInstance()

        deleteAudiovisualsFromDB()

        if (audiovisualList.isEmpty()) {

            Realm.compactRealm(realm.configuration)
            realm.close()

            return true
        }

        var success = true

        try {
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(audiovisualList)
            realm.commitTransaction()

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ErrorSavingAudiovisuals","Error guardando audiovisuales en la base de datos local: $e")
            success = false
        }

        actualVersion ++
        updateVersion ++

        Realm.compactRealm(realm.configuration)
        realm.close()

        return  success
    }

    private fun savePoisToLocalDatabase(poisList: ArrayList<PuntoInteres>): Boolean {
        val realm = Realm.getDefaultInstance()

        deletePoisFromDB()

        if (poisList.isEmpty()) {

            Realm.compactRealm(realm.configuration)
            realm.close()

            return true
        }

        var success = true


        try {
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(poisList)
            realm.commitTransaction()

        } catch (e: Exception){
            e.printStackTrace()
            Log.e("ErrorSavingPois","Error guardando puntos de interés en la base de datos local: $e")
            success = false
        }

        actualVersion ++
        updateVersion ++

        Realm.compactRealm(realm.configuration)
        realm.close()

        return success
    }

    private fun saveRoutesToLocalDatabase(rutesList: ArrayList<Ruta>) : Boolean {
        val realm = Realm.getDefaultInstance()

        deleteRoutesFromDB()

        if (rutesList.isEmpty()) {

            Realm.compactRealm(realm.configuration)
            realm.close()

            return true
        }

        var success = true

        try {
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(rutesList)
            realm.commitTransaction()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ErrorSavingRoutes","Error guardando rutas en la base de datos local: $e")
            success = false
        }

        actualVersion ++
        updateVersion ++

        Realm.compactRealm(realm.configuration)
        realm.close()

        return success
    }


}

