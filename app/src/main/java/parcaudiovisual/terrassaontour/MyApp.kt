package parcaudiovisual.terrassaontour

import android.app.Activity
import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import parcaudiovisual.terrassaontour.interfaces.AppStateChange
import parcaudiovisual.terrassaontour.interfaces.DataLoaded
import parcaudiovisual.terrassaontour.interfaces.StaticsUpdateFromServer
import parcaudiovisual.terrassaontour.realm.DBRealmHelper

private const val MAPS_ACTIVITY = "MapsActivity"
private const val LOADING_ACTIVITY = "LoadingActivity"

class MyApp: Application(), LifecycleObserver, Application.ActivityLifecycleCallbacks, AppStateChange, StaticsUpdateFromServer {

    private var connectionUtils = ConnectionStateMonitor()
    private var connectivityManager: ConnectivityManager? = null

    private var currentActivity: Activity? = null

    private lateinit var dbHelper: DBRealmHelper

    private var staticsSended = false
    private var appActive = true

    private var staticsColdLoaded = false

    var activitiesOpen = ArrayList<String>()

    private val isNetworkAvailable: Boolean
        get() {
            connectivityManager?.let {
                val activeNetworkInfo = it.activeNetworkInfo
                return activeNetworkInfo != null && activeNetworkInfo.isConnected
            }
            return false
        }


    override fun onCreate() {
        super.onCreate()

        dbHelper = DBRealmHelper()
        dbHelper.appStateInterface = this
        dbHelper.staticsUpdateInterface = this

        setUpRealm()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        registerActivityLifecycleCallbacks(this)
    }

    private fun setUpRealm(){
        Realm.init(applicationContext)
        val config = RealmConfiguration.Builder()
        config.name("tot_db")
        config.deleteRealmIfMigrationNeeded()
        config.compactOnLaunch()
        Realm.setDefaultConfiguration(config.build())
    }

    fun startStatics(){
        val currentStatics = dbHelper.initStatics()
        if (currentStatics != null) {
            Log.i("Data","Envio al iniciar estadisticas al servidor")
            staticsSended = true
            dbHelper.insertUserOnServerDB(currentStatics.id,currentStatics.model,currentStatics.name,currentStatics.product)
        }
    }

    private fun sendStatics(){
        connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (isNetworkAvailable && !staticsSended) {
            Log.i("Data","Renvio estadisticas al servidor")
            dbHelper.insertStaticsToServer()
        }
    }

    private fun trackConectivity() {
        connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (!isNetworkAvailable) showMessage("No hay conexión a internet")

        connectionUtils.enable(this)

        connectionUtils.conectionListener = object : OnConnectionChange {
            override fun conectionEnabled(enabled: Boolean) {
                if (enabled){
                    Log.i("CONECTION","Conexion restablecida")
                    loadDataFromDatabase()
                    if (!staticsColdLoaded) {
                        startStatics()

                        staticsColdLoaded = true
                    }
                } else {
                    Log.i("CONECTION","Conexion perdida")
                    showMessage("No hay conexión a internet")
                }
            }
        }
    }

    private fun unTrackConectivity(){
        connectionUtils.conectionListener = null
        connectionUtils.disable(this)
    }

    fun showMessage(message: String) {
        if (currentActivity != null) {
            if (currentActivity!!.localClassName == LOADING_ACTIVITY) return

            val view = currentActivity!!.window.decorView.rootView
            val snack = Snackbar.make(view,message, Snackbar.LENGTH_LONG)

            snack.show()
        }

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        // app moved to foreground
        Log.i("LifeCycle", "Foreground")
        trackConectivity()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        // app moved to background
        Log.i("LifeCycle", "Background")
        unTrackConectivity()
    }

    override fun onActivityPaused(activity: Activity?) {}

    override fun onActivityResumed(activity: Activity?) {
        currentActivity = activity
    }

    override fun onActivityStarted(activity: Activity?) {}

    override fun onActivityDestroyed(activity: Activity?) {
        activity?.let {
            activitiesOpen.remove(it.localClassName)
        }
        if (currentActivity == activity) {
            currentActivity = null
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}

    override fun onActivityStopped(activity: Activity?) {}

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        activity?.let {
            activitiesOpen.add(it.localClassName)
        }

        //todo extraer loadingactivity en constante
        currentActivity = activity

        if (activity?.localClassName != MAPS_ACTIVITY && activity?.localClassName != LOADING_ACTIVITY){
            sendStatics()
        }

    }

    fun loadDataFromDatabase(){
        Log.i("Data","Data reloading...")
        CoroutineScope(Main).launch {
            val poiCoroutine = async(IO) { dbHelper.loadPois() }
            val routeCoroutine = async(IO) { dbHelper.loadRutes() }
            val audCoroutine = async(IO) { dbHelper.loadAudiovisuals() }

            manageAllDataLoading(poiSuccess = poiCoroutine.await(),routeSucces = routeCoroutine.await(),audSuccess = audCoroutine.await())

            Log.i("Data","All Data reload")
        }
    }

    fun pointsLoaded(succes: Boolean) {

        if (!succes) return
        if (currentActivity == null) return
        //TODO AÑADIR LUEGO AQUI TAMBIEN LA ACTIVIDAD DE VUFORIA
        if (currentActivity!!.localClassName != MAPS_ACTIVITY ) return

        val intent = Intent(DBRealmHelper.BROADCAST_CHANGE_POINTS)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    fun rutesLoaded(succes: Boolean) {

        if (!succes) return
        if (currentActivity == null) return
        if (currentActivity!!.localClassName != MAPS_ACTIVITY ) return

        val intent = Intent(DBRealmHelper.BROADCAST_CHANGE_RUTES)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    fun audiovisualsLoaded(succes: Boolean) {

        if (!succes) return

        val intent = Intent(DBRealmHelper.BROADCAST_CHANGE_AUDIOVISUALS)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }


    override fun appStateChange(appActive: Boolean, message: String?) {
        if (this.appActive != appActive){

        }
    }

    override fun reloadData() {
        loadDataFromDatabase()
    }

    override fun dayTimeChange() {
        CoroutineScope(IO).launch {
            dbHelper.loadPois()
        }

    }

    override fun onStaticsUpdateFromServer(success: Boolean) {
        Log.i("Data","Estadisticas recibidas del servidor, con validacion $success")
        if (success) staticsSended = false
    }

   /* private fun manageAllDataLoading() {
        if (currentActivity?.localClassName == "LoadingActivity") {
            elementsLoaded++
            if (elementsLoaded == 3) {
                val intent = Intent(DBRealmHelper.BROADCAST_FIRST_DATA_LOAD)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                elementsLoaded = 0
            }
        }
    }*/
   private fun manageAllDataLoading(poiSuccess: Boolean,routeSucces: Boolean, audSuccess: Boolean) {
       if (currentActivity?.localClassName == "LoadingActivity") {
           val intent = Intent(DBRealmHelper.BROADCAST_FIRST_DATA_LOAD)
           LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
       }

       pointsLoaded(poiSuccess)
       rutesLoaded(routeSucces)
   }

}