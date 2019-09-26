package parcaudiovisual.terrassaontour.database

import androidx.room.*

@Dao
interface AudiovisualDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAudiovisuals(vararg audiovisuals: Audiovisual)

    @Query("DELETE FROM audiovisual_table WHERE id in (:audsToDelete) ")
    fun deleteSpecificAudiovisual(audsToDelete: ArrayList<String>)

    @Query("SELECT * FROM audiovisual_table WHERE id = :audID")
    fun selectAudByID(audID: String): Audiovisual?

    @Query("DELETE FROM audiovisual_table")
    fun deleteAllAudiovisuals()

    @Transaction
    fun insertAndDeleteAudiovisualsTransaction(audiovisuals: Array<Audiovisual>) {
        deleteAllAudiovisuals()
        insertAudiovisuals(*audiovisuals)
    }
}