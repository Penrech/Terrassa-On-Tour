package parcaudiovisual.terrassaontour

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_multiple_audiovisual.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import parcaudiovisual.terrassaontour.realm.DBRealmHelper
import kotlin.math.roundToInt

private const val NUM_OF_COLUMN_LANDSCAPE = 3
private const val NUM_OF_COLUMN_PORTRAIT = 2

class MultipleAudiovisualActivity : AppCompatActivity(), AudiovisualsListAdapter.OnMaClickListener {

    private var audiovisualsLayoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager? = null
    private var audiovisualsAdapter: AudiovisualsListAdapter? = null
    private var dbHelper: DBRealmHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiple_audiovisual)

        CoroutineScope(Default).launch{
            dbHelper = DBRealmHelper()
            setCloseFAB()

            val idPoint = intent.getStringExtra("IDPOINT")

            if (idPoint == null) {
                errorLoadingAudiovisuals()
            }

            withContext(Main){
                dbHelper?.updateStaticsAddPointVisit(idPoint)
            }

            val pointAudiovisuals = intent.getParcelableArrayListExtra<AudiovisualParcelable>("AUDIOVISUALES")

            if (pointAudiovisuals == null) {
                errorLoadingAudiovisuals()
            } else {
                initReyclerView(pointAudiovisuals)
            }
        }
    }

    private fun setCloseFAB(){
        closeMultipleAudiovisual.setOnClickListener {
            finish()
        }
    }

    private suspend fun errorLoadingAudiovisuals(){
        val toast = Toast.makeText(this,"No se han podido recuperar los audiovisuales del punto de interés", Toast.LENGTH_LONG)

        withContext(Main){
            toast.show()
            finish()
        }
    }

    private suspend fun initReyclerView(audiovisualList : ArrayList<AudiovisualParcelable>){
        val numOfColumns = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) NUM_OF_COLUMN_LANDSCAPE else NUM_OF_COLUMN_PORTRAIT
        audiovisualsLayoutManager = GridLayoutManager(this, numOfColumns)

        val padding = 8 * resources.displayMetrics.density
        val routeAudiovisuals = intent.getStringArrayExtra("RUTEAUD")
        audiovisualsAdapter = AudiovisualsListAdapter(this,audiovisualList, routeAudiovisuals,this)

        withContext(Main){
            multipleAudiovisualRV.addItemDecoration(MaRecyclerViewItemDecoration(padding.roundToInt(),audiovisualList.size,numOfColumns))
            multipleAudiovisualRV.layoutManager = audiovisualsLayoutManager
            multipleAudiovisualRV.adapter = audiovisualsAdapter
        }

    }

    override fun onMaClickListener(audiovisual: AudiovisualParcelable) {
        val intent = Intent(this,AudiovisualDetailActivity::class.java)
        intent.putExtra("AUDIOVISUAL",audiovisual)
        startActivity(intent)
    }
}
