package nl.tijmen.articlereader.data

data class Article(
    val title: String,
    val link: String,
    val description: String,
    val pubDate: String,
    val author: String = "",
    val imageUrl: String? = null
)
