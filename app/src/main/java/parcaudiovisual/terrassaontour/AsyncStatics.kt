package parcaudiovisual.terrassaontour

import android.os.AsyncTask
import java.util.concurrent.Callable

class AsyncStatics: AsyncTask<Callable<InsertStaticsResponse>, Void, Void>(){

    var serverStaticsResponse: InsertStaticsResponse? = null
    var taskListener: OnServerResponseFromStaticsQuery? = null

    override fun doInBackground(vararg params: Callable<InsertStaticsResponse>?): Void? {
        serverStaticsResponse =  params.first()?.call()
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        taskListener?.onServerResponseFromStaticsQuery(serverStaticsResponse)
    }
}
interface OnServerResponseFromStaticsQuery{
    fun onServerResponseFromStaticsQuery(result: InsertStaticsResponse?)
}
