package parcaudiovisual.terrassaontour

import android.os.Build
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Statics: RealmObject() {

    @PrimaryKey var id: String = UUID.randomUUID().toString()
    var model = Build.MODEL
    var name = Build.DEVICE
    var product = Build.PRODUCT
    var savedOnRemoteServer = false
    private var visitedPoints = RealmList<String>()
    private var visitedAudiovisuals = RealmList<String>()
    private var visitedRoutes = RealmList<String>()
    private var currentRoute: String? = null
    private var visitedRouteAudiovisuals = RealmList<AudiovisualFromRouteVisited>()

    fun getCurrentRoute(): String?{
        return currentRoute
    }

    fun cleanPoints(){
        visitedPoints.clear()
    }

    fun cleanAudiovisuals(){
        visitedAudiovisuals.clear()
    }

    fun cleanRoutes(){
        visitedRoutes.clear()
    }

    fun addPointVisit(pointID: String){
        visitedPoints.add(pointID)
    }

    fun addAudiovisualVisit(AudiovisualID: String){
        visitedAudiovisuals.add(AudiovisualID)

        if (currentRoute != null){
            checkIfAudiovisualIsInCurrentRoute(AudiovisualID)
            checkIfRouteIsCompleted()
        }
    }

    fun setCurrentRoute(RuteID: String, RuteAudiovisuals: List<String>){
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

    }

    fun removeCurrentRoute(){
        if (currentRoute == null) return

        currentRoute = null
        visitedRouteAudiovisuals.clear()
    }

    fun isSameRoute(RuteID: String, RuteAudiovisuals: List<String>): Boolean{
        if (currentRoute == null || currentRoute != RuteID) return false

        val flatVisitedIds = visitedRouteAudiovisuals.map { it.idAudiovisual }
        val sum = RuteAudiovisuals + flatVisitedIds
        val difference = sum.groupBy { it }
            .filter { it.value.size == 1 }
            .flatMap { it.value }

        return difference.isEmpty()
    }

    private fun checkIfAudiovisualIsInCurrentRoute(AudiovisualID: String){
        val index = visitedRouteAudiovisuals.asSequence().map { it.idAudiovisual }.indexOf(AudiovisualID)
        if (index != -1){
            visitedRouteAudiovisuals[index]?.visited = true
        }
    }

    private fun checkIfRouteIsCompleted(){
        visitedRouteAudiovisuals.forEach {
            if (!it.visited) return
        }

        visitedRoutes.add(currentRoute)
        currentRoute = null
        visitedRouteAudiovisuals.clear()
    }


}
open class AudiovisualFromRouteVisited: RealmObject(){
    var idAudiovisual: String? = null
    var visited: Boolean = false
}