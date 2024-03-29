package parcaudiovisual.terrassaontour

import android.content.Context
import android.net.Network
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.ConnectivityManager.NetworkCallback
import android.util.Log
import java.lang.IllegalArgumentException

class ConnectionStateMonitor : NetworkCallback() {

    internal val networkRequest: NetworkRequest = NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build()
    var conectionListener : OnConnectionChange? = null

    fun enable(context: Context) {
        Log.i("NETWORKSTATE","Enabled")

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(networkRequest, this)
    }

    fun disable(context: Context){
        Log.i("NETWORKSTATE","Disabled")

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            connectivityManager.unregisterNetworkCallback(this)
        } catch (e: IllegalArgumentException){
            e.printStackTrace()
        }
    }

    fun checkConection(context: Context): Boolean{
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    override fun onAvailable(network: Network) {
        Log.i("NETWORKSTATE","Availabled")

        conectionListener?.conectionEnabled(true)
    }

    override fun onLost(network: Network?) {
        Log.i("NETWORKSTATE","Lost")

        conectionListener?.conectionEnabled(false)
    }
}
interface OnConnectionChange {
    fun conectionEnabled(enabled: Boolean)
}