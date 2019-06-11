package parcaudiovisual.terrassaontour

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.WindowManager
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.drawer_menu.*
import java.util.concurrent.Callable


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
    RutasListAdapter.LoadRuteUtils {


    private lateinit var mMap: GoogleMap
    private val LOCATION_REQUEST_CODE = 101
    private var MAX_RESTORE_LOCATION_TRYS = 5

    private var booted: Boolean = false

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

    private var noUbicationBounds = LatLngBounds.builder()
    private  var mMarkerArray = ArrayList<Marker>()
    private var currentRoutePolyline: Polyline? = null

    private var connectionUtils = ConnectionStateMonitor()

    private var lastMarkerInfo: PuntoInteres? = null

    private val isNetworkAvailable: Boolean
        get() {
            connectivityManager?.let {
                val activeNetworkInfo = it.activeNetworkInfo
                return activeNetworkInfo != null && activeNetworkInfo.isConnected
            }
            return false
        }

    private var markerImages: HashMap<String, ArrayList<Uri>> = HashMap()

    private var rutesLayoutManager: RecyclerView.LayoutManager? = null
    private var rutesAdapter: RutasListAdapter? = null
    private var rutesList = ArrayList<Ruta>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drawer_menu)

        setPositionButton()
        setRutesButton()
        setUpRoutesRecyclerView()

        mapservice = MapServices(this,mapRoot)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    private fun setUpRoutesRecyclerView(){
        rutesLayoutManager = LinearLayoutManager(this)
        rutesAdapter = RutasListAdapter(this,rutesList,this)

        rutes_RV.adapter = rutesAdapter
        rutes_RV.layoutManager = rutesLayoutManager
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val mapSettings = mMap.uiSettings
        mapSettings.isMapToolbarEnabled = false
        mapSettings.isMyLocationButtonEnabled = false
        mapSettings.isCompassEnabled = false

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(41.510161,0.3189047), 12f))

        Log.i("IMAGES","$markerImages")
        mMap.setOnInfoWindowClickListener(this)
        mMap.setInfoWindowAdapter(InfoWindowAdapter(applicationContext,layoutInflater,markerImages,mMap))

        googleLocationManager = FusedLocationProviderClient(this)
        googleLocationRequest = LocationRequest()
        googleLocationRequest?.interval = 500
        googleLocationRequest?.maxWaitTime = 0
        googleLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        googleLocationRequest?.smallestDisplacement = 1f

        requestPermission()

    }

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

    private fun hideSystemUI() {

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    fun showMessage(message: String) {
        val snack = Snackbar.make(mapRoot ,message, Snackbar.LENGTH_LONG)

        snack.show()
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

    fun loadPois(){
        AsyncPois().let {
            it.execute(Callable {
                mapservice!!.getPOIS()
            })
            it.taskListener = object : OnDownloadsCompleted {
                override fun onPoisDonwloaded(arrayList: ArrayList<PuntoInteres?>?) {
                    val areTherePreviousMarkers = !mMarkerArray.isEmpty()

                    mMarkerArray.forEach {marker ->
                        marker.remove()
                    }
                    mMarkerArray.clear()
                    noUbicationBounds = LatLngBounds.builder()
                    arrayList?.forEach { puntoInteres ->
                        if (puntoInteres != null){
                            val posicionPunto = LatLng(puntoInteres.latitud, puntoInteres.longitud)
                            addCustomPoiMarker(puntoInteres,posicionPunto)
                        }
                    }
                    if (lastMarkerInfo!= null) {

                        val coincidence = mMarkerArray.filter { marker ->  (marker.tag as PuntoInteres).id == lastMarkerInfo!!.id }
                        if (coincidence.isNotEmpty()) {
                            val markerToReload = coincidence.first()
                            markerToReload.showInfoWindow()
                        }
                        lastMarkerInfo = null
                    }

                    if (!areTherePreviousMarkers) moveCameraIfNoLocation()
                }
            }
        }
    }

    fun loadRutes(){
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
    }

    fun printDirectionsRoute(currentRoute: Ruta){
        AsyncDirections().let {
            it.execute(Callable {
                mapservice!!.getRoutePath(currentRoute.puntos)
            })
            it.taskListener = object : OnDirectionsDownloadedCompleted {
                override fun onPoisDonwloaded(arrayList: ArrayList<List<LatLng>>?) {
                    if (currentRoutePolyline != null) {
                        currentRoutePolyline!!.remove()
                        currentRoutePolyline = null
                    }

                    if (arrayList?.first() != null) {
                        val color = if (currentRoute.color == null) Color.GRAY else currentRoute.color!!
                        currentRoutePolyline =
                                mMap.addPolyline(PolylineOptions().addAll(arrayList.first()).color(color))
                        setRouteBounds(arrayList.first())
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

        marker.tag = puntoInteres

        if (puntoInteres.id != null) {
            val images = ArrayList<Uri>()
            images.add(Uri.parse(puntoInteres.img_url.toString()))
            images.add(Uri.parse(puntoInteres.img_url_big.toString()))
            markerImages[marker.id] = images
        }

        mMarkerArray.add(marker)
        noUbicationBounds.include(marker.position)
    }

    fun trackConectivity() {
        connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (!isNetworkAvailable) showMessage("No hay conexión a internet")

        connectionUtils.enable(this)

        connectionUtils.conectionListener = object : OnConnectionChange {
            override fun conectionEnabled(enabled: Boolean) {
                if (enabled){
                    Log.i("CONECTION","Conexion restablecida")
                    loadPois()
                    loadRutes()

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

    override fun onPause() {
        super.onPause()
        unTrackConectivity()
        unTrackGoogleLocation()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
       trackConectivity()
        trackGoogleLocation()
    }

    private fun unTrackGoogleLocation(){
        val markers = mMarkerArray.filter { it.isInfoWindowShown }
        if (markers.count() > 0) {
            lastMarkerInfo = markers.first().tag as PuntoInteres
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


    override fun onInfoWindowClick(p0: Marker?) {
        Log.i("Marker","Imagen a mostrar: ${markerImages[p0?.id]?.get(1)}")

        val puntoInteres = p0?.tag as PuntoInteres
        val imagesToDetail = DetailInfoImages(puntoInteres.img_url_big.toString(),puntoInteres.img_url_big_secundary.toString(),if (puntoInteres.deDia!!) 1 else 0)

        val intent = Intent(this,InfoWindowDetail::class.java)
        intent.putExtra("imagesToDetail",imagesToDetail)
        startActivity(intent)

    }

    fun closeRutesMenu(view: View){
        drawer_menu.closeDrawers()
    }

    private fun openRutesMenu(){
        drawer_menu.openDrawer(Gravity.START)
    }

    override fun loadRuteWithPoints(ruta: Ruta) {
       printDirectionsRoute(ruta)
    }
}
