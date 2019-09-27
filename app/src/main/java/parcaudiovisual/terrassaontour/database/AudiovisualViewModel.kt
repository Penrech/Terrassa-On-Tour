package parcaudiovisual.terrassaontour.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class AudiovisualViewModel(application: Application): AndroidViewModel(application) {
    private val repository: AudiovisualRepository = AudiovisualRepository(application)

    fun deleteSpecificAudiovisual(audiovisualToDelete: ArrayList<String>){
        repository.deleteSpecificAudiovisual(audiovisualToDelete)
    }

    suspend fun selectAudiovisualByID(audiovisualID: String): Audiovisual? {
        return repository.selectAudiovisualById(audiovisualID)
    }

    suspend fun selectAudiovisualsByID(audiovisualsArray: ArrayList<String>): List<Audiovisual>{
        return repository.selectAudiovisualsById(audiovisualsArray)
    }

    suspend fun getAudiovisualFromServerDB(): Boolean {
        return repository.getAudiovisualFromServerDB()
    }
}