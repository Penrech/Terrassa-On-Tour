package parcaudiovisual.terrassaontour

import android.content.Context
import android.net.Network
import android.content.Context.CONNECTIVITY_SERVICE
import android.support.v4.content.ContextCompat.getSystemService
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.ConnectivityManager.NetworkCallback
import android.util.Log
import java.lang.IllegalArgumentException


class ConnectionStateMonitor : NetworkCallback() {

    internal val networkRequest: NetworkRequest
    var conectionListener : OnConnectionChange? = null

    init {
        networkRequest = NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build()
    }

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

    override fun onAvailable(network: Network) {
        // Do what you need to do here
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