package parcaudiovisual.terrassaontour

import android.os.AsyncTask
import java.util.concurrent.Callable

class AsyncPois(): AsyncTask<Callable<Pair<Boolean,ArrayList<PuntoInteres>>>, Void, Void>(){

    var puntosDeInteres: Pair<Boolean, ArrayList<PuntoInteres>>? = null
    var taskListener: OnDownloadsCompleted? = null

    override fun doInBackground(vararg params: Callable<Pair<Boolean, ArrayList<PuntoInteres>>>?): Void? {
        puntosDeInteres =  params.first()?.call()
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        taskListener?.onPoisDonwloaded(puntosDeInteres)
    }
}
interface OnDownloadsCompleted{
    fun onPoisDonwloaded(result: Pair<Boolean, ArrayList<PuntoInteres>>?)
}