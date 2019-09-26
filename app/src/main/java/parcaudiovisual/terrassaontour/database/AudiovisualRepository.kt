package parcaudiovisual.terrassaontour.database

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import parcaudiovisual.terrassaontour.OnlineQueries.ServerServices

class AudiovisualRepository(application: Application) {

    private var audiovisualDao: AudiovisualDao
    private var serverServices: ServerServices

    init {
        val database = TOTDatabase.getInstance(application)
        audiovisualDao = database.audiovisualDao()
        serverServices = ServerServices.getInstance()
    }

    private fun insertNewAudiovisuals(audiovisuals: Array<Audiovisual>){
        CoroutineScope(IO).launch {
            audiovisualDao.insertAndDeleteAudiovisualsTransaction(audiovisuals)
        }
    }

    fun deleteSpecificAudiovisual(audiovisualToDelete: ArrayList<String>){
        CoroutineScope(IO).launch {
            audiovisualDao.deleteSpecificAudiovisual(audiovisualToDelete)
        }
    }

    suspend fun selectAudiovisualById(audiovisualID: String): Audiovisual? {
        val audiovisual = CoroutineScope(IO).async {
            audiovisualDao.selectAudByID(audiovisualID)
        }

        return audiovisual.await()
    }

    suspend fun getAudiovisualFromServerDB(): Boolean {
        val result = CoroutineScope(IO).async {
            serverServices.getAudiovisuals()
        }

        if (!result.await().first) return false

        insertNewAudiovisuals(result.await().second.toTypedArray())

        return true
    }

}