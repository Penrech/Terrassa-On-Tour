package parcaudiovisual.terrassaontour.database

import android.os.Build
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

@Entity (tableName = "statics_table")
class Statics(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    var model: String = Build.MODEL,
    var name: String = Build.DEVICE,
    var product: String = Build.PRODUCT,
    var savedOnRemoteServer: Boolean = false,
    var dayTime: Boolean = true,
    var lastServerUpdate: Long = 0,
    private var visitedPoints: ArrayList<VisitHistory> = arrayListOf(),
    private var visitedAudiovisuals: ArrayList<VisitHistory> = arrayListOf(),
    private var visitedRoutes: ArrayList<VisitHistory> = arrayListOf(),
    private var currentRoute: String? = null,
    private var visitedRouteAudiovisuals: ArrayList<AudiovisualFromRouteVisited> = arrayListOf()) {

    fun getVisitedPoints(): List<VisitHistory> {
        return visitedPoints
    }

    fun getVisitedAudiovisuals(): List<VisitHistory>{
        return visitedAudiovisuals
    }

    fun getVisitedRoutes(): List<VisitHistory> {
        return visitedRoutes
    }

    fun getCurrentRoute(): String?{
        return currentRoute
    }

    fun cleanPoints(list: List<String>){
        val pointsToRemove = ArrayList<VisitHistory>()
        list.forEach { id->
            val filter = visitedPoints.firstOrNull { it.id == id }
            if (filter != null) pointsToRemove.add(filter)
        }

        visitedPoints.removeAll(pointsToRemove)
    }

    fun cleanAudiovisuals(list: List<String>){
        val audiovisualsToRemove = ArrayList<VisitHistory>()
        list.forEach { id->
            val filter = visitedAudiovisuals.firstOrNull { it.id == id }
            if (filter != null) audiovisualsToRemove.add(filter)
        }

        visitedAudiovisuals.removeAll(audiovisualsToRemove)
    }

    fun cleanRoutes(list: List<String>){
        val routesToRemove = ArrayList<VisitHistory>()
        list.forEach { id->
            val filter = visitedRoutes.firstOrNull { it.id == id }
            if (filter != null) routesToRemove.add(filter)
        }

        visitedRoutes.removeAll(routesToRemove)
    }

    fun addPointVisit(pointID: String){
        val rightNow = Calendar.getInstance()
        visitedPoints.add(VisitHistory(pointID,rightNow.timeInMillis))
    }

    fun addAudiovisualVisit(AudiovisualID: String){
        val rightNow = Calendar.getInstance()
        visitedAudiovisuals.add(VisitHistory(AudiovisualID,rightNow.timeInMillis))

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
                visitedRouteAudiovisuals.add(AudiovisualFromRouteVisited(it))
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
            visitedRouteAudiovisuals.add(AudiovisualFromRouteVisited(it))
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
            visitedRouteAudiovisuals[index].visited = true
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

        val rightNow = Calendar.getInstance()

        currentRoute?.let {
            visitedRoutes.add(VisitHistory(it,rightNow.timeInMillis))

            visitedRouteAudiovisuals.forEach {audiovisualFromRoute ->
                audiovisualFromRoute.visited = false
            }
        }


        Log.i("Rutas","La ruta $currentRoute SI ha sido completada")
    }



    class VisitHistory(var id: String, var date: Long)
    class AudiovisualFromRouteVisited(var idAudiovisual: String, var visited: Boolean = false)
}