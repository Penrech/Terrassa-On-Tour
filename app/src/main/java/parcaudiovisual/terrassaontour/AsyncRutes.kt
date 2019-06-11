package parcaudiovisual.terrassaontour

import android.os.AsyncTask
import java.util.concurrent.Callable

class AsyncRutes: AsyncTask<Callable<ArrayList<Ruta?>>, Void, Void>(){

    var rutas: ArrayList<Ruta?>? = null
    var taskListener: OnRutesDownloadCompleted? = null

    override fun doInBackground(vararg params: Callable<ArrayList<Ruta?>>?): Void? {
        rutas =  params.first()?.call()
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        taskListener?.onRutesDownloaded(rutas)
    }
}
interface OnRutesDownloadCompleted{
    fun onRutesDownloaded(arrayList: ArrayList<Ruta?>?)
}