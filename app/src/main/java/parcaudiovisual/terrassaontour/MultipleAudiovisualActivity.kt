package parcaudiovisual.terrassaontour

import android.content.Intent
import android.content.res.Configuration
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_multiple_audiovisual.*
import parcaudiovisual.terrassaontour.realm.DBRealmHelper

class MultipleAudiovisualActivity : AppCompatActivity(), AudiovisualsListAdapter.OnMaClickListener {

    private var audiovisualsLayoutManager: RecyclerView.LayoutManager? = null
    private var audiovisualsAdapter: AudiovisualsListAdapter? = null
    private var dbHelper: DBRealmHelper? = null

    private val NUM_OF_COLUMN_LANDSCAPE = 3
    private val NUM_OF_COLUMN_PORTRAIT = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiple_audiovisual)

        dbHelper = DBRealmHelper()
        setCloseFAB()

        val idPoint = intent.getStringExtra("IDPOINT")

        if (idPoint == null) {
            errorLoadingAudiovisuals()
            return
        }

        val pointAudiovisuals = loadAudiovisuals(idPoint)
        if (pointAudiovisuals == null) {
            errorLoadingAudiovisuals()
        } else {
            initReyclerView(pointAudiovisuals)
        }
    }

    fun setCloseFAB(){
        closeMultipleAudiovisual.setOnClickListener {
            finish()
        }
    }

    fun loadAudiovisuals(idPoint: String): List<Audiovisual>? {
        val audiovisuals = dbHelper?.getAudiovisualsFromPoint(idPoint)
        return audiovisuals
    }

    fun errorLoadingAudiovisuals(){
        val toast = Toast.makeText(this,"No se han podido recuperar los audiovisuales del punto de interés", Toast.LENGTH_LONG)
        toast.show()
        finish()
    }

    fun initReyclerView(audiovisualList : List<Audiovisual>){
        val numOfColumns = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) NUM_OF_COLUMN_LANDSCAPE else NUM_OF_COLUMN_PORTRAIT
        audiovisualsLayoutManager = GridLayoutManager(this, numOfColumns)
        multipleAudiovisualRV.layoutManager = audiovisualsLayoutManager

        audiovisualsAdapter = AudiovisualsListAdapter(this,audiovisualList, this)
        multipleAudiovisualRV.adapter = audiovisualsAdapter
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onMaClickListener(idAudiovisual: String) {
        Log.i("AudiovisualClick","Audiovisual clicked: $idAudiovisual")
    }
}
