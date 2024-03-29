package parcaudiovisual.terrassaontour

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageButton
import kotlinx.android.synthetic.main.activity_info_window_detail.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import parcaudiovisual.terrassaontour.adapters.InfoWindowImageViewPager
import parcaudiovisual.terrassaontour.fragments.BigImageInfoWindow
import java.util.*

class InfoWindowDetail : AppCompatActivity(), ViewPager.OnPageChangeListener {

    private lateinit var mPager: ViewPager
    private var pagerAdapter: InfoWindowImageViewPager? = null

    private val dayDrawable = R.drawable.ic_icono_dia
    private val nightDrawable = R.drawable.ic_icono_noche

    private var dialog: AlertDialog? = null

    private var fragmentsList: MutableList<BigImageInfoWindow> = mutableListOf()

    enum class Icon {
        DAY, NIGHT
    }

    private var changeToIcon: Icon? = null
    set(value) {
        typeIcon = when(value) {
            Icon.DAY -> {
                runOnUiThread {switchInfoPhoto.setImageDrawable(getDrawable(nightDrawable))}
                Icon.DAY
            } else -> {
                runOnUiThread {switchInfoPhoto.setImageDrawable(getDrawable(dayDrawable)) }
                Icon.NIGHT
            }
        }
        field = value
    }

    private var typeIcon: Icon? = null

    private var pagerIsListening = false

    override fun onPageScrollStateChanged(p0: Int) {}

    override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

    override fun onPageSelected(p0: Int) {

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

        mPager = detailImagePager

        listeningPagerEvents()
        setCloseFabButton()
        setSwitchFabButton()
        setInfoInteriorFabButton()

        CoroutineScope(Dispatchers.Default).launch {
            loadData()
        }
    }

    private suspend fun loadData(){
        fragmentsList.clear()

        val intent = intent
        val imagesData = intent.getParcelableExtra<DetailInfoImages>("imagesToDetail")

        if (imagesData != null) {
            var imagePrincipal = imagesData.imgPrincipal
            var imageSecundary = imagesData.imgSecundary

            if (imagesData.day == 0) {
                imagePrincipal = imagesData.imgSecundary
                imageSecundary = imagesData.imgPrincipal
            }

            fragmentsList.add(BigImageInfoWindow.newInstance(imagePrincipal))
            fragmentsList.add(BigImageInfoWindow.newInstance(imageSecundary))

            withContext(Dispatchers.Main){
                if (typeIcon == null) changeToIcon =  if (imagesData.day == 1) Icon.DAY else Icon.NIGHT

                if (imagesData.interior == 1) InfoInteriorPoiFab.show()

                pagerAdapter = InfoWindowImageViewPager(supportFragmentManager,fragmentsList)
                mPager.adapter = pagerAdapter
            }
        }
    }

    private fun showDialog(){
        dialog?.let {
            it.show()
            return
        }

        val factory = LayoutInflater.from(this)
        val view = factory.inflate(R.layout.custom_dialog,null)
        dialog = AlertDialog.Builder(this).create()
        dialog!!.setView(view)

        val dialogButton = view.findViewById<ImageButton>(R.id.CloseDialog)
        dialogButton.setOnClickListener {
            dialog!!.dismiss()
        }

        dialog!!.show()

    }

    private fun setInfoInteriorFabButton(){
        InfoInteriorPoiFab.setOnClickListener {
            showDialog()
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

    override fun onBackPressed() {
        if (mPager.currentItem != 0) {
            mPager.currentItem = 0
        } else {
            super.onBackPressed()
        }
    }

}
