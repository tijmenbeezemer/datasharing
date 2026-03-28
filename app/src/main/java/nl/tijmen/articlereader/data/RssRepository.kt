package nl.tijmen.articlereader.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class RssRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val browserUserAgent =
        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/124.0.6367.82 Mobile Safari/537.36"

    private fun fallbackUrls(source: FeedSource): List<String> = when (source) {
        FeedSource.STUK_ROOD_VLEES -> listOf(
            "https://www.stukroodvlees.nl/feed/",
            "https://stukroodvlees.nl/feed/",
            "https://www.stukroodvlees.nl/?feed=rss2"
        )
        FeedSource.ESB -> listOf(
            "https://esb.nl/rss",
            "https://esb.nl/feed/",
            "https://esb.nl/rss.xml",
            "https://esb.nl/atom.xml",
            "https://esb.nl/?feed=rss2",
            "https://esb.nl/artikelen/rss",
            "https://www.esb.nl/rss",
            "https://www.esb.nl/feed/"
        )
    }

    suspend fun fetchArticles(source: FeedSource): List<Article> =
        withContext(Dispatchers.IO) {
            var lastException: Exception? = null
            for (url in fallbackUrls(source)) {
                try {
                    val result = fetchUrl(url, source)
                    if (result != null) return@withContext result
                } catch (e: FeedUnavailableException) {
                    lastException = e
                } catch (e: Exception) {
                    lastException = e
                }
            }
            throw lastException ?: FeedUnavailableException(source, "All feed URLs failed")
        }

    private fun fetchUrl(url: String, source: FeedSource): List<Article>? {
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
                body.byteStream().use { RssParser.parse(it, source) }
            }
            response.code == 403 || response.code == 404 -> null
            else -> throw Exception("HTTP ${response.code} from $url")
        }
    }
}

class FeedUnavailableException(
    val source: FeedSource,
    message: String
) : Exception(message)
