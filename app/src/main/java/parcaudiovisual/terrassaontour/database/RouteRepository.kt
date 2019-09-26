package parcaudiovisual.terrassaontour.database

import android.app.Application
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import parcaudiovisual.terrassaontour.OnlineQueries.ServerServices

class RouteRepository(application: Application) {

    private var routeDao: RouteDao
    private var serverServices: ServerServices
    private var allRoutes: LiveData<List<Route>>

    init {
        val database = TOTDatabase.getInstance(application)
        routeDao = database.routeDao()
        allRoutes = routeDao.getAllRoutes()
        serverServices = ServerServices.getInstance()
    }

    private fun insertNewRoutes(routes: Array<Route>){
        CoroutineScope(IO).launch {
            routeDao.insertAndDeleteRoutesTransaction(routes)
        }
    }

    fun deleteSpecificRoutes(routesToDelete: ArrayList<String>){
        CoroutineScope(IO).launch {
            routeDao.deleteSpecificRoutes(routesToDelete)
        }
    }

    suspend fun selectRouteById(routeID: String): Route?{
        val route = CoroutineScope(IO).async {
            routeDao.selectRouteById(routeID)
        }

        return route.await()
    }

    fun getAllRoutes(): LiveData<List<Route>>{
        return allRoutes
    }

    suspend fun getRoutesFromServerDB(): Boolean{
        val result = CoroutineScope(IO).async {
            serverServices.getRoutes()
        }

        if (!result.await().first) return false

        insertNewRoutes(result.await().second.toTypedArray())

        return true
    }
}