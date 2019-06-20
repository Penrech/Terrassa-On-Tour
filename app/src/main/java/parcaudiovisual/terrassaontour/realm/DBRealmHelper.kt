package parcaudiovisual.terrassaontour.realm

import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import io.realm.Realm
import parcaudiovisual.terrassaontour.*
import parcaudiovisual.terrassaontour.interfaces.DataLoaded
import java.lang.Exception
import java.security.spec.ECField
import java.util.concurrent.Callable

class DBRealmHelper {

    companion object {
        var updateVersion = 0
        const val BROADCAST_CHANGE_DB = "dbUploaded"
        const val BROADCAST_CHANGE_POINTS = "dbPointsUploaded"
        const val BROADCAST_CHANGE_RUTES = "dbRutesUploaded"
        const val BROADCAST_FIRST_DATA_LOAD = "dbFirstLoad"
    }

    private val serverServices = ServerServices()
    var downloadInterface: DataLoaded? = null

    var actualVersion = 0

    init {
        actualVersion = updateVersion
    }

    fun getPoisFromDB(): List<PuntoInteres>{
        val realm = Realm.getDefaultInstance()
        val puntosDeInteres = realm.where(PuntoInteres::class.java).findAll()

        return puntosDeInteres
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
        } else {
            try {
                realm.beginTransaction()
                staticsObj.removeCurrentRoute()
                realm.commitTransaction()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return staticsObj
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


    fun savePoisToLocalDatabase(poisList: ArrayList<PuntoInteres>): Boolean {
        val realm = Realm.getDefaultInstance()

        deletePoisFromDB()

        if (poisList.isEmpty()) return true

        var success = true

        poisList.forEach {
            try {
                realm.beginTransaction()
                realm.copyToRealmOrUpdate(it)
                realm.commitTransaction()

            } catch (e: Exception){
                e.printStackTrace()
                success = false
            }
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

        rutesList.forEach {
            try {
                realm.beginTransaction()
                realm.copyToRealmOrUpdate(it)
                realm.commitTransaction()
            } catch (e: Exception) {
                e.printStackTrace()
                success = false
            }
        }

        actualVersion ++
        updateVersion ++

        return success
    }


}

