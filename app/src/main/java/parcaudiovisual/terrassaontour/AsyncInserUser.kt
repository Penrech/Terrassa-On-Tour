package parcaudiovisual.terrassaontour

import android.os.AsyncTask
import java.util.concurrent.Callable

class AsyncInsertUser(): AsyncTask<Callable<Boolean>, Void, Void>() {

    var success: Boolean? = null
    var taskListener: OnUserInsertionCompleted? = null

    override fun doInBackground(vararg params: Callable<Boolean>?): Void? {
       success = params.first()?.call()
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        taskListener?.userInsertedOnServerDB(success)
    }
}
interface OnUserInsertionCompleted{
    fun userInsertedOnServerDB(success: Boolean?)
}