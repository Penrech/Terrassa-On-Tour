package parcaudiovisual.terrassaontour.interfaces

interface AppStateChange {
    fun appStateChange(appActive: Boolean, message: String?)
    fun reloadData()
    fun dayTimeChange()
}