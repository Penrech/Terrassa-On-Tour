package parcaudiovisual.terrassaontour


import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_info_window_detail.*
import parcaudiovisual.terrassaontour.adapters.InfoWindowImageViewPager
import parcaudiovisual.terrassaontour.fragments.BigImageInfoWindow
import java.util.*

class InfoWindowDetail : AppCompatActivity(), ViewPager.OnPageChangeListener {

    private lateinit var mPager: ViewPager
    private var pagerAdapter: InfoWindowImageViewPager? = null

    private val DayDrawable = R.drawable.ic_icono_dia
    private val NightDrawable = R.drawable.ic_icono_noche

    private var fragmentsList: MutableList<BigImageInfoWindow> = mutableListOf()

    enum class Icon {
        DAY, NIGHT
    }

    private var changeToIcon: Icon? = null
    set(value) {
        typeIcon = when(value) {
            Icon.DAY -> {
                runOnUiThread {switchInfoPhoto.setImageDrawable(getDrawable(NightDrawable))}
                Icon.DAY
            } else -> {
                runOnUiThread {switchInfoPhoto.setImageDrawable(getDrawable(DayDrawable)) }
                Icon.NIGHT
            }
        }
    }

    private var typeIcon: Icon? = null

    private var pagerIsListening = false

    override fun onPageScrollStateChanged(p0: Int) {}

    override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

    override fun onPageSelected(p0: Int) {
        Log.i("Pagina","Pagina cambiada a $p0")
        if (typeIcon != null) {
            changeToIcon = when (typeIcon) {
                Icon.DAY -> Icon.NIGHT
                else -> Icon.DAY
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_window_detail)

        Log.i("Time","Time init onCreate: ${Date()}")
        mPager = detailImagePager

        listeningPagerEvents()
        setCloseFabButton()
        setSwitchFabButton()

        loadData()
    }


    private fun loadData(){
        Log.i("Time","Time init loadData: ${Date()}")
        fragmentsList.clear()

        val intent = intent
        val imagesData = intent.getParcelableExtra<DetailInfoImages>("imagesToDetail")

        if (imagesData != null) {
            fragmentsList.add(BigImageInfoWindow.newInstance(imagesData.imgPrincipal))
            fragmentsList.add(BigImageInfoWindow.newInstance(imagesData.imgSecundary))
            if (typeIcon == null) changeToIcon =  if (imagesData.day == 1) Icon.DAY else Icon.NIGHT
        }

        Log.i("Time","Time end loadData: ${Date()}")

        runOnUiThread {
            Log.i("Time","Time init runOnUiThread loadData: ${Date()}")
            pagerAdapter = InfoWindowImageViewPager(supportFragmentManager,fragmentsList)
            mPager.adapter = pagerAdapter

            Log.i("Time","Time end runOnUiThread loadData: ${Date()}")
        }



    }

    private fun setCloseFabButton(){
        closeDetailInfoWindowBtn.setOnClickListener {
            finish()
        }
    }

    private fun setSwitchFabButton(){
        switchInfoPhoto.setOnClickListener {
            when (mPager.currentItem) {
                0 -> mPager.currentItem = 1
                1 -> mPager.currentItem = 0
            }
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
        Log.i("Time","Time init Resume: ${Date()}")
        listeningPagerEvents()
    }

}
