package parcaudiovisual.terrassaontour

import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_info_window_detail.*
import kotlinx.android.synthetic.main.activity_maps.*

class InfoWindowDetail : AppCompatActivity() {

    private var dayImageUri: Uri? = null

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
        setContentView(R.layout.activity_info_window_detail)

        setCloseFabButton()
        loadData()
    }

    fun loadData(){
        val intent = intent
        val dayUriString = intent.getStringExtra("dayString")

        Log.i("Marker","Marker en detail: $title")
        if (dayUriString.isNullOrEmpty()) showMessage("Error cargando datos")

        if (!dayUriString.isNullOrEmpty()) {
            dayImageUri = Uri.parse(dayUriString)

           Picasso.get()
                .load(dayImageUri)
                .noFade()
                .placeholder(R.drawable.placeholder_loading_big)
                .into(infoWindowImageDetail)
        }

    }

    fun setCloseFabButton(){
        closeDetailInfoWindowBtn.setOnClickListener {
            finish()
        }
    }

    fun showMessage(message: String) {
        val snack = Snackbar.make(mapRoot ,message, Snackbar.LENGTH_LONG)

        snack.show()
    }

    fun trackConectivity() {
        connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (!isNetworkAvailable) showMessage("No hay conexión a internet")

        connectionUtils.enable(this)

        connectionUtils.conectionListener = object : OnConnectionChange {
            override fun conectionEnabled(enabled: Boolean) {
                if (enabled){
                    Log.i("CONECTION","Conexion restablecida")
                } else {
                    Log.i("CONECTION","Conexion perdida")
                    showMessage("No hay conexión a internet")
                }
            }
        }
    }

    private fun hideSystemUI() {

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
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
        hideSystemUI()
        trackConectivity()
    }

}
