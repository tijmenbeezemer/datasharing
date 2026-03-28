package nl.tijmen.articlereader.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_articles")
data class SavedArticleEntity(
    @PrimaryKey val link: String,
    val title: String,
    val description: String,
    val pubDate: String,
    val timestamp: Long,
    val author: String,
    val imageUrl: String?,
    val source: String
)
