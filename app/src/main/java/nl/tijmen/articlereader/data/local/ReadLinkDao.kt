package nl.tijmen.articlereader.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadLinkDao {

    @Query("SELECT link FROM read_links")
    fun getAll(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: ReadLinkEntity)
}
