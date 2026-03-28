package nl.tijmen.articlereader.data

enum class FeedSource(
    val displayName: String,
    val feedUrl: String,
    val siteUrl: String
) {
    STUK_ROOD_VLEES(
        displayName = "Stuk Rood Vlees",
        feedUrl = "https://www.stukroodvlees.nl/feed/",
        siteUrl = "https://www.stukroodvlees.nl"
    ),
    ESB(
        displayName = "ESB",
        feedUrl = "https://esb.nu/feed/",
        siteUrl = "https://esb.nu"
    )
}
