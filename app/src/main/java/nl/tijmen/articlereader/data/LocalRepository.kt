package nl.tijmen.articlereader.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.tijmen.articlereader.data.local.AppDatabase
import nl.tijmen.articlereader.data.local.ReadLinkEntity
import nl.tijmen.articlereader.data.local.SavedArticleEntity

class LocalRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)

    val savedArticles: Flow<List<Article>> =
        db.savedArticleDao().getAll().map { list -> list.map { it.toArticle() } }

    val savedLinks: Flow<Set<String>> =
        db.savedArticleDao().getAllLinks().map { it.toSet() }

    val readLinks: Flow<Set<String>> =
        db.readLinkDao().getAll().map { it.toSet() }

    suspend fun saveArticle(article: Article) =
        db.savedArticleDao().insert(article.toEntity())

    suspend fun unsaveArticle(link: String) =
        db.savedArticleDao().delete(link)

    suspend fun markAsRead(link: String) =
        db.readLinkDao().insert(ReadLinkEntity(link))
}

private fun Article.toEntity() = SavedArticleEntity(
    link = link,
    title = title,
    description = description,
    pubDate = pubDate,
    timestamp = timestamp,
    author = author,
    imageUrl = imageUrl,
    source = source.name
)

private fun SavedArticleEntity.toArticle() = Article(
    link = link,
    title = title,
    description = description,
    pubDate = pubDate,
    timestamp = timestamp,
    author = author,
    imageUrl = imageUrl,
    source = try { FeedSource.valueOf(source) } catch (e: Exception) { FeedSource.STUK_ROOD_VLEES }
)
