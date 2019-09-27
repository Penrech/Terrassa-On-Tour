package parcaudiovisual.terrassaontour.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class StaticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StaticsRepository = StaticsRepository(application)
    private val

}