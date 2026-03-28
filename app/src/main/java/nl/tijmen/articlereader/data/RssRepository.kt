package nl.tijmen.articlereader.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Fetches RSS articles for a given FeedSource.
 *
 * 403 handling strategy (layered):
 *  1. Primary request with browser-like User-Agent — resolves most WordPress 403s.
 *  2. On 403, retry with alternate feed URL variants.
 *  3. If all variants fail, throw FeedUnavailableException so the UI shows
 *     the "Open site in browser" fallback action.
 */
class RssRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val browserUserAgent =
        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/124.0.6367.82 Mobile Safari/537.36"

    private fun fallbackUrls(source: FeedSource): List<String> = listOf(
        source.feedUrl,
        "${source.siteUrl}/?feed=rss2",
        "${source.siteUrl}/rss",
        "${source.siteUrl}/feed/rss/"
    )

    suspend fun fetchArticles(source: FeedSource): List<Article> =
        withContext(Dispatchers.IO) {
            var lastException: Exception? = null
            for (url in fallbackUrls(source)) {
                try {
                    val result = fetchUrl(url)
                    if (result != null) return@withContext result
                } catch (e: FeedUnavailableException) {
                    lastException = e
                } catch (e: Exception) {
                    lastException = e
                }
            }
            throw lastException ?: FeedUnavailableException(source, "All feed URLs failed")
        }

    private fun fetchUrl(url: String): List<Article>? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", browserUserAgent)
            .header("Accept", "application/rss+xml, application/xml, text/xml, */*")
            .header("Accept-Language", "nl,en;q=0.9")
            .build()

        val response = client.newCall(request).execute()
        return when {
            response.isSuccessful -> {
                val body = response.body ?: return null
                body.byteStream().use { RssParser.parse(it) }
            }
            response.code == 403 -> null
            else -> throw Exception("HTTP ${response.code} from $url")
        }
    }
}

class FeedUnavailableException(
    val source: FeedSource,
    message: String
) : Exception(message)
