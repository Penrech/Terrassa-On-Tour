package parcaudiovisual.terrassaontour

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.util.Log
import parcaudiovisual.terrassaontour.realm.DBRealmHelper

class LoadingActivity : AppCompatActivity(){

    private var conectionUtils = ConnectionStateMonitor()

    private val dbChangeLocalBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action){
                DBRealmHelper.BROADCAST_FIRST_DATA_LOAD -> startMainActivity()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
    }

    private fun loadDatabase(){
        if (!conectionUtils.checkConection(this)) startMainActivity()
        else {
            LocalBroadcastManager.getInstance(this).registerReceiver(dbChangeLocalBroadcastReceiver, IntentFilter(DBRealmHelper.BROADCAST_FIRST_DATA_LOAD))
        }
    }

    private fun unLoadListener(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dbChangeLocalBroadcastReceiver)
    }

    fun startMainActivity(){
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(0, 0)
    }

    override fun onResume() {
        super.onResume()
        loadDatabase()
    }

    override fun onPause() {
        super.onPause()
        unLoadListener()
    }
}
