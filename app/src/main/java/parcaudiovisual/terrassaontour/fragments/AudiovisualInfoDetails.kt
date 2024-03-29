package parcaudiovisual.terrassaontour.fragments

import android.content.Context
import android.os.Bundle
import com.google.android.material.appbar.AppBarLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_audiovisual_info_details.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import parcaudiovisual.terrassaontour.AudiovisualParcelable
import parcaudiovisual.terrassaontour.ClienteProductoraParcelable

import parcaudiovisual.terrassaontour.R
import parcaudiovisual.terrassaontour.SectionModel
import parcaudiovisual.terrassaontour.adapters.SectionRecyclerViewAdapter
import parcaudiovisual.terrassaontour.interfaces.ChangeDetailCloseButton
import kotlin.math.abs

private const val AUDIOVISUAL = "audiovisual"

class AudiovisualInfoDetails : Fragment() {
    private var sendInfoToParentInterface: ChangeDetailCloseButton? = null

    private var rootView: View? = null
    private var crewRecyclerView: RecyclerView? = null

    private var popupAnimation: Animation? = null
    private var popOutAnimation: Animation? = null
    private var outAnimationOn = false

    private var imageToolbarHeight: Float? = null
    private var textToolbarHeight: Float? = null
    private var midHeightOfFAB: Float? = null

    private var onOffsetChangeToolbarListener =

        AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->

            val bottomLimit = imageToolbarHeight!!
            val absOffset = abs(verticalOffset)
            val minPercentage = 1 - (textToolbarHeight!! / imageToolbarHeight!!)

            val percentage = (absOffset / bottomLimit)

            hideFabShowToolbarButton(percentage > minPercentage)
        }

    private var popOutAnimationListener = object : Animation.AnimationListener{
        override fun onAnimationRepeat(animation: Animation?) {}

        override fun onAnimationEnd(animation: Animation?) {
            if (animation == popOutAnimation) {
                rootView!!.closeButtonFromToolbar.visibility = View.INVISIBLE
                outAnimationOn = false
                sendInfoToParentInterface?.hideChangeToMultimediaButton(false)
            }
        }

        override fun onAnimationStart(animation: Animation?) {
            if (animation == popOutAnimation) {
                outAnimationOn = true
            }
        }
    }

    private var onCloseClickListerner = View.OnClickListener {
        sendInfoToParentInterface?.backToInfo()
    }

    private var appbarListenerOffsetOn = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_audiovisual_info_details, container, false)

        setUpRecyclerView()

        imageToolbarHeight = context?.resources?.getDimension(R.dimen.infoToolbarImageHeight)
        textToolbarHeight = context?.resources?.getDimension(R.dimen.infoToolbarHeight)
        midHeightOfFAB = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,20f,context?.resources?.displayMetrics)
        rootView?.closeButtonFromToolbar?.visibility = View.INVISIBLE
        rootView?.closeButtonFromToolbar?.setOnClickListener(onCloseClickListerner)

        CoroutineScope(Default).launch {
            loadData()
        }

        setUpAnimations()

        return rootView
    }

    private suspend fun loadData(){
        val audiovisual = arguments?.getParcelable<AudiovisualParcelable>(AUDIOVISUAL)

        val actoresList = audiovisual?.actores ?: ArrayList()
        val directoresList = audiovisual?.directores ?: ArrayList()
        val productorasList: ArrayList<ClienteProductoraParcelable> = audiovisual?.productoras ?: ArrayList()
        val clienteList: ArrayList<ClienteProductoraParcelable> = audiovisual?.clientes ?: ArrayList()

        val sectionModelArrayList = ArrayList<SectionModel>()

        if (actoresList.isNotEmpty()) {
            val sectionTitle = if (actoresList.size > 1) "Actores/Actrices" else "Actor/Actriz"
            val pairArray = ArrayList<Pair<String,String?>>()
            actoresList.forEach {
                pairArray.add(Pair(it,null))
            }
            sectionModelArrayList.add(SectionModel(sectionTitle,pairArray))
        }

        if (directoresList.isNotEmpty()) {
            val sectionTitle = if (directoresList.size > 1) "Directores/as" else "Director/a"
            val pairArray = ArrayList<Pair<String,String?>>()
            directoresList.forEach {
                pairArray.add(Pair(it,null))
            }
            sectionModelArrayList.add(SectionModel(sectionTitle,pairArray))
        }

        if (productorasList.isNotEmpty()) {
            val sectionTitle = if (productorasList.size > 1) "Productoras" else "Productora"
            val pairArray = ArrayList<Pair<String,String?>>()
            productorasList.forEach {
                pairArray.add(Pair(it.nombre,it.link))
            }
            sectionModelArrayList.add(SectionModel(sectionTitle,pairArray))
        }

        if (clienteList.isNotEmpty()) {
            val sectionTitle = if (clienteList.size > 1) "Clientes" else "Cliente"
            val pairArray = ArrayList<Pair<String,String?>>()
            clienteList.forEach {
                pairArray.add(Pair(it.nombre,it.link))
            }
            sectionModelArrayList.add(SectionModel(sectionTitle,pairArray))
        }

        withContext(Main){
            Picasso.get()
                .load(audiovisual?.img_cabecera)
                .noFade()
                .placeholder(R.drawable.placeholder_loading_big)
                .into(rootView!!.AudiovisualImageView)

            rootView!!.audInfoTitle.text = audiovisual?.title
            rootView!!.audInfoYear.text = audiovisual?.year

            val adapter = SectionRecyclerViewAdapter(context!!,sectionModelArrayList)
            crewRecyclerView!!.adapter = adapter

            if (!audiovisual?.description.isNullOrEmpty()) {
                rootView!!.description_text.text = audiovisual!!.description
            }
        }

    }

    private fun setUpRecyclerView(){
        crewRecyclerView = rootView!!.sectioned_RV
        crewRecyclerView!!.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        crewRecyclerView!!.layoutManager = linearLayoutManager
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ChangeDetailCloseButton) {
            sendInfoToParentInterface = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        if (sendInfoToParentInterface != null) {
            sendInfoToParentInterface = null
        }
    }

    override fun onResume() {
        super.onResume()
        setOffsetListener()
    }

    override fun onPause() {
        super.onPause()
        deleteOffsetListener()
    }

    private fun setOffsetListener(){
        if (!appbarListenerOffsetOn) {
            rootView!!.appbar.addOnOffsetChangedListener(onOffsetChangeToolbarListener)
            appbarListenerOffsetOn = true
        }
    }

    private fun deleteOffsetListener(){
        if (appbarListenerOffsetOn) {
            rootView!!.appbar.removeOnOffsetChangedListener(onOffsetChangeToolbarListener)
            appbarListenerOffsetOn = false
        }
    }

    private fun hideFabShowToolbarButton(hide: Boolean) {
        if (hide) {
            sendInfoToParentInterface?.hideChangeToMultimediaButton(true)
            if (rootView!!.closeButtonFromToolbar.visibility == View.INVISIBLE) {
                rootView!!.closeButtonFromToolbar.visibility = View.VISIBLE
                rootView!!.closeButtonFromToolbar.startAnimation(popupAnimation)
            }
        } else {
            if (rootView!!.closeButtonFromToolbar.visibility == View.VISIBLE && !outAnimationOn) {
                rootView!!.closeButtonFromToolbar.startAnimation(popOutAnimation)
            }
        }
    }

    private fun setUpAnimations(){
        popupAnimation = AnimationUtils.loadAnimation(context, R.anim.popup)
        popOutAnimation = AnimationUtils.loadAnimation(context, R.anim.popout_fast)
        popOutAnimation?.setAnimationListener(popOutAnimationListener)
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
