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

class MyApp: Application(), LifecycleObserver, Application.ActivityLifecycleCallbacks, DataLoaded, AppStateChange, StaticsUpdateFromServer {

    private var connectionUtils = ConnectionStateMonitor()
    private var connectivityManager: ConnectivityManager? = null

    private var currentActivity: Activity? = null

    private lateinit var dbHelper: DBRealmHelper

    private var staticsSended = false
    private var appActive = true

    private var dataColdLoaded = false
    private var staticsColdLoaded = false

    private var elementsLoaded = 0

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
        dbHelper.downloadInterface = this
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
            staticsSended = true
            dbHelper.insertUserOnServerDB(currentStatics.id,currentStatics.model,currentStatics.name,currentStatics.product)
        }
    }

    private fun sendStatics(){
        connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (isNetworkAvailable && !staticsSended) {
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
                    if (!dataColdLoaded && !staticsColdLoaded) {
                        loadDataFromDatabase()
                        startStatics()

                        dataColdLoaded = true
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
            if (currentActivity!!.localClassName == "LoadingActivity") return

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
        if (activity?.localClassName == "LoadingActivity" ) {
            loadDataFromDatabase()
            startStatics()
        }

        sendStatics()
    }

    fun loadDataFromDatabase(){
        Log.i("Data","Data reloading...")
        CoroutineScope(Main).launch {
            val poiCoroutine = async(IO) { dbHelper.loadPois() }
            val routeCoroutine = async(IO) { dbHelper.loadRutes() }
            val audCoroutine = async(IO) { dbHelper.loadAudiovisuals() }

            poiCoroutine.await()
            routeCoroutine.await()
            audCoroutine.await()

            manageAllDataLoading()

            Log.i("Data","All Data reload")
        }
    }

    override fun pointsLoaded(succes: Boolean) {
        manageAllDataLoading()
        val intent = Intent(DBRealmHelper.BROADCAST_CHANGE_POINTS)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun rutesLoaded(succes: Boolean) {
        manageAllDataLoading()
        val intent = Intent(DBRealmHelper.BROADCAST_CHANGE_RUTES)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun audiovisualsLoaded(succes: Boolean) {
        manageAllDataLoading()
        val intent = Intent(DBRealmHelper.BROADCAST_CHANGE_AUDIOVISUALS)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun allLoaded(succes: Boolean) {
        if (currentActivity?.localClassName == "LoadingActivity") {
            val intent = Intent(DBRealmHelper.BROADCAST_FIRST_DATA_LOAD)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
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
   private fun manageAllDataLoading() {
       if (currentActivity?.localClassName == "LoadingActivity") {
           val intent = Intent(DBRealmHelper.BROADCAST_FIRST_DATA_LOAD)
           LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
       }
   }

}