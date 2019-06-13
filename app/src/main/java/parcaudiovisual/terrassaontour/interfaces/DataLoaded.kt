package parcaudiovisual.terrassaontour.interfaces

interface DataLoaded {
    fun pointsLoaded(succes: Boolean)
    fun rutesLoaded(succes: Boolean)
    fun audiovisualsLoaded(succes: Boolean)
    fun allLoaded(succes: Boolean)
}