package parcaudiovisual.terrassaontour

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.util.Log
import kotlinx.android.synthetic.main.activity_audiovisual_detail.*
import parcaudiovisual.terrassaontour.adapters.InfoWindowImageViewPager
import parcaudiovisual.terrassaontour.fragments.AudiovisualInfoDetails
import parcaudiovisual.terrassaontour.fragments.StaticAudiovisualResource
import parcaudiovisual.terrassaontour.realm.DBRealmHelper
import java.util.*

class AudiovisualDetailActivity : AppCompatActivity(), ViewPager.OnPageChangeListener {

    private lateinit var mPager: ViewPager
    private var pagerAdapter: InfoWindowImageViewPager? = null

    private var fragmentStaticImage: StaticAudiovisualResource? = null
    private var fragmentInfoAudiovisual: AudiovisualInfoDetails? = null

    private var fragmentsList: MutableList<Fragment> = mutableListOf()

    private var pagerIsListening = false

    private lateinit var id: String
    private lateinit var src: String
    private lateinit var title: String



    override fun onPageScrollStateChanged(p0: Int) {}

    override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

    override fun onPageSelected(p0: Int) {
        //Pagina cambiada
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audiovisual_detail)

        mPager = AudiovisualInfoPager

        setCloseOpenInfo()
        setCloseAudiovisualDetails()

    }

    private fun loadData(){
        val dbHelper = DBRealmHelper()

        id = intent.getStringExtra("ID")
        val src = intent.getStringExtra("SRC")
        val title = intent.getStringExtra("TITLE")
        val year = intent.getStringExtra("YEAR")
        val description = intent.getStringExtra("DESC")
        val actor = intent.getParcelableArrayExtra("ACTOR")
        val director = intent.getParcelableArrayExtra("DIRECTOR")
        val productor = intent.getParcelableArrayExtra("PRODUCTOR")
        val client = intent.getParcelableArrayExtra("CLIENT")

    }

    private fun setCloseOpenInfo(){
        ChangeToInfoFAB.setOnClickListener {
            if (mPager.currentItem == 0) {
                //mover a pager 1
            } else {
                //Mover a pager 0
            }
        }
    }

    private fun setCloseAudiovisualDetails(){
        BackToCameraFAB.setOnClickListener {
            finish()
        }
    }

    private fun listeningPagerEvents(){
        if (!pagerIsListening) {
            mPager.addOnPageChangeListener(this)
            pagerIsListening = true
        }
    }

    private fun deleteListeningPagerEvents(){
        if (pagerIsListening) {
            mPager.removeOnPageChangeListener(this)
            pagerIsListening = false
        }
    }

    override fun onPause() {
        super.onPause()
        deleteListeningPagerEvents()
    }

    override fun onResume() {
        super.onResume()
        listeningPagerEvents()
    }

}
