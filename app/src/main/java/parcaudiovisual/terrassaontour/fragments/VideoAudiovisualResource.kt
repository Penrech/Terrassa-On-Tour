package parcaudiovisual.terrassaontour.fragments

import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.media.MediaDrm
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaTimestamp
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import parcaudiovisual.terrassaontour.R
import parcaudiovisual.terrassaontour.utils.AsyncVideoThumbnail
import parcaudiovisual.terrassaontour.utils.OnThumbnailLoaded
import parcaudiovisual.terrassaontour.utils.VideoUtility
import java.lang.Exception

private const val VIDEO_URI = "videoUri"

class VideoAudiovisualResource : Fragment() {

    private var AUTO_HIDE = false
    private val AUTO_HIDE_DELAY_MILLIS = 3000
    private val UI_ANIMATION_DELAY = 300

    private var totalTimeTextView: TextView? = null
    private var currentTimeTextView: TextView? = null
    private var fullscreenContent: FrameLayout? = null
    private var fullScreenContentController: LinearLayout? = null
    private var playPauseButton: ImageView? = null
    private var videoSeekBar: SeekBar? = null
    private var videoView: VideoView? = null
    private var videoBackground: ImageView? = null
    private var loadingCircle: ProgressBar? = null
    private var errorLabel: TextView? = null

    private var rootView: View? = null

    private var videoUri: String? = null

    private val mHideHandler = Handler()
    private var videoRunnable: Runnable? = null

    private var utilities: VideoUtility? = null

    private val mHidePart2Runnable = Runnable {

        videoView?.let {
            if (it.isPlaying){
                playPauseButton?.visibility = View.GONE
            }
        }
    }

    private val mShowPart2Runnable = Runnable {
        playPauseButton?.visibility = View.VISIBLE
        fullScreenContentController?.visibility = View.VISIBLE
    }

    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }
    private var videoPath: Uri? = null
    private var videoPositionOnFragmentPause: Int = 0

    private var isVideoPrepare = false
    private var isVideoError = false
    private var waitingForPreparation = false

    private var videoThumbnail: Bitmap? = null

    private val seekBarListener = object : SeekBar.OnSeekBarChangeListener{
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            mHideHandler.removeCallbacks(videoRunnable)
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            mHideHandler.removeCallbacks(videoRunnable)
            val totalDuration = videoView?.duration
            val currentPosition = utilities!!.progressToTimer(seekBar!!.progress, totalDuration!!)

            videoView?.seekTo(currentPosition)

            updateProgressBar()
        }
    }

    private val onMainClickListener = View.OnClickListener {
        toggle()
    }

    private val onPlayPauseClickListener = View.OnClickListener {
        if (videoView == null) return@OnClickListener

        if (isVideoPrepare) {
            playPauseVideo(!videoView!!.isPlaying)
        } else {
            waitingForPreparation = true
            loadingCircle?.visibility = View.VISIBLE
        }

    }

    private val onVideoPrepareListener = MediaPlayer.OnPreparedListener {
        isVideoPrepare = true

        videoThumbnail?.let { bitmap ->
            Log.i("Thumbnail","VideoBackground en video prepare: ${videoBackground!!.background}")
            if (videoBackground?.background is ColorDrawable) {
                Log.i("Thumbnail","Inserto bitmap en video prepare")
                videoBackground!!.setImageBitmap(bitmap)
            }
        }

        fullScreenContentController?.visibility = View.VISIBLE

        loadingCircle?.visibility = View.GONE

        totalTimeTextView?.text = utilities!!.formatSecondsToString(it.duration)

        playPauseButton?.visibility = View.VISIBLE

        it.setOnCompletionListener(onVideoCompleteListener)

        if (videoSeekBar != null && videoSeekBar!!.progress > 0) {
            it.seekTo(videoPositionOnFragmentPause)
            updateProgressBar()
        }

        videoSeekBar?.max = 100

        if (waitingForPreparation) {
            loadingCircle?.visibility = View.GONE
            playPauseVideo(true)
            waitingForPreparation = false
        }
    }

    private val onVideoErrorListener = MediaPlayer.OnErrorListener { mp, what, extra ->
        Log.e("VideoError","Error video: $mp")
        Log.e("VideoError","Error video type: $what")
        Log.e("VideoError","Error video extra data: $extra")
        Log.e("VideoError","Error video link: $videoUri")

        enableErrorUI()
        isVideoError = true

        true
    }
    
    private val onVideoBufferingInfoListener = MediaPlayer.OnInfoListener { mp, what, extra ->
        val returnValue: Boolean
        when (what) {
            MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                Log.i("VideoBuffer","Video start rendering")
                returnValue = true
            }
            MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                Log.i("VideoBuffer","Video start buffering")
                loadingCircle?.visibility = View.VISIBLE
                returnValue = true
            }
            MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                Log.i("VideoBuffer","Video end buffering")
                loadingCircle?.visibility = View.INVISIBLE
                returnValue = true
            }
            else -> returnValue = false
        }
        returnValue
    }


    private val onVideoCompleteListener = MediaPlayer.OnCompletionListener {
        Log.i("TAG","Completado")
        restartVideo()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {

        } else {
            arguments?.let {
                videoUri = it.getString(VIDEO_URI)
                videoPath = Uri.parse(videoUri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_video_audiovisual_resource, container, false)

        playPauseButton = rootView?.findViewById<ImageView>(R.id.playPauseButton)
        fullScreenContentController = rootView?.findViewById<LinearLayout>(R.id.fullscreen_content_controls)
        fullscreenContent = rootView?.findViewById<FrameLayout>(R.id.fullscreen_content)
        totalTimeTextView = rootView?.findViewById<TextView>(R.id.totalTime)
        currentTimeTextView = rootView?.findViewById<TextView>(R.id.currentTime)
        videoSeekBar = rootView?.findViewById<SeekBar>(R.id.videoSeekbar)
        videoView = rootView?.findViewById<VideoView>(R.id.videoView)
        videoBackground = rootView?.findViewById<ImageView>(R.id.videoBackground)
        loadingCircle = rootView?.findViewById<ProgressBar>(R.id.progressBar)
        errorLabel = rootView?.findViewById<TextView>(R.id.errorVideoLabel)

        utilities = VideoUtility()

        loadingCircle?.visibility = View.VISIBLE

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRunnable()
        getVideoThumbnail()
        setUpVideo()
        mVisible = true

        fullscreenContent?.setOnClickListener(onMainClickListener)
        playPauseButton?.setOnClickListener(onPlayPauseClickListener)
        videoSeekBar?.setOnSeekBarChangeListener(seekBarListener)
    }

    private fun getVideoThumbnail(){
        /*AsyncVideoThumbnail().let {
            it.execute(videoUri)

            it.taskListener = object : OnThumbnailLoaded {
                override fun onThumbnailLoaded(bitmap: Bitmap?) {
                    videoThumbnail = bitmap
                    Log.i("Thumbnail","Thumbnail recibida: $bitmap")
                    Log.i("Thumbnail","VideoBackground en callback: ${videoBackground!!.background}")
                    videoThumbnail?.let {
                        if (isVideoPrepare && videoBackground!!.background is ColorDrawable) {
                            Log.i("Thumbnail","Inserto bitmap en callback")
                            videoBackground!!.setImageBitmap(it)
                        }
                    }
                }
            }
        }*/
        CoroutineScope(IO).launch {
            var bitmap: Bitmap? = null
            val mediaMetadataRetriever = MediaMetadataRetriever()

            try {
                mediaMetadataRetriever.setDataSource(videoUri,HashMap<String,String>())
                bitmap = mediaMetadataRetriever.frameAtTime

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("GettingVideoThumb","Error getting video thumb: $e")
            } finally {
                mediaMetadataRetriever.release()
            }

            videoThumbnail = bitmap

            withContext(Main){
                videoThumbnail?.let {
                    if (isVideoPrepare && videoBackground!!.background is ColorDrawable) {
                        videoBackground!!.setImageBitmap(it)
                    }
                }
            }
        }
    }

    private fun setUpPlayButton(){
        if (videoView!!.isPlaying)
            playPauseButton?.setImageDrawable(context?.getDrawable(R.drawable.ic_round_pause_circle_filled_100))
        else
            playPauseButton?.setImageDrawable(context?.getDrawable(R.drawable.ic_round_play_circle_filled_white_100))
    }

    private fun restartVideo(){
        videoView?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
        show()
        setUpPlayButton()
        if (videoBackground?.visibility == View.INVISIBLE){
            videoBackground?.visibility = View.VISIBLE
        }
        videoView?.seekTo(1)
        updateProgressBar()
        videoSeekBar?.progress = 0
        cancelDelayedHide()
        AUTO_HIDE = false
    }

    private fun enableErrorUI(){
        loadingCircle?.visibility = View.GONE
        videoBackground?.setImageDrawable(context?.getDrawable(android.R.color.background_dark))
        errorLabel?.visibility = View.VISIBLE

        hide()
    }

    private fun setUpVideo(){

        videoView?.setVideoURI(videoPath)
        videoView?.setOnPreparedListener(onVideoPrepareListener)
        videoView?.setOnErrorListener(onVideoErrorListener)
        videoView?.setOnInfoListener(onVideoBufferingInfoListener)

    }

    private fun playPauseVideo(play: Boolean){

        if (play){
            if (videoBackground?.visibility == View.VISIBLE){
                videoBackground?.visibility = View.INVISIBLE
            }

            videoView?.start()
            updateProgressBar()
            setUpPlayButton()
            AUTO_HIDE = true
            hide()

        } else {
            videoView?.pause()
            mHideHandler.removeCallbacks(videoRunnable)
            setUpPlayButton()
            cancelDelayedHide()
            AUTO_HIDE = false
            show()

        }
    }

    fun updateProgressBar(){
        mHideHandler.removeCallbacksAndMessages(videoRunnable)
        mHideHandler.postDelayed(videoRunnable, 100)
    }

    private fun setRunnable() {
        videoRunnable = Runnable {

            val totalDuration = videoView?.duration
            val currentDuration = videoView?.currentPosition

            totalTimeTextView?.text = utilities!!.formatSecondsToString(totalDuration!!)
            currentTimeTextView?.text = utilities!!.formatSecondsToString(currentDuration!!)

            val progress = utilities!!.getProgressPercentage(currentDuration,totalDuration)
            rootView?.findViewById<SeekBar>(R.id.videoSeekbar)?.progress = progress

            if (videoView?.isPlaying!!)
                mHideHandler.postDelayed(videoRunnable, 100)
        }
    }

    private fun hide() {
        fullScreenContentController?.visibility = View.GONE

        mVisible = false

        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {

        mVisible = true

        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    private fun cancelDelayedHide(){
        mHideHandler.removeCallbacks(mHideRunnable)
    }

    private fun toggle() {
        if (!isVideoPrepare || isVideoError) return

        if (mVisible) {
            hide()
        } else {
            show()
            if (AUTO_HIDE)
                delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
    }

    override fun onPause() {
        super.onPause()
        mHideHandler.removeCallbacks(videoRunnable)

        isVideoPrepare = false

        videoView?.let {
            if (it.isPlaying) {
                playPauseVideo(false)
            }
            videoPositionOnFragmentPause = videoView!!.currentPosition
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mHideHandler.removeCallbacks(videoRunnable)
        videoView?.stopPlayback()
    }

    fun pauseOnPageChange(){
        if (isVideoPrepare) playPauseVideo(false)
    }

    companion object {

        @JvmStatic
        fun newInstance(videoURL: String?) =
            VideoAudiovisualResource().apply {
                arguments = Bundle().apply {
                    putString(VIDEO_URI, videoURL)
                }
            }
    }
}
