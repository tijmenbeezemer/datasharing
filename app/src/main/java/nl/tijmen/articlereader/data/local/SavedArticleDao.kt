package nl.tijmen.articlereader.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedArticleDao {

    @Query("SELECT * FROM saved_articles ORDER BY timestamp DESC")
    fun getAll(): Flow<List<SavedArticleEntity>>

    @Query("SELECT link FROM saved_articles")
    fun getAllLinks(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(article: SavedArticleEntity)

    @Query("DELETE FROM saved_articles WHERE link = :link")
    suspend fun delete(link: String)
}
