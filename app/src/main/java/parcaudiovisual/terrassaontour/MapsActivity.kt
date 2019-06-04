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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Network
import com.google.android.gms.maps.model.BitmapDescriptor
import java.lang.IllegalArgumentException
import java.util.concurrent.Callable


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val LOCATION_REQUEST_CODE = 101
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private var markerPosition: Marker? = null
    private var mapservice: MapServices? = null
    private var connectivityManager: ConnectivityManager? = null

    private var connectionUtils = ConnectionStateMonitor()

    private val isNetworkAvailable: Boolean
        get() {
            connectivityManager?.let {
                val activeNetworkInfo = it.activeNetworkInfo
                return activeNetworkInfo != null && activeNetworkInfo.isConnected
            }
            return false
        }

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

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = object :LocationListener{
            override fun onLocationChanged(location: Location?) {
                if (location != null){
                    Log.i("TAG","Location changed")
                    val localizaConUsuario = LatLng(location.latitude,location.longitude)

                    if (markerPosition != null){
                        markerPosition!!.position = localizaConUsuario
                    } else {
                        markerPosition = mMap.addMarker(MarkerOptions().position(localizaConUsuario)
                            .title("Tu posición")
                            .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.ic_pointer_usuario)))
                    }

                }
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

            }

            override fun onProviderEnabled(provider: String?) {
                showMessage("Localización activada")
            }

            override fun onProviderDisabled(provider: String?) {
                showMessage("Localización desactivada")
            }
        }

        requestPermission()

        // Add a marker in Sydney and move the camera
        /*val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/
    }

    private fun requestPermission(){
        val permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permiso == PackageManager.PERMISSION_GRANTED){
            if (!mMap.isMyLocationEnabled) {
                mMap.isMyLocationEnabled = true
                locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1f, locationListener)
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
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

    fun moveToUserPosition(){
        val permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permiso == PackageManager.PERMISSION_GRANTED){
            locationManager?.let { location ->
                val currentLocation = location.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val localizaConUsuario = LatLng(currentLocation.latitude ,currentLocation.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(localizaConUsuario , 10f))

                if (markerPosition != null){
                    markerPosition!!.position = localizaConUsuario
                } else {
                    markerPosition = mMap.addMarker(
                        MarkerOptions().position(localizaConUsuario)
                            .title("Tu posición")
                            .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.ic_pointer_usuario))
                    )
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
                    arrayList?.forEach { puntoInteres ->
                        if (puntoInteres != null){
                            val posicionPunto = LatLng(puntoInteres.latitud, puntoInteres.longitud)

                            addCustomPoiMarker(puntoInteres,posicionPunto)
                        }
                    }
                }
            }
        }
    }

    fun addCustomPoiMarker(puntoInteres: PuntoInteres, position: LatLng){
        if (puntoInteres.exterior == true) {
            mMap.addMarker(
                MarkerOptions().position(position)
                    .title(puntoInteres.title)
                    .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.ic_pointer_exterior))
            )
        } else {
            mMap.addMarker(
                MarkerOptions().position(position)
                    .title(puntoInteres.title)
                    .icon(bitmapDescriptorFromVector(applicationContext, R.drawable.ic_pointer_interior))
            )
        }
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
    }

    override fun onResume() {
        super.onResume()
        trackConectivity()
    }


}
