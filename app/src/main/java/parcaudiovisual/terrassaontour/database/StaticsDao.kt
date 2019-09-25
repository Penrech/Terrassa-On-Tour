package parcaudiovisual.terrassaontour.database

import android.util.Log
import androidx.room.*
import java.util.*
import kotlin.collections.ArrayList

@Dao
interface StaticsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun initStatics(statics: Statics)

    @Query("SELECT visitedPoints FROM statics_table LIMIT 1")
    fun getVisitedPoints(): ArrayList<Statics.VisitHistory>

    @Query("SELECT visitedAudiovisuals FROM statics_table LIMIT 1")
    fun getVisitedAudiovisuals(): ArrayList<Statics.VisitHistory>

    @Query("SELECT visitedRoutes FROM statics_table LIMIT 1")
    fun getVisitedRoutes(): ArrayList<Statics.VisitHistory>

    @Query("SELECT currentRoute FROM statics_table LIMIT 1")
    fun getCurrentRoute(): String?

    @Query("UPDATE statics_table SET currentRoute = NULL")
    fun deleteCurrentRoute()

    @Query("SELECT visitedRouteAudiovisuals FROM statics_table LIMIT 1")
    fun getCurrentRouteVisitedAudiovisuals(): ArrayList<Statics.AudiovisualFromRouteVisited>

    @Query("UPDATE statics_table SET visitedPoints = :visitedPointsUpdate")
    fun updateVisitedPoints(visitedPointsUpdate: ArrayList<Statics.VisitHistory>)

    @Query("UPDATE statics_table SET visitedAudiovisuals = :visitedAudiovisualsUpdate")
    fun updateVisitedAudiovisuals(visitedAudiovisualsUpdate: ArrayList<Statics.VisitHistory>)

    @Query("UPDATE statics_table SET visitedRoutes = :visitedRoutesUpdate")
    fun updateVisitedRoutes(visitedRoutesUpdate: ArrayList<Statics.VisitHistory>)

    @Query("UPDATE statics_table SET visitedRouteAudiovisuals = :currentRouteVisitedAudiovisuals")
    fun updateCurrentRouteVisiteAudiovisuals(currentRouteVisitedAudiovisuals: ArrayList<Statics.AudiovisualFromRouteVisited>)

    @Transaction
    fun cleanPoints(pointsList: List<String>){
        val currentPoints = getVisitedPoints()
        pointsList.forEach { id->
            val filter = currentPoints.firstOrNull { it.id == id }
            currentPoints.remove(filter)
        }
        updateVisitedPoints(currentPoints)
    }

    @Transaction
    fun cleanAudiovisuals(audiovisualsList: List<String>){
        val currentAudiovisuals = getVisitedAudiovisuals()
        audiovisualsList.forEach { id->
            val filter = currentAudiovisuals.firstOrNull { it.id == id }
            if (filter != null) currentAudiovisuals.remove(filter)
        }
        updateVisitedAudiovisuals(currentAudiovisuals)
    }

    @Transaction
    fun cleanRoutes(routesList: List<String>){
        val currentRoutes = getVisitedRoutes()
        routesList.forEach { id->
            val filter = currentRoutes.firstOrNull { it.id == id }
            if (filter != null) currentRoutes.remove(filter)
        }
        updateVisitedRoutes(currentRoutes)
    }

    @Transaction
    fun addPointVisit(pointID: String){
        val rightNow = Calendar.getInstance()
        val currentPoints = getVisitedPoints()
        currentPoints.add(Statics.VisitHistory(pointID,rightNow.timeInMillis))
        updateVisitedPoints(currentPoints)
    }

    @Transaction
    fun addAudiovisualVisit(audiovisualID: String){
        val rightNow = Calendar.getInstance()
        val currentAudiovisuals = getVisitedAudiovisuals()
        currentAudiovisuals.add(Statics.VisitHistory(audiovisualID,rightNow.timeInMillis))
        updateVisitedAudiovisuals(currentAudiovisuals)
    }

    @Transaction
    fun addRouteVisit(routeID: String){
        val rightNow = Calendar.getInstance()
        val currentRoutes = getVisitedRoutes()
        currentRoutes.add(Statics.VisitHistory(routeID,rightNow.timeInMillis))
        updateVisitedRoutes(currentRoutes)
    }

    @Transaction
    fun isSameRoute(ruteID: String, routeAudiovisuals: List<String>):Boolean{
        val currentRoute = getCurrentRoute()
        if (currentRoute == null || currentRoute != ruteID) return false

        val flatVisitedIds = getCurrentRouteVisitedAudiovisuals().map { it.idAudiovisual }
        val sum = routeAudiovisuals + flatVisitedIds
        val difference = sum.groupBy { it }
            .filter { it.value.size == 1 }
            .flatMap { it.value }

        return difference.isEmpty()
    }

    @Transaction
    fun checkIfAudiovisualIsInCurrentRoute(audiovisualID: String){
        val currentRouteVisitedAudiovisuals = getCurrentRouteVisitedAudiovisuals()
        val index = currentRouteVisitedAudiovisuals.asSequence().map { it.idAudiovisual }.indexOf(audiovisualID)
        if (index != -1){
            currentRouteVisitedAudiovisuals[index].visited = true
            updateCurrentRouteVisiteAudiovisuals(currentRouteVisitedAudiovisuals)
        }
    }

    @Transaction
    fun checkIfRouteIsCompleted(){
        val currentRouteVisitedAudiovisuals = getCurrentRouteVisitedAudiovisuals()
        currentRouteVisitedAudiovisuals.forEach {
            if (!it.visited) {
                return
            }
        }

        getCurrentRoute()?.let {
            addRouteVisit(it)

            currentRouteVisitedAudiovisuals.forEach { audiovisualFromRoute ->
                audiovisualFromRoute.visited = false
            }

            updateCurrentRouteVisiteAudiovisuals(currentRouteVisitedAudiovisuals)
        }
    }

    @Transaction
    fun removeCurrentRoute(){
        if (getCurrentRoute() == null) return

        deleteCurrentRoute()
        val currentRouteVisiteAudiovisuals = getCurrentRouteVisitedAudiovisuals()
        currentRouteVisiteAudiovisuals.clear()
        updateCurrentRouteVisiteAudiovisuals(currentRouteVisiteAudiovisuals)
    }
}