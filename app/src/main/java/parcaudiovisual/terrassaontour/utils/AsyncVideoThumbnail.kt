package parcaudiovisual.terrassaontour.utils

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.AsyncTask
import java.lang.Exception

class AsyncVideoThumbnail: AsyncTask<String, Void, Void>(){

    var bitmap: Bitmap? = null
    var taskListener: OnThumbnailLoaded? = null

    override fun doInBackground(vararg params: String?): Void? {
        val mediaMetadataRetriever = MediaMetadataRetriever()

        try {
            mediaMetadataRetriever.setDataSource(params[0],HashMap<String,String>())
            bitmap = mediaMetadataRetriever.frameAtTime

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaMetadataRetriever.release()
        }

        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        taskListener?.onThumbnailLoaded(bitmap)
    }
}
interface OnThumbnailLoaded{
    fun onThumbnailLoaded(bitmap: Bitmap?)
}
