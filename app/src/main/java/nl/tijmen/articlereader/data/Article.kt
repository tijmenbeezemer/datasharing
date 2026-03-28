package nl.tijmen.articlereader.data

data class Article(
    val title: String,
    val link: String,
    val description: String,
    val pubDate: String,        // formatted display string
    val timestamp: Long = 0L,  // epoch ms for sorting
    val author: String = "",
    val imageUrl: String? = null,
    val source: FeedSource
)
