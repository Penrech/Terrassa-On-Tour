package parcaudiovisual.terrassaontour

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_audiovisual_detail.*
import kotlinx.android.synthetic.main.activity_info_window_detail.*
import parcaudiovisual.terrassaontour.adapters.InfoElementsLinkRecyclerAdapter
import parcaudiovisual.terrassaontour.adapters.InfoWindowImageViewPager
import parcaudiovisual.terrassaontour.fragments.AudiovisualInfoDetails
import parcaudiovisual.terrassaontour.fragments.InfoElementFragment
import parcaudiovisual.terrassaontour.fragments.StaticAudiovisualResource
import parcaudiovisual.terrassaontour.interfaces.ChangeDetailCloseButton
import parcaudiovisual.terrassaontour.interfaces.SendLinkToOpenInBrowser
import parcaudiovisual.terrassaontour.realm.DBRealmHelper
import java.net.URL
import java.util.*
import kotlin.math.max
import kotlin.math.min

class AudiovisualDetailActivity : AppCompatActivity(),
    ViewPager.OnPageChangeListener,
    InfoElementsLinkRecyclerAdapter.OnClickLink,
    ChangeDetailCloseButton,
    SendLinkToOpenInBrowser{

    private lateinit var mPager: ViewPager
    private var pagerAdapter: InfoWindowImageViewPager? = null

    private var fragmentStaticImage: StaticAudiovisualResource? = null
    private var fragmentInfoAudiovisual: AudiovisualInfoDetails? = null

    private var InfoDrawable : Drawable? = null
    private var CloseDrawable : Drawable? = null

    private var fragmentsList: MutableList<Fragment> = mutableListOf()

    private var pagerIsListening = false

    private var audiovisual: AudiovisualParcelable? = null

    override fun onPageScrollStateChanged(p0: Int) {}

    override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

    enum class Icon {
        MULTIMEDIA, INFO
    }
    private var changeToIcon: Icon? = null
        set(value) {
            typeIcon = when(value) {
                Icon.MULTIMEDIA -> {
                    runOnUiThread {ChangeToInfoFAB.setImageDrawable(InfoDrawable)}
                    runOnUiThread{BackToCameraFAB.show()}
                    if (isCloseButtonHiddenOnInfoWindow) {
                        runOnUiThread { ChangeToInfoFAB.show() }
                    }
                    //runOnUiThread { ChangeToInfoFAB.show() }
                    Icon.MULTIMEDIA
                } else -> {
                    runOnUiThread {ChangeToInfoFAB.setImageDrawable(CloseDrawable) }
                    runOnUiThread{BackToCameraFAB.hide()}
                    if (isCloseButtonHiddenOnInfoWindow) {
                        runOnUiThread { ChangeToInfoFAB.hide() }
                    }
                    //runOnUiThread { ChangeToInfoFAB.hide() }
                    Icon.INFO
                }
            }
            field = value
        }

    private var typeIcon: Icon? = null

    private var isCloseButtonHiddenOnInfoWindow = false

    override fun onPageSelected(p0: Int) {
        if (typeIcon != null) {
            changeToIcon = when (typeIcon) {
                Icon.MULTIMEDIA -> Icon.INFO
                else -> Icon.MULTIMEDIA
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("PageNumber",mPager.currentItem)
        outState.putParcelable("audiovisual",audiovisual)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audiovisual_detail)

        mPager = AudiovisualInfoPager

        Log.i("Pruebas","Entro en onCreate")

        val pageNumber = savedInstanceState?.getInt("PageNumber")
        val bundleAudiovisual: AudiovisualParcelable? = savedInstanceState?.getParcelable("audiovisual")

        InfoDrawable = getDrawable(R.drawable.ic_icono_info)?.mutate()
        CloseDrawable = getDrawable(R.drawable.ic_icono_cerrar)?.mutate()

        setCloseOpenInfo()
        setCloseAudiovisualDetails()

        managePage(pageNumber)

        loadData(bundleAudiovisual)

    }

    private fun managePage(previousPageNumber: Int?) {
        if (previousPageNumber == null) return

        if (previousPageNumber == 0) changeToIcon = Icon.MULTIMEDIA
        else changeToIcon = Icon.INFO
    }

    private fun loadData(previousData: AudiovisualParcelable?){

        if (previousData == null) {

            val dbHelper = DBRealmHelper()

            audiovisual = intent.getParcelableExtra("AUDIOVISUAL")

            changeToIcon = Icon.MULTIMEDIA

            if (audiovisual == null) {
                errorLoadingAudiovisual()
            }

            dbHelper.updateStaticsAddAudiovisualVisit(audiovisual!!.id!!)

        } else {
            audiovisual = previousData
        }

        fragmentStaticImage = StaticAudiovisualResource.newInstance(audiovisual?.src)
        fragmentInfoAudiovisual = AudiovisualInfoDetails.newInstance(audiovisual)

        fragmentsList.add(fragmentStaticImage!!)
        fragmentsList.add(fragmentInfoAudiovisual!!)

        pagerAdapter = InfoWindowImageViewPager(supportFragmentManager,fragmentsList)
        mPager.adapter = pagerAdapter

    }

    private fun setCloseOpenInfo(){
        ChangeToInfoFAB.setOnClickListener {
            if (mPager.childCount < 2) return@setOnClickListener

            if (mPager.currentItem == 0) {
                mPager.currentItem = 1
            } else {
                mPager.currentItem = 0
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

    override fun onBackPressed() {
        if (mPager.currentItem != 0) {
            mPager.currentItem = 0
        } else {
            super.onBackPressed()
        }

    }

    override fun onClickLink(urlString: String) {
        Log.i("Clicked","URl Clicked $urlString")
    }

    fun errorLoadingAudiovisual(){
        val toast = Toast.makeText(this,"No se han podido recuperar la información del audiovisual", Toast.LENGTH_LONG)
        toast.show()
        finish()
    }

    override fun hideChangeToMultimediaButton(hide: Boolean) {
        if (hide) {
            if (!isCloseButtonHiddenOnInfoWindow) {
                ChangeToInfoFAB.hide()
            }
        } else {
            if (isCloseButtonHiddenOnInfoWindow) {
                ChangeToInfoFAB.show()
            }
        }

        isCloseButtonHiddenOnInfoWindow = hide
    }

    override fun backToInfo() {
        mPager.currentItem = 0
    }

    override fun openInBrowser(link: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(Uri.parse(link))
        startActivity(intent)
    }
}
