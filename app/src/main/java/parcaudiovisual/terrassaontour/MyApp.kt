package parcaudiovisual.terrassaontour

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.OnLifecycleEvent
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.ProcessLifecycleOwner
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import io.realm.Realm
import com.squareup.leakcanary.LeakCanary
import io.realm.RealmConfiguration
import parcaudiovisual.terrassaontour.interfaces.DataLoaded
import parcaudiovisual.terrassaontour.realm.DBRealmHelper

class MyApp: Application(), LifecycleObserver, Application.ActivityLifecycleCallbacks, DataLoaded {

    private var connectionUtils = ConnectionStateMonitor()
    private var connectivityManager: ConnectivityManager? = null

    private var currentActivity: Activity? = null

    private lateinit var dbHelper: DBRealmHelper

    private var poiLoaded = false
    private var rutesLoaded = false
    private var audiovisualesLoaded = false

    private var elementsLoaded = 0

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

        setUpRealm()

        //loadDataFromDatabase()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        registerActivityLifecycleCallbacks(this)

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
    }

    fun setUpRealm(){
        Realm.init(applicationContext)
        val config = RealmConfiguration.Builder()
        config.name("tot_db")
        config.deleteRealmIfMigrationNeeded()
        Realm.setDefaultConfiguration(config.build())
    }

    fun startStatics(){
        val currentStatics = dbHelper.initStatics()
        if (currentStatics != null) dbHelper.insertUserOnServerDB(currentStatics.id,currentStatics.model,currentStatics.name,currentStatics.product)
    }


    fun trackConectivity() {
        connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (!isNetworkAvailable) showMessage("No hay conexión a internet")

        connectionUtils.enable(this)

        connectionUtils.conectionListener = object : OnConnectionChange {
            override fun conectionEnabled(enabled: Boolean) {
                if (enabled){
                    Log.i("CONECTION","Conexion restablecida")
                    Log.i("JAJA","Conection from Myapp")
                    loadDataFromDatabase()
                    startStatics()
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

    override fun onActivityResumed(activity: Activity?) {}

    override fun onActivityStarted(activity: Activity?) {}

    override fun onActivityDestroyed(activity: Activity?) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}

    override fun onActivityStopped(activity: Activity?) {}

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        currentActivity = activity
    }

    fun loadDataFromDatabase(){
        dbHelper.loadPois()
        dbHelper.loadRutes()
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

    }

    override fun allLoaded(succes: Boolean) {

    }

    fun manageAllDataLoading() {
        if (currentActivity?.localClassName == "LoadingActivity") {
            elementsLoaded++
            if (elementsLoaded == 2) {
                val intent = Intent(DBRealmHelper.BROADCAST_FIRST_DATA_LOAD)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                elementsLoaded = 0
            }
        }
    }
}