package nl.tijmen.articlereader.data

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale

object RssParser {

    private val NS: String? = null
    private val RFC2822 = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
    private val DISPLAY_FORMAT = SimpleDateFormat("d MMM yyyy", Locale("nl", "NL"))

    fun parse(inputStream: InputStream, source: FeedSource): List<Article> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)

        val articles = mutableListOf<Article>()
        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                articles.add(readItem(parser, source))
            }
            eventType = parser.next()
        }
        return articles
    }

    private fun readItem(parser: XmlPullParser, source: FeedSource): Article {
        var title = ""
        var link = ""
        var description = ""
        var pubDate = ""
        var timestamp = 0L
        var author = ""
        var imageUrl: String? = null

        parser.require(XmlPullParser.START_TAG, NS, "item")
        while (!(parser.next() == XmlPullParser.END_TAG && parser.name == "item")) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                "title"       -> title = readText(parser)
                "link"        -> link = readText(parser)
                "description" -> description = stripHtml(readText(parser))
                "pubDate"     -> {
                    val raw = readText(parser)
                    pubDate = formatDate(raw)
                    timestamp = parseTimestamp(raw)
                }
                "dc:creator"  -> author = readText(parser)
                "creator"     -> if (author.isEmpty()) author = readText(parser)
                "media:content", "media:thumbnail" -> {
                    val url = parser.getAttributeValue(NS, "url")
                    if (url != null && imageUrl == null) imageUrl = url
                    skip(parser)
                }
                "enclosure" -> {
                    val type = parser.getAttributeValue(NS, "type") ?: ""
                    if (type.startsWith("image") && imageUrl == null) {
                        imageUrl = parser.getAttributeValue(NS, "url")
                    }
                    skip(parser)
                }
                else -> skip(parser)
            }
        }

        return Article(
            title = decodeHtmlEntities(title.trim()),
            link = link.trim(),
            description = description.trim().take(200),
            pubDate = pubDate,
            timestamp = timestamp,
            author = author.trim(),
            imageUrl = imageUrl,
            source = source
        )
    }

    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) return
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.START_TAG -> depth++
                XmlPullParser.END_TAG   -> depth--
            }
        }
    }

    private fun stripHtml(html: String): String =
        android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_COMPACT)
            .toString().replace("\n\n", " ").trim()

    private fun decodeHtmlEntities(text: String): String =
        text.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
            .replace("&quot;", "\"").replace("&#8217;", "\u2019")
            .replace("&#8216;", "\u2018").replace("&#8220;", "\u201C")
            .replace("&#8221;", "\u201D")

    private fun formatDate(raw: String): String {
        return try {
            val date = RFC2822.parse(raw) ?: return raw
            DISPLAY_FORMAT.format(date)
        } catch (e: Exception) { raw }
    }

    private fun parseTimestamp(raw: String): Long = try {
        RFC2822.parse(raw)?.time ?: 0L
    } catch (e: Exception) { 0L }
}
