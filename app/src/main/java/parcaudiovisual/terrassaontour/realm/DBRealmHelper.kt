package parcaudiovisual.terrassaontour.realm

import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import io.realm.Realm
import org.json.JSONArray
import org.json.JSONObject
import parcaudiovisual.terrassaontour.*
import parcaudiovisual.terrassaontour.interfaces.AppStateChange
import parcaudiovisual.terrassaontour.interfaces.DataLoaded
import parcaudiovisual.terrassaontour.interfaces.StaticsUpdateFromServer
import java.lang.Exception
import java.security.spec.ECField
import java.util.concurrent.Callable

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

        return puntosDeInteres
    }

    fun getRandomPoint(): String? {
        val realm = Realm.getDefaultInstance()
        val puntos = realm.where(PuntoInteres::class.java).findAll() ?: return null

        val puntosArray = realm.copyFromRealm(puntos)
        puntosArray.shuffle()

        return puntosArray.first().id
    }

    fun initStatics(): Statics?{
        val realm = Realm.getDefaultInstance()
        val staticsObj = realm.where(Statics::class.java).findFirst()

        Log.i("DatosRealm","Iniciado en server: ${staticsObj?.savedOnRemoteServer}")

        if (staticsObj == null) {
            val startStatics = Statics()
            try {
                realm.beginTransaction()
                realm.insertOrUpdate(startStatics)
                realm.commitTransaction()
                return startStatics
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        return staticsObj

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
        }
    }

    fun getCurrentStatics(): Statics? {
        val realm = Realm.getDefaultInstance()
        val staticsObj = realm.where(Statics::class.java).findFirst()

        return staticsObj
    }

    fun udpateStaticsInsertion() {
        val realm = Realm.getDefaultInstance()
        try {
            realm.beginTransaction()
            val statics = realm.where(Statics::class.java).findFirst()
            statics?.savedOnRemoteServer = true
            realm.commitTransaction()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateStaticsAddPointVisit(pointID: String){
        val realm = Realm.getDefaultInstance()
        try {
            realm.beginTransaction()
            val statics = realm.where(Statics::class.java).findFirst()
            statics?.addPointVisit(pointID)
            realm.commitTransaction()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateStaticsAddCurrentRoute(RutaID: String): Boolean {
        val realm = Realm.getDefaultInstance()
        val ruta = realm.where(Ruta::class.java).equalTo("id",RutaID).findFirst()
        val statics = getCurrentStatics() ?: return false
        if (ruta == null) return false

        try {
            realm.beginTransaction()
            statics.setCurrentRoute(RutaID,ruta.idAudiovisuales)
            realm.commitTransaction()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return true
    }

    fun getAudiovisualsFromPoint(pointID: String): List<Audiovisual>? {
        val realm = Realm.getDefaultInstance()
        val audiovisuales = realm.where(Audiovisual::class.java).equalTo("id_punto_audiovisual",pointID).findAll()

        return if (audiovisuales != null) audiovisuales
        else null
    }


    fun deletePoisFromDB(){
        val realm = Realm.getDefaultInstance()
        val results = realm.where(PuntoInteres::class.java).findAll()

        try {
            realm.beginTransaction()
            results.deleteAllFromRealm()
            realm.commitTransaction()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getRutesFromDB(): List<Ruta> {
        val realm = Realm.getDefaultInstance()
        val rutas = realm.where(Ruta::class.java).findAll()

        return rutas
    }

    fun updateStaticsAfterInsertion(data: InsertStaticsResponse){
        val realm = Realm.getDefaultInstance()
        val staticsObj = realm.where(Statics::class.java).findFirst() ?: return

        try {
            realm.beginTransaction()
            staticsObj.dayTime = data.isDayTime
            staticsObj.cleanAudiovisuals(data.audiovisualsToDelete)
            staticsObj.cleanPoints(data.pointsToDelete)
            staticsObj.cleanRoutes(data.rutesToDelete)
            realm.commitTransaction()
            Log.i("StaticsAfterUpdate","$staticsObj")
        } catch (e: Exception){
            e.printStackTrace()
        }

    }

    fun deleteRoutesFromDB(){
        val realm = Realm.getDefaultInstance()
        val results = realm.where(Ruta::class.java).findAll()

        try {
            realm.beginTransaction()
            results.deleteAllFromRealm()
            realm.commitTransaction()
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun deleteAudiovisualsFromDB(){
        val realm = Realm.getDefaultInstance()
        val results = realm.where(Audiovisual::class.java).findAll()

        try {
            realm.beginTransaction()
            results.deleteAllFromRealm()
            realm.commitTransaction()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun insertUserOnServerDB(userID: String, userDeviceMode: String, userDeviceName: String, userDeviceType: String){
        AsyncInsertUser().let {
            it.execute(Callable {
                serverServices.insertUserIfNeeded(userID,userDeviceMode,userDeviceName,userDeviceType)
            })
            it.taskListener = object : OnUserInsertionCompleted {
                override fun userInsertedOnServerDB(success: Boolean?) {
                    val currentStatics = getCurrentStatics()

                    if (success == null || currentStatics == null || success == false) return

                    udpateStaticsInsertion()
                }
            }
        }
    }

    fun insertStaticsToServer(){
        AsyncStatics().let {
            val currentStatics = getCurrentStatics() ?: return
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


            it.execute(Callable {
                serverServices.insertPeriodicallyStatics(postData)
            })
            it.taskListener = object : OnServerResponseFromStaticsQuery {
                override fun onServerResponseFromStaticsQuery(result: InsertStaticsResponse?) {
                    if (result == null || result.appStateError) return

                    appStateInterface?.appStateChange(result.appActive,result.message)

                    updateStaticsAfterInsertion(result)
                }
            }
        }
    }

    fun loadRutes(){
        AsyncRutes().let {
            it.execute(Callable {
                serverServices.getRoutes()
            })
            it.taskListener = object : OnRutesDownloadCompleted {
                override fun onRutesDownloaded(result: Pair<Boolean, ArrayList<Ruta>>?) {
                    if (result == null) downloadInterface?.rutesLoaded(false)
                    else {
                        if (!result.first) downloadInterface?.rutesLoaded(false)
                        else {
                            if (saveRoutesToLocalDatabase(result.second)) downloadInterface?.rutesLoaded(true)
                            else downloadInterface?.rutesLoaded(false)
                        }
                    }
                }
            }
        }
    }

    fun loadPois(){
        AsyncPois().let {
            it.execute(Callable {
                serverServices.getPOIS()
            })
            it.taskListener = object : OnDownloadsCompleted {
                override fun onPoisDonwloaded(result: Pair<Boolean, ArrayList<PuntoInteres>>?) {
                    if (result == null) downloadInterface?.pointsLoaded(false)
                    else {
                        if (!result.first) downloadInterface?.pointsLoaded(false)
                        else {
                            if (savePoisToLocalDatabase(result.second)) downloadInterface?.pointsLoaded(true)
                            else downloadInterface?.pointsLoaded(false)
                        }
                    }
                }

            }
        }
    }

    fun loadAudiovisuals(){
        AsyncAudiovisual().let {
            it.execute(Callable {
                serverServices.getAudiovisuals()
            })
            it.taskListener = object : OnAudiovisuaDownloadCompleted {
                override fun onAudiovisualDonwloaded(result: Pair<Boolean, ArrayList<Audiovisual>>?) {
                    if (result == null) downloadInterface?.audiovisualsLoaded(false)
                    else {
                        if (!result.first) downloadInterface?.audiovisualsLoaded(false)
                        else {
                            if (saveAudiovisualsToLocalDatabase(result.second)) downloadInterface?.audiovisualsLoaded(true)
                            else downloadInterface?.audiovisualsLoaded(false)
                        }
                    }
                }
            }
        }
    }

    fun saveAudiovisualsToLocalDatabase(audiovisualList: ArrayList<Audiovisual>) : Boolean {
        val realm = Realm.getDefaultInstance()

        deleteAudiovisualsFromDB()

        if (audiovisualList.isEmpty()) return true

        var success = true

        try {
                realm.beginTransaction()
                realm.copyToRealmOrUpdate(audiovisualList)
                realm.commitTransaction()

        } catch (e: Exception) {
            e.printStackTrace()
            success = false
        }

        actualVersion ++
        updateVersion ++

        return  success
    }

    fun savePoisToLocalDatabase(poisList: ArrayList<PuntoInteres>): Boolean {
        val realm = Realm.getDefaultInstance()

        deletePoisFromDB()

        if (poisList.isEmpty()) return true

        var success = true


        try {
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(poisList)
            realm.commitTransaction()

        } catch (e: Exception){
            e.printStackTrace()
            success = false
        }


        actualVersion ++
        updateVersion ++

        return success
    }

    fun saveRoutesToLocalDatabase(rutesList: ArrayList<Ruta>) : Boolean {
        val realm = Realm.getDefaultInstance()

        deleteRoutesFromDB()

        if (rutesList.isEmpty()) return true

        var success = true


        try {
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(rutesList)
            realm.commitTransaction()
        } catch (e: Exception) {
            e.printStackTrace()
            success = false
        }


        actualVersion ++
        updateVersion ++

        return success
    }


}

