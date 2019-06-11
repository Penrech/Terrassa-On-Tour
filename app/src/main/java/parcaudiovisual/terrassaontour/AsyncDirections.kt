package parcaudiovisual.terrassaontour

import android.os.AsyncTask
import com.google.android.gms.maps.model.LatLng
import java.util.concurrent.Callable

class AsyncDirections: AsyncTask<Callable<ArrayList<List<LatLng>>>, Void, Void>(){

    var path: ArrayList<List<LatLng>>? = null
    var taskListener: OnDirectionsDownloadedCompleted? = null

    override fun doInBackground(vararg params: Callable<ArrayList<List<LatLng>>>?): Void? {
        path =  params.first()?.call()
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        taskListener?.onPoisDonwloaded(path)
    }
}
interface OnDirectionsDownloadedCompleted{
    fun onPoisDonwloaded(arrayList:  ArrayList<List<LatLng>>?)
}