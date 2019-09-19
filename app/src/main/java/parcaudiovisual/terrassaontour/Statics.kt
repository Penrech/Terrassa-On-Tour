package parcaudiovisual.terrassaontour

import android.os.Build
import android.util.Log
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

open class Statics: RealmObject() {

    //todo añadir campo de tiempo para sync con servidor
    @PrimaryKey var id: String = UUID.randomUUID().toString()
    var model = Build.MODEL
    var name = Build.DEVICE
    var product = Build.PRODUCT
    var savedOnRemoteServer = false
    var dayTime = true
    var lastServerUpdate : Long = 0
    private var visitedPoints = RealmList<visitHistory>()
    private var visitedAudiovisuals = RealmList<visitHistory>()
    private var visitedRoutes = RealmList<visitHistory>()
    private var currentRoute: String? = null
    private var visitedRouteAudiovisuals = RealmList<AudiovisualFromRouteVisited>()

    fun getVisitedPoints(): List<visitHistory> {
        return visitedPoints
    }

    fun getVisitedAudiovisuals(): List<visitHistory>{
        return visitedAudiovisuals
    }

    fun getVisitedRoutes(): List<visitHistory> {
        return visitedRoutes
    }

    fun getCurrentRoute(): String?{
        return currentRoute
    }

    fun cleanPoints(list: List<String>){
        val pointsToRemove = RealmList<visitHistory>()
        list.forEach { id->
            val filter = visitedPoints.filter { it.id == id }.first()
            if (filter != null) pointsToRemove.add(filter)
        }

        visitedPoints.removeAll(pointsToRemove)
    }

    fun cleanAudiovisuals(list: List<String>){
        val audiovisualsToRemove = RealmList<visitHistory>()
        list.forEach { id->
            val filter = visitedAudiovisuals.filter { it.id == id }.first()
            if (filter != null) audiovisualsToRemove.add(filter)
        }

        visitedAudiovisuals.removeAll(audiovisualsToRemove)
    }

    fun cleanRoutes(list: List<String>){
        val routesToRemove = RealmList<visitHistory>()
        list.forEach { id->
            val filter = visitedRoutes.filter { it.id == id }.first()
            if (filter != null) routesToRemove.add(filter)
        }

        visitedRoutes.removeAll(routesToRemove)
    }

    fun addPointVisit(pointID: String){
        val newPoint = visitHistory()
        newPoint.id = pointID
        val rightNow = Calendar.getInstance()
        newPoint.date = rightNow.timeInMillis
        visitedPoints.add(newPoint)
    }

    fun addAudiovisualVisit(AudiovisualID: String){
        val newAud = visitHistory()
        newAud.id = AudiovisualID
        val rightNow = Calendar.getInstance()
        newAud.date = rightNow.timeInMillis
        visitedAudiovisuals.add(newAud)

        if (currentRoute != null){
            checkIfAudiovisualIsInCurrentRoute(AudiovisualID)
            checkIfRouteIsCompleted()
        }
    }

    fun setCurrentRoute(RuteID: String, RuteAudiovisuals: List<String>){
        CoroutineScope(Main).launch {
            if (!isSameRoute(RuteID,RuteAudiovisuals)) visitedRouteAudiovisuals.clear()
            currentRoute = RuteID

            val flatVisitedIds = visitedRouteAudiovisuals.map { it.idAudiovisual }
            val sum = RuteAudiovisuals + flatVisitedIds

            val difference = sum.groupBy { it }
                .filter { it.value.size == 1 }
                .flatMap { it.value }

            val sum2 = RuteAudiovisuals + difference

            val addIDs = sum2.groupBy { it }
                .filterNot { it.value.size == 1 }
                .flatMap { it.value }.distinct()

            val removeIDs = difference - addIDs

            addIDs.forEach {
                val newAud = AudiovisualFromRouteVisited()
                newAud.idAudiovisual = it
                visitedRouteAudiovisuals.add(newAud)
            }

            removeIDs.forEach { idToRemove ->
                visitedRouteAudiovisuals.removeAll { it.idAudiovisual == idToRemove }
            }

            Log.i("Rutas","Seteo ruta con id: $currentRoute")
            Log.i("Rutas","Dicha ruta tiene los siguientes audiovisuales:")
            visitedRouteAudiovisuals.forEach {
                Log.i("Rutas","--> ID: ${it.idAudiovisual} , Visitado: ${it.visited}")
            }
        }
        /*if (!isSameRoute(RuteID,RuteAudiovisuals)) visitedRouteAudiovisuals.clear()
        currentRoute = RuteID

        val flatVisitedIds = visitedRouteAudiovisuals.map { it.idAudiovisual }
        val sum = RuteAudiovisuals + flatVisitedIds

        val difference = sum.groupBy { it }
            .filter { it.value.size == 1 }
            .flatMap { it.value }

        val sum2 = RuteAudiovisuals + difference

        val addIDs = sum2.groupBy { it }
            .filterNot { it.value.size == 1 }
            .flatMap { it.value }.distinct()

        val removeIDs = difference - addIDs

        addIDs.forEach {
            val newAud = AudiovisualFromRouteVisited()
            newAud.idAudiovisual = it
            visitedRouteAudiovisuals.add(newAud)
        }

        removeIDs.forEach { idToRemove ->
            visitedRouteAudiovisuals.removeAll { it.idAudiovisual == idToRemove }
        }

        Log.i("Rutas","Seteo ruta con id: $currentRoute")
        Log.i("Rutas","Dicha ruta tiene los siguientes audiovisuales:")
        visitedRouteAudiovisuals.forEach {
            Log.i("Rutas","--> ID: ${it.idAudiovisual} , Visitado: ${it.visited}")
        }
*/
    }

    fun recalculateRouteStaticsIfRoutePointsChanged(RuteID: String, RuteAudiovisuals: List<String>){
        if (currentRoute != RuteID) return

        val flatVisitedIds = visitedRouteAudiovisuals.map { it.idAudiovisual }
        val sum = RuteAudiovisuals + flatVisitedIds

        val difference = sum.groupBy { it }
            .filter { it.value.size == 1 }
            .flatMap { it.value }

        val sum2 = RuteAudiovisuals + difference

        val addIDs = sum2.groupBy { it }
            .filterNot { it.value.size == 1 }
            .flatMap { it.value }.distinct()

        val removeIDs = difference - addIDs

        addIDs.forEach {
            val newAud = AudiovisualFromRouteVisited()
            newAud.idAudiovisual = it
            visitedRouteAudiovisuals.add(newAud)
        }

        removeIDs.forEach { idToRemove ->
            visitedRouteAudiovisuals.removeAll { it.idAudiovisual == idToRemove }
        }

        Log.i("Rutas","Recalculo ruta con id: $currentRoute")
        Log.i("Rutas","Dicha ruta tiene los siguientes puntos:")
        visitedRouteAudiovisuals.forEach {
            Log.i("Rutas","--> ID: ${it.idAudiovisual} , Visitado: ${it.visited}")
        }

        checkIfRouteIsCompleted()
    }

    fun removeCurrentRoute(){
        if (currentRoute == null) return

        Log.i("Rutas","Borro ruta con id: $currentRoute")
        currentRoute = null
        visitedRouteAudiovisuals.clear()


    }

    suspend fun isSameRoute(RuteID: String, RuteAudiovisuals: List<String>): Boolean{
        if (currentRoute == null || currentRoute != RuteID) return false

        val flatVisitedIds = visitedRouteAudiovisuals.map { it.idAudiovisual }
        val sum = RuteAudiovisuals + flatVisitedIds
        val difference = sum.groupBy { it }
            .filter { it.value.size == 1 }
            .flatMap { it.value }

        Log.i("Rutas","Compruebo si la ruta $RuteID es diferente a la ruta actual $currentRoute , el resultado es ${difference.isEmpty()}")

        return difference.isEmpty()
    }


    private fun checkIfAudiovisualIsInCurrentRoute(AudiovisualID: String){
        val index = visitedRouteAudiovisuals.asSequence().map { it.idAudiovisual }.indexOf(AudiovisualID)
        if (index != -1){
            visitedRouteAudiovisuals[index]?.visited = true
        }
    }

    private fun checkIfRouteIsCompleted(){
        Log.i("Rutas","Compruebo si la ruta $currentRoute ha sido completada")
        visitedRouteAudiovisuals.forEach {
            if (!it.visited) {
                Log.i("Rutas","La ruta $currentRoute NO ha sido completada")
                return
            }
        }

        val newRute = visitHistory()
        newRute.id = currentRoute
        val rightNow = Calendar.getInstance()
        newRute.date = rightNow.timeInMillis
        visitedRoutes.add(newRute)

        visitedRouteAudiovisuals.forEach {
            it.visited = false
        }

        Log.i("Rutas","La ruta $currentRoute SI ha sido completada")
    }


}
open class AudiovisualFromRouteVisited: RealmObject(){
    var idAudiovisual: String? = null
    var visited: Boolean = false
}
open class visitHistory: RealmObject(){
    var id: String? = null
    var date: Long? = null
}
class InsertStaticsResponse {

    var appStateError = true
    var appActive = true
    var message: String? = null
    var isDayTime = true
    var lastUpdate: Long? = null

    var audiovisualsToDelete = ArrayList<String>()
    var pointsToDelete = arrayListOf<String>()
    var rutesToDelete = arrayListOf<String>()
}