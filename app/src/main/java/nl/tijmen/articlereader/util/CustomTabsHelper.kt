package nl.tijmen.articlereader.util

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

/**
 * Opens a URL in a Chrome Custom Tab, following the system dark/light setting.
 * Falls back to any available browser if Custom Tabs are not supported.
 */
fun Context.openInCustomTab(url: String) {
    val uri = Uri.parse(url)
    val customTabsIntent = CustomTabsIntent.Builder()
        .setShowTitle(true)
        .setUrlBarHidingEnabled(true)
        .setShareState(CustomTabsIntent.SHARE_STATE_ON)
        .setColorScheme(CustomTabsIntent.COLOR_SCHEME_SYSTEM)
        .build()
    customTabsIntent.launchUrl(this, uri)
}
