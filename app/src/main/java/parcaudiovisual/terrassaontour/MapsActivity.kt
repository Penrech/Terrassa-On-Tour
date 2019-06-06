package parcaudiovisual.terrassaontour

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_maps.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.Uri
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
import java.lang.IllegalArgumentException
import java.util.concurrent.Callable


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, LocationListener {


    private lateinit var mMap: GoogleMap
    private val LOCATION_REQUEST_CODE = 101
    private var locationManager: LocationManager? = null

    private var googleLocationManager: FusedLocationProviderClient? = null
    private var googleLocationRequest: LocationRequest? = null
    private var googleLocationCallback: LocationCallback = object : LocationCallback(){
        private var lastLocation: Location? = null
        private var lastStatus: Boolean = false

        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)

            if (this.lastLocation == null) {
                moveToUserPosition()
            }
            this.lastLocation = p0?.lastLocation

        }

        override fun onLocationAvailability(p0: LocationAvailability?) {
            super.onLocationAvailability(p0)

            if (p0?.isLocationAvailable!! && p0.isLocationAvailable != lastStatus) {
                showMessage("Ubicación activada")
            } else if (!p0.isLocationAvailable && p0.isLocationAvailable != lastStatus){
                showMessage("Ubicación desactivada")
            }

            lastStatus = p0.isLocationAvailable
        }
    }

    private var markerPosition: Marker? = null
    private var mapservice: MapServices? = null
    private var connectivityManager: ConnectivityManager? = null

    private var lastLocation: Location? = null

    private var noUbicationBounds = LatLngBounds.builder()
    private  var mMarkerArray = ArrayList<Marker>()

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

    private var markerImages: HashMap<String, ArrayList<Uri>> = HashMap<String,  ArrayList<Uri>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        setPositionButton()

        mapservice = MapServices(this,mapRoot)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val mapSettings = mMap.uiSettings
        mapSettings.isMapToolbarEnabled = false
        mapSettings.isMyLocationButtonEnabled = false
        mapSettings.isCompassEnabled = false

        Log.i("IMAGES","$markerImages")
        mMap.setOnInfoWindowClickListener(this)
        mMap.setInfoWindowAdapter(InfoWindowAdapter(applicationContext,layoutInflater,markerImages,mMap))

        //locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        googleLocationManager = FusedLocationProviderClient(this)
        googleLocationRequest = LocationRequest()
        googleLocationRequest?.interval = 500
        googleLocationRequest?.maxWaitTime = 1000
        googleLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        googleLocationRequest?.smallestDisplacement = 1f

        requestPermission()

    }

    private fun requestPermission(){
        Log.i("Primero","Request permission")
        val permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permiso == PackageManager.PERMISSION_GRANTED){
            if (!mMap.isMyLocationEnabled) {
                mMap.isMyLocationEnabled = true
                //locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1f, this)
                googleLocationManager!!.requestLocationUpdates(googleLocationRequest,googleLocationCallback, null)
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
    }

    fun showMessage(message: String) {
        val snack = Snackbar.make(mapRoot ,message, Snackbar.LENGTH_LONG)

        snack.show()
    }

    fun setPositionButton() {
        userPosition.setOnClickListener {
            requestPermission()
        }
    }

    /*fun moveToUserPosition(){
        val permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permiso == PackageManager.PERMISSION_GRANTED){
            locationManager?.let { location ->
                if (location.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    val currentLocation = location.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (currentLocation != null){
                        val localizaConUsuario = LatLng(currentLocation.latitude ,currentLocation.longitude)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(localizaConUsuario , 18f))
                    }
                } else {
                    showMessage("Ubicación desactivada")
                }


            }
        } else {
            showMessage("No se ha permitido la ubicación")
        }
    }*/

    fun moveToUserPosition(){
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
                                showMessage("Ubicación desactivada o iniciandose...")
                            }
                        }
                    } else {
                        showMessage("Ubicación desactivada o iniciandose...")
                    }
                }
            }
        } else {
            showMessage("No se ha permitido la ubicación")
        }
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        val size: Int = 120
        vectorDrawable!!.setBounds(0, 0, size, size)
        val bitmap =
            Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun loadPois(){
        AsyncPois().let {
            it.execute(Callable<ArrayList<PuntoInteres?>>{
                mapservice!!.getPOIS()
            })
            it.taskListener = object : OnDownloadsCompleted {
                override fun onPoisDonwloaded(arrayList: ArrayList<PuntoInteres?>?) {
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

                    moveCameraIfNoLocation()
                }
            }
        }
    }

   /* fun moveCameraIfNoLocation(){
        locationManager?.let {
            if (!it.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(noUbicationBounds.build(),0))
            }
        }
    }*/

    fun moveCameraIfNoLocation(){
        googleLocationManager?.let {
            val permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            if (permiso == PackageManager.PERMISSION_GRANTED) {
                it.locationAvailability.addOnSuccessListener { locationAvailability ->
                    if (!locationAvailability.isLocationAvailable){
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(noUbicationBounds.build(), 0))
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
            markerImages.put(marker.id, images)
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
                } else {
                    Log.i("CONECTION","Conexion perdida")
                    showMessage("No hay conexión a internet")
                }
            }
        }
    }

    fun unTrackConectivity(){
        connectionUtils.disable(this)
    }

    override fun onPause() {
        super.onPause()
        unTrackConectivity()
       // unTrackGps()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
       trackConectivity()
        //trackGps()

    }

    fun unTrackGps(){
        val permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permiso == PackageManager.PERMISSION_GRANTED){
            mMap.isMyLocationEnabled = false
        }
        locationManager?.removeUpdates(this)
    }

    fun trackGps(){
      locationManager?.let{
          val permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
          if (permiso == PackageManager.PERMISSION_GRANTED){
                  it.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1f, this)
                  mMap.isMyLocationEnabled = true
              }
        }
    }

    override fun onInfoWindowClick(p0: Marker?) {
        Log.i("Marker","Imagen a mostrar: ${markerImages[p0?.id]?.get(1)}")

        lastMarkerInfo = p0?.tag as PuntoInteres

        val intent = Intent(this,InfoWindowDetail::class.java)
        intent.putExtra("dayString",markerImages[p0.id]?.get(1).toString())
        startActivity(intent)
    }

    override fun onLocationChanged(location: Location?) {
        if (lastLocation == null) {
            moveToUserPosition()
        }
        lastLocation = location
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.i("TAG","Estado cambiado de provider : $provider a estado ${status}")
    }

    override fun onProviderEnabled(provider: String?) {
        showMessage("Ubicación activada")
    }

    override fun onProviderDisabled(provider: String?) {
        showMessage("Ubicación desactivada")
    }
}
