package parcaudiovisual.terrassaontour

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_maps.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmList
import kotlinx.android.synthetic.main.drawer_menu.*
import parcaudiovisual.terrassaontour.realm.DBRealmHelper
import java.lang.Exception
import java.util.concurrent.Callable


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
    RutasListAdapter.LoadRuteUtils {

    //Variables de estado

    private var booted: Boolean = false

    //Variables mapa y localizacion

    private lateinit var mMap: GoogleMap
    private val LOCATION_REQUEST_CODE = 101
    private var MAX_RESTORE_LOCATION_TRYS = 5

    private var currentRestoreLocationTrys = 0

    private var googleLocationManager: FusedLocationProviderClient? = null
    private var googleLocationRequest: LocationRequest? = null
    private var googleLocationCallback: LocationCallback = object : LocationCallback(){
        private var lastStatus: Boolean = false
        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)

            Log.i("Locacion","Nueva Locacion")
        }

        override fun onLocationAvailability(p0: LocationAvailability?) {
            super.onLocationAvailability(p0)

            if (!booted) {
                lastStatus = p0?.isLocationAvailable!!
                booted = true
                return
            }

            if (p0?.isLocationAvailable!! && p0.isLocationAvailable != lastStatus) {
                showMessage("Ubicación activada")
                if (googleLocationManager != null) {
                    val permiso = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
                    if (permiso == PackageManager.PERMISSION_GRANTED) {
                        mMap.isMyLocationEnabled = true
                    }
                    restoreUserPosition()
                }
            } else if (!p0.isLocationAvailable && p0.isLocationAvailable != lastStatus){
                showMessage("Ubicación desactivada")
                if (googleLocationManager != null) {
                    val permiso = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
                    if (permiso == PackageManager.PERMISSION_GRANTED) {
                        mMap.isMyLocationEnabled = false
                    }
                }
            }

            lastStatus = p0.isLocationAvailable
        }
    }

    private var mapservice: MapServices? = null
    private var connectivityManager: ConnectivityManager? = null

    private var isGoogleLocationsUpdatesActive = false

    private var connectionUtils = ConnectionStateMonitor()

    //Variables marcadores

    private var noUbicationBounds = LatLngBounds.builder()
    private var marcadores: HashMap<String, Pair<Marker,PuntoInteres>> = HashMap()
    private var lastMarkerID: String? = null

    //Variables rutas

    private var currentRoutePolyline: Polyline? = null

    private var rutesLayoutManager: RecyclerView.LayoutManager? = null
    private var rutesAdapter: RutasListAdapter? = null
    private var rutesList: List<Ruta> = listOf()

    //Variables animaciones

    private var popupAnimation: Animation? = null
    private var popOutAnimation: Animation? = null

    private var popOutAnimationListener = object : Animation.AnimationListener{
        override fun onAnimationRepeat(animation: Animation?) {}

        override fun onAnimationEnd(animation: Animation?) {
            closeRuteFab.hide()
        }

        override fun onAnimationStart(animation: Animation?) {}
    }

    //Variables base de datos local

    private lateinit var dbHelper: DBRealmHelper

    private val dbChangeLocalBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action){
                DBRealmHelper.BROADCAST_CHANGE_POINTS -> reloadPoints()
                DBRealmHelper.BROADCAST_CHANGE_RUTES -> reloadRutes()
            }
        }
    }

    //Funciones de estado de la actividad

    override fun onPause() {
        super.onPause()
        disableDBHelperListener()
        unTrackGoogleLocation()
        Log.i("LIFE","Entro en pause")
    }

    override fun onResume() {
        super.onResume()
        setDBHelperListener()
        checkIfDBUpdatedWhilePause()
        trackGoogleLocation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drawer_menu)

        dbHelper = DBRealmHelper()

        checkNetworkOnCreate()
        setUpAnimations()
        setPositionButton()
        setRutesButton()
        setCloseRuteButton()
        setArButton()
        setUpRoutesRecyclerView()

        mapservice = MapServices(this,mapRoot)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    //Funciones base de datos local

    private fun setDBHelperListener(){
        val intentFilter = IntentFilter()
        intentFilter.addAction(DBRealmHelper.BROADCAST_CHANGE_POINTS)
        intentFilter.addAction(DBRealmHelper.BROADCAST_CHANGE_RUTES)
        LocalBroadcastManager.getInstance(this).registerReceiver(dbChangeLocalBroadcastReceiver, intentFilter)
    }

    private fun disableDBHelperListener(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dbChangeLocalBroadcastReceiver)
    }

    fun reloadRutes(){
        Log.i("Recargo","Recargo rutas")
        loadRutes()
    }

    fun reloadPoints(){
        Log.i("Recargo","Recargo puntos")
        loadMarkers()
    }

    fun reloadData(){
        loadMarkers()
        loadRutes()
    }

    fun checkIfDBUpdatedWhilePause(){
        if (dbHelper.actualVersion != DBRealmHelper.updateVersion) {
            reloadData()
        }
    }

    //Funciones animaciones

    private fun setUpAnimations(){
        popupAnimation = AnimationUtils.loadAnimation(this, R.anim.popup)
        popOutAnimation = AnimationUtils.loadAnimation(this, R.anim.popout)
        popOutAnimation?.setAnimationListener(popOutAnimationListener)
    }

    //Funciones de setteo

    private fun setUpRoutesRecyclerView(){
        rutesLayoutManager = LinearLayoutManager(this)
        rutesAdapter = RutasListAdapter(this,rutesList,this)

        rutes_RV.adapter = rutesAdapter
        rutes_RV.layoutManager = rutesLayoutManager
    }

    private fun setPositionButton() {

        userPosition.setOnClickListener {
            requestPermission()
        }
    }


    private fun setRutesButton(){
        rutesButton.setOnClickListener {
            openRutesMenu()
        }
    }

    private fun setCloseRuteButton(){
        closeRuteFab.hide()
        closeRuteFab.setOnClickListener {
            turnCloseRuteButton(false)
            dbHelper.removeCurrentPreviousRouteOnAppStart()
            if (currentRoutePolyline != null){
                currentRoutePolyline!!.remove()
            }
        }
    }

    private fun setArButton(){
        ARButton.setOnClickListener {
            loadRandomPoint()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        try {
            mMap.setInfoWindowAdapter(InfoWindowAdapter(applicationContext,layoutInflater,marcadores))
            manageInfoWindowChangeSizeOnScreenRotate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //Funciones Google map

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val mapSettings = mMap.uiSettings
        mapSettings.isMapToolbarEnabled = false
        mapSettings.isMyLocationButtonEnabled = false
        mapSettings.isCompassEnabled = false

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(41.510161,0.3189047), 12f))

        mMap.setOnInfoWindowClickListener(this)
        mMap.setInfoWindowAdapter(InfoWindowAdapter(applicationContext,layoutInflater,marcadores))

        googleLocationManager = FusedLocationProviderClient(this)
        googleLocationRequest = LocationRequest()
        googleLocationRequest?.interval = 500
        googleLocationRequest?.maxWaitTime = 0
        googleLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        googleLocationRequest?.smallestDisplacement = 1f

        setOnMarkerClickListener()
        loadMarkers()
        loadRutes()

        requestPermission()

    }

    fun setOnMarkerClickListener() {
        mMap.setOnMarkerClickListener {

            if (!it.isInfoWindowShown) {
                centerProperly(it)
                it.showInfoWindow()
            }

            true
        }
    }

    private fun manageInfoWindowChangeSizeOnScreenRotate() {
        var markerInfoWindowShown : Marker? = null
        for ((key, marker) in marcadores) {
            if (marker.first.isInfoWindowShown) {
                markerInfoWindowShown = marker.first
                break
            }
        }
        if (markerInfoWindowShown != null){
            centerProperly(markerInfoWindowShown)
            markerInfoWindowShown.hideInfoWindow()
            markerInfoWindowShown.showInfoWindow()
        }
    }

    private fun centerProperly(marker: Marker){

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay

        val size = Point()

        display.getRealSize(size)

        val projection = mMap.projection
        val markerPosition = marker.position
        val markerPoint = projection.toScreenLocation(markerPosition)
        val targetPoint = Point(markerPoint.x,markerPoint.y - size.y / 4)
        val targetPosition = projection.fromScreenLocation(targetPoint)
        mMap.animateCamera(CameraUpdateFactory.newLatLng(targetPosition),300, null)
    }

    //Funciones Google map - markers

    fun loadMarkers(){
        val areTherePreviousMarkers = !marcadores.isEmpty()

        for ((markerID, markerInfo) in marcadores) {
            markerInfo.first.remove()
        }
        marcadores.clear()

        noUbicationBounds = LatLngBounds.builder()

        val puntosDeInteres = dbHelper.getPoisFromDB()
        puntosDeInteres.forEach {
            val posicionPunto = LatLng(it.latitud, it.longitud)
            addCustomPoiMarker(it, posicionPunto)
        }

        if (lastMarkerID!= null) {

            val coincidence = marcadores.get(lastMarkerID!!)
            if (coincidence != null) {
                val markerToReload = coincidence.first
                markerToReload.showInfoWindow()
            }
            lastMarkerID = null
        }

        if (!areTherePreviousMarkers) moveCameraIfNoLocation()

    }

    fun addCustomPoiMarker(puntoInteres: PuntoInteres, position: LatLng){
        var drawable = R.drawable.ic_pointer_interior
        var locationText = "Int"

        if (puntoInteres.exterior == true) {
            drawable = R.drawable.ic_pointer_exterior
            locationText = "Ext"
        }

        val marker = mMap.addMarker(
            MarkerOptions().position(position)
                .title(puntoInteres.title)
                .snippet(locationText)
                .icon(bitmapDescriptorFromVector(applicationContext, drawable))
        )

        marcadores[marker.id] = Pair(marker, puntoInteres)
        noUbicationBounds.include(marker.position)
    }

    override fun onInfoWindowClick(p0: Marker?) {
        if (p0 != null) {
            val marcadorInfo = marcadores[p0.id]
            if (marcadorInfo != null) {
                val puntoInteres = marcadorInfo.second
                val imagesToDetail = DetailInfoImages(
                    puntoInteres.img_url_big.toString(),
                    puntoInteres.img_url_big_secundary.toString(),
                    if (puntoInteres.deDia!!) 1 else 0
                )

                val intent = Intent(this, InfoWindowDetail::class.java)
                intent.putExtra("imagesToDetail", imagesToDetail)
                startActivity(intent)
            }
        }

    }

    //Funciones rutas

    fun loadRutes(){
        rutesList = listOf()
        rutesList = dbHelper.getRutesFromDB()
        rutes_RV.adapter = rutesAdapter
        rutesAdapter?.update(rutesList)

        val currentStatics = dbHelper.getCurrentStatics()
        Log.i("Lista","Current Route = ${currentStatics?.getCurrentRoute()}")
        if (currentStatics == null || currentStatics.getCurrentRoute() == null) {
            removeCurrentPolyline()
        } else {
            val currentRouteDisplayed = currentStatics.getCurrentRoute()
            val ruteDetailsFromServer = rutesList.filter { it.id == currentRouteDisplayed }.firstOrNull()
            if (ruteDetailsFromServer != null) {
                if (!currentStatics.isSameRoute(ruteDetailsFromServer.id!!,ruteDetailsFromServer.idAudiovisuales)) {
                    printDirectionsRoute(ruteColor = ruteDetailsFromServer.color, rutePoints = ruteDetailsFromServer.getPointsInNotRealmClass())
                }
            }
        }
    }

    fun removeCurrentPolyline(){
        turnCloseRuteButton(false)
        if (currentRoutePolyline != null) {
            currentRoutePolyline!!.remove()
            currentRoutePolyline = null
        }
    }

    fun printDirectionsRoute(ruteColor: Int?, rutePoints: ArrayList<pointLocationNotRealm>){
        AsyncDirections().let {
            it.execute(Callable {
                mapservice!!.getRoutePath(rutePoints = rutePoints)
            })
            it.taskListener = object : OnDirectionsDownloadedCompleted {
                override fun onPoisDonwloaded(arrayList: ArrayList<List<LatLng>>?) {
                    if (currentRoutePolyline != null) {
                        currentRoutePolyline!!.remove()
                        currentRoutePolyline = null
                    }
                    Log.i("Directions","$arrayList")

                    if (arrayList != null && arrayList.isNotEmpty() ) {
                        val color = ruteColor ?: Color.GRAY
                        val polylineOptions = PolylineOptions()
                        var mergedList = listOf<LatLng>()
                        arrayList.forEach { list ->
                            polylineOptions.addAll(list)
                            mergedList += list
                        }

                        currentRoutePolyline = mMap.addPolyline(polylineOptions.color(color))
                        turnCloseRuteButton(true)
                        setRouteBounds(mergedList)

                    }
                    if (arrayList != null && arrayList.isEmpty()) {
                        dbHelper.removeCurrentPreviousRouteOnAppStart()
                    }

                }
            }
        }
    }


    fun setRouteBounds(listOfLocations: List<LatLng>){
        val bounds = LatLngBounds.builder()
        listOfLocations.forEach {
            bounds.include(it)
        }
        if (googleLocationManager != null){
            val permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            if (permiso == PackageManager.PERMISSION_GRANTED) {
                googleLocationManager!!.lastLocation.addOnSuccessListener {
                    if (it != null){
                        bounds.include(LatLng(it.latitude,it.longitude))
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 150))
                    } else {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 150))
                    }
                }
            } else {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 150))
            }
        } else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 150))
        }
    }

    fun closeRutesMenu(view: View){
        drawer_menu.closeDrawers()
    }

    private fun openRutesMenu(){
        drawer_menu.openDrawer(Gravity.START)
    }

    private fun turnCloseRuteButton(On: Boolean){
        if (On) {
            closeRuteFab.show()
            closeRuteFab.startAnimation(popupAnimation)
        } else {
            closeRuteFab.startAnimation(popOutAnimation)
        }
    }

    override fun loadRuteWithPoints(ruta: Ruta) {
        drawer_menu.closeDrawers()

        val currentStatics = dbHelper.getCurrentStatics()
        if (currentStatics != null) {
            if (currentStatics.isSameRoute(ruta.id!!,ruta.idAudiovisuales)) return
        }
        if (dbHelper.updateStaticsAddCurrentRoute(ruta.id!!)) {
            printDirectionsRoute(ruteColor = ruta.color,rutePoints = ruta.getPointsInNotRealmClass())
        } else {
            val toast = Toast.makeText(this,"Error cargando ruta",Toast.LENGTH_LONG)
            toast.show()
        }

    }



    //Funciones Localizacion

    private fun moveToUserPosition(){
        val permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permiso == PackageManager.PERMISSION_GRANTED){
            googleLocationManager?.let{ location ->
                location.locationAvailability.addOnCompleteListener {
                    if (it.isSuccessful) {
                        location.lastLocation.addOnCompleteListener { locationTask ->
                            val currentLocation = locationTask.result
                            if (currentLocation != null) {
                                val localizacionUsuario = LatLng(currentLocation.latitude,currentLocation.longitude)
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(localizacionUsuario , 18f))
                            } else {
                                showMessage("Ubicación desactivada")
                            }
                        }
                    } else {
                        showMessage("Ubicación desactivada")
                    }
                }
            }
        } else {
            showMessage("No se ha permitido la ubicación")
        }
    }

    fun restoreUserPosition(){
        currentRestoreLocationTrys += 1
        if (currentRestoreLocationTrys == MAX_RESTORE_LOCATION_TRYS) {
            currentRestoreLocationTrys = 0
            return
        }
        val permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permiso == PackageManager.PERMISSION_GRANTED){
            googleLocationManager?.let{ location ->
                location.locationAvailability.addOnCompleteListener {
                    if (it.isSuccessful) {
                        location.lastLocation.addOnCompleteListener { locationTask ->
                            val currentLocation = locationTask.result
                            if (currentLocation != null) {
                                val localizacionUsuario = LatLng(currentLocation.latitude,currentLocation.longitude)
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(localizacionUsuario , 18f))
                                currentRestoreLocationTrys = 0
                            } else {
                                restoreUserPosition()
                            }
                        }
                    } else {
                        restoreUserPosition()
                    }
                }
            }
        } else {
            showMessage("No se ha permitido la ubicación")
            currentRestoreLocationTrys = 0
        }
    }

    fun moveCameraIfNoLocation(){
        googleLocationManager?.let {
            val permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            if (permiso == PackageManager.PERMISSION_GRANTED) {
                it.locationAvailability.addOnSuccessListener { locationAvailability ->
                    if (!locationAvailability.isLocationAvailable){
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(noUbicationBounds.build(), 150))
                    }
                }

            }
        }
    }

    private fun unTrackGoogleLocation(){
        val markers = ArrayList<Marker>()
        for ((key, markerInfo) in marcadores) {
            if (markerInfo.first.isInfoWindowShown) markers.add(markerInfo.first)
        }

        if (markers.count() > 0) {
            lastMarkerID = markers.first().id
        }
        if (isGoogleLocationsUpdatesActive && googleLocationManager != null) {

            val permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            if (permiso == PackageManager.PERMISSION_GRANTED) {
                mMap.isMyLocationEnabled = false
                googleLocationManager?.removeLocationUpdates(googleLocationCallback)
                isGoogleLocationsUpdatesActive = false
            }
        }
    }

    private fun trackGoogleLocation(){
        if (!isGoogleLocationsUpdatesActive && googleLocationManager != null) {
            val permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            if (permiso == PackageManager.PERMISSION_GRANTED) {
                mMap.isMyLocationEnabled = true
                googleLocationManager!!.requestLocationUpdates(googleLocationRequest,googleLocationCallback, null)
                isGoogleLocationsUpdatesActive = true
            }
        }
    }

    //Funciones permisos de localización

    private fun requestPermission(){
        Log.i("Primero","Request permission")
        val permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permiso == PackageManager.PERMISSION_GRANTED){
            if (!isGoogleLocationsUpdatesActive) {
                mMap.isMyLocationEnabled = true
                //locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1f, this)
                googleLocationManager!!.requestLocationUpdates(googleLocationRequest,googleLocationCallback, null)
                isGoogleLocationsUpdatesActive= true
            }
            moveToUserPosition()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            LOCATION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                   showMessage("No se ha permitido la ubicación")
                }
                else{
                    requestPermission()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //Funciones conexión

    fun checkNetworkOnCreate(){
        connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (!connectionUtils.checkConection(this)) showMessage("No hay conexión a internet")
    }

    //Funciones utiles, herramientas UI, etc.

    fun showMessage(message: String) {
        val snack = Snackbar.make(mapRoot ,message, Snackbar.LENGTH_LONG)
        snack.show()
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        val size = 120
        vectorDrawable!!.setBounds(0, 0, size, size)
        val bitmap =
            Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    //Funciones de prueba

    fun loadRandomPoint(){
        val idPuntoAleatorio = dbHelper.getRandomPoint() ?: return
        val audiovisualesRutaActual = dbHelper.getAudiovisualsFromCurrentRoute().toTypedArray()
        Log.i("AudiovisualesEnRuta","${audiovisualesRutaActual.size}")

        val audiovisualesPunto = dbHelper.getParcelableAudiovisualsFromPoint(idPuntoAleatorio)

        val intent = Intent(this, MultipleAudiovisualActivity::class.java)
        intent.putExtra("IDPOINT", idPuntoAleatorio)
        intent.putExtra("AUDIOVISUALES",audiovisualesPunto)
        intent.putExtra("RUTEAUD",audiovisualesRutaActual)
        startActivity(intent)
    }

  /*  fun loadRutes(){
        AsyncRutes().let {
            it.execute(Callable {
                mapservice!!.getRoutes()
            })
            it.taskListener = object  : OnRutesDownloadCompleted {
                override fun onRutesDownloaded(arrayList: ArrayList<Ruta?>?) {
                    rutesList.clear()
                    arrayList?.forEach { ruta ->
                        if (ruta != null) rutesList.add(ruta)
                    }
                    rutes_RV.adapter = rutesAdapter
                    //rutesAdapter?.notifyDataSetChanged()
                }
            }
        }
    }*/



    /*
    *  fun loadMarkers(){
        val areTherePreviousMarkers = !marcadores.isEmpty()

        for ((markerID, markerInfo) in marcadores) {
            markerInfo.first.remove()
        }
        marcadores.clear()

        noUbicationBounds = LatLngBounds.builder()

        val puntosDeInteres = dbHelper.getPoisFromDB()
        puntosDeInteres.forEach {
            val posicionPunto = LatLng(it.latitud, it.longitud)
            addCustomPoiMarker(it, posicionPunto)
        }

        if (lastMarkerID!= null) {

            val coincidence = marcadores.get(lastMarkerID!!)
            if (coincidence != null) {
                val markerToReload = coincidence.first
                markerToReload.showInfoWindow()
            }
            lastMarkerID = null
        }

        if (!areTherePreviousMarkers) moveCameraIfNoLocation()

    }
    * */



}
