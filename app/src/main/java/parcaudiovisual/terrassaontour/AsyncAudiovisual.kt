package parcaudiovisual.terrassaontour

import android.os.AsyncTask
import java.util.concurrent.Callable

class AsyncAudiovisual: AsyncTask<Callable<Pair<Boolean, ArrayList<Audiovisual>>>, Void, Void>(){

    var audiovisuales: Pair<Boolean, ArrayList<Audiovisual>>? = null
    var taskListener: OnAudiovisuaDownloadCompleted? = null

    override fun doInBackground(vararg params: Callable<Pair<Boolean, ArrayList<Audiovisual>>>?): Void? {
        audiovisuales =  params.first()?.call()
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        taskListener?.onAudiovisualDonwloaded(audiovisuales)
    }
}
interface OnAudiovisuaDownloadCompleted{
    fun onAudiovisualDonwloaded(result: Pair<Boolean, ArrayList<Audiovisual>>?)
}