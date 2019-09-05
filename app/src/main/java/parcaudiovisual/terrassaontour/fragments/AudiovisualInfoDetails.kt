package parcaudiovisual.terrassaontour.fragments


import android.content.res.Resources
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_audiovisual_info_details.view.*
import parcaudiovisual.terrassaontour.AudiovisualParcelable
import parcaudiovisual.terrassaontour.ClienteProductoraParcelable

import parcaudiovisual.terrassaontour.R
import parcaudiovisual.terrassaontour.adapters.ElementsLikeActorsRecyclerAdapter
import parcaudiovisual.terrassaontour.layoutManagers.NoScrollLinearLayoutManager
import java.lang.reflect.Type
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val AUDIOVISUAL = "audiovisual"

class AudiovisualInfoDetails : Fragment() {
    private var rootView: View? = null

    private var linearLayoutManager: LinearLayoutManager? = null
    private var adapter: ElementsLikeActorsRecyclerAdapter? = null

    private var imageToolbarHeight: Float? = null
    private var textToolbarHeight: Float? = null
    private var midHeightOfFAB: Float? = null

    private var onOffsetChangeToolbarListener =

        AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
           /* val screenHeight = Resources.getSystem().displayMetrics.heightPixels
            val screenWidth = Resources.getSystem().displayMetrics.widthPixels
            val textToolbar = rootView!!.AudiovisualTitleToolbar
            val titleTextView = if (textToolbar.getChildAt(0) is AppCompatTextView) textToolbar.getChildAt(0) as AppCompatTextView else null
            val yearTextView = if (textToolbar.getChildAt(0) is AppCompatTextView) textToolbar.getChildAt(1) as AppCompatTextView else null
            Log.i("TextParams","Param1: ${titleTextView?.width}, Param2: ${titleTextView?.translationX}")
            val marginMax = (screenWidth * 0.15).roundToInt()
            val maximum = screenHeight * 0.4
            val maxCenter = ((textToolbar.width ) / 2)
            Log.i("Offset","Textoolbar Inset: ${textToolbar.contentInsetLeft}")
            val offset = Math.abs(verticalOffset)
            val percentage = (offset * 100) / maximum
            val marginNormalized = ((percentage * marginMax) / 100).roundToInt()
            val centerNormalized = (percentage * maxCenter / 100).toFloat()
            if (offset >= 0 && offset <= maximum) {
                textToolbar.setPadding(marginNormalized ,textToolbar.paddingTop,marginNormalized,textToolbar.paddingBottom)
                titleTextView?.translationX = centerNormalized - 2 * marginNormalized
                yearTextView?.translationX = centerNormalized - 2 * marginNormalized

                if (offset == maximum.toInt()) {

                }
            }*/
           /*if (Math.abs(verticalOffset) >= (screenHeight * 0.4)) {
                  Log.i("Offset","TOP")
                  textToolbar.setPadding(margin,textToolbar.paddingTop,margin,textToolbar.paddingBottom)
           } else {

           }*/
            Log.i("OFFSET","Offset pixeles: $verticalOffset")
            val topLimit = imageToolbarHeight!! - textToolbarHeight!! + midHeightOfFAB!!
            val bottomLimit = imageToolbarHeight!!
            val absOffset = abs(verticalOffset)
            val minPercentage = 1 - (textToolbarHeight!! / imageToolbarHeight!!)
            Log.i("OFFSET","ABS OFFSET: $absOffset")
            Log.i("OFFSET","TOPLIMIT: $topLimit")
            Log.i("OFFSET","min percentage: $minPercentage")

            val percentage = (absOffset / bottomLimit)

            if (percentage > minPercentage) {
                    val multiplier = 1 / (1 - minPercentage)
                Log.i("OFFSET","Elevation multiplier: $multiplier")
            }



        }

    private var appbarListenerOffsetOn = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_audiovisual_info_details, container, false)

        imageToolbarHeight = context?.resources?.getDimension(R.dimen.infoToolbarImageHeight)
        textToolbarHeight = context?.resources?.getDimension(R.dimen.infoToolbarHeight)
        midHeightOfFAB = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,20f,context?.resources?.displayMetrics)

        Log.i("VALUES","IMAGE TOOLBAR HEIGHT: $imageToolbarHeight")
        Log.i("VALUES","text TOOLBAR HEIGHT: $textToolbarHeight")
        Log.i("VALUES","midheigth: $midHeightOfFAB")

        loadData()

        return rootView
    }

    private fun loadData(){
        val audiovisual = arguments?.getParcelable<AudiovisualParcelable>(AUDIOVISUAL)
        //rootView!!.AudiovisualImageView.layoutParams.height = (screenSize * 0.4).roundToInt()

        Picasso.get()
            .load(audiovisual?.img_cabecera)
            .noFade()
            .placeholder(R.drawable.placeholder_loading_big)
            .into(rootView!!.AudiovisualImageView)

        rootView!!.audInfoTitle.text = audiovisual?.title
        rootView!!.audInfoYear.text = audiovisual?.year

        val screenSize = Resources.getSystem().displayMetrics.heightPixels

        val actoresList = audiovisual?.actores ?: ArrayList()
        val directoresList = audiovisual?.directores ?: ArrayList()
        val productorasArray: ArrayList<ClienteProductoraParcelable> = audiovisual?.productoras ?: ArrayList()
        val clienteList: ArrayList<ClienteProductoraParcelable> = audiovisual?.clientes ?: ArrayList()

        val elements = ArrayList<Pair<String, List<Any>>>()
        if (actoresList.isNotEmpty()) elements.add(Pair("Actores",actoresList))
        if (directoresList.isNotEmpty()) elements.add(Pair("Directores",directoresList))
        if (productorasArray.isNotEmpty()) elements.add(Pair("Productoras",productorasArray))
        if (clienteList.isNotEmpty()) elements.add(Pair("Clientes",clienteList))
        if (!audiovisual?.description.isNullOrEmpty()) elements.add(Pair("Descripción", listOf(audiovisual!!.description!!)))

        linearLayoutManager = NoScrollLinearLayoutManager(context!!)
        adapter = ElementsLikeActorsRecyclerAdapter(context!!,elements)
        /*rootView!!.ElementsLikeActorsRV.layoutManager = linearLayoutManager
        rootView!!.ElementsLikeActorsRV.adapter =  adapter*/

    }

    override fun onResume() {
        super.onResume()
        setOffsetListener()
    }

    override fun onPause() {
        super.onPause()
        deleteOffsetListener()
    }

    fun setOffsetListener(){
        if (!appbarListenerOffsetOn) {
            rootView!!.appbar.addOnOffsetChangedListener(onOffsetChangeToolbarListener)
            appbarListenerOffsetOn = true
        }
    }

    fun deleteOffsetListener(){
        if (appbarListenerOffsetOn) {
            rootView!!.appbar.removeOnOffsetChangedListener(onOffsetChangeToolbarListener)
            appbarListenerOffsetOn = false
        }
    }

    companion object {
        fun newInstance(audiovisualInfo: AudiovisualParcelable?): AudiovisualInfoDetails {
            val fragment = AudiovisualInfoDetails()
            val args = Bundle()

            args.putParcelable(AUDIOVISUAL,audiovisualInfo)
            fragment.arguments = args
            return fragment
        }
    }
}
