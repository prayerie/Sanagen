package cafe.rob.sanagen

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(w: Word)

    @Query("select * from words")
    fun getAll(): Flow<List<Word>>

    @Delete
    suspend fun delete(w: Word)

    @Query("delete from words where word = :w")
    suspend fun deleteBy(w: String)

    @Query("delete from words")
    suspend fun deleteAll()
}