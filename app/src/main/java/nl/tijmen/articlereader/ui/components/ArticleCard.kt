package nl.tijmen.articlereader.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.tijmen.articlereader.data.Article
import nl.tijmen.articlereader.data.FeedSource
import nl.tijmen.articlereader.ui.theme.EconPrimary
import nl.tijmen.articlereader.ui.theme.EconOnPrimary
import nl.tijmen.articlereader.ui.theme.FoodPrimary
import nl.tijmen.articlereader.ui.theme.FoodOnPrimary

@Composable
fun ArticleCard(
    article: Article,
    onClick: () -> Unit,
    showSourceBadge: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (showSourceBadge) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SourceBadge(source = article.source)
                    if (article.pubDate.isNotEmpty()) {
                        Text(
                            text = article.pubDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (!showSourceBadge && (article.author.isNotEmpty() || article.pubDate.isNotEmpty())) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = buildString {
                        if (article.author.isNotEmpty()) append(article.author)
                        if (article.author.isNotEmpty() && article.pubDate.isNotEmpty()) append(" \u00B7 ")
                        if (article.pubDate.isNotEmpty()) append(article.pubDate)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (article.description.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = article.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SourceBadge(source: FeedSource, modifier: Modifier = Modifier) {
    val (bg, fg) = when (source) {
        FeedSource.STUK_ROOD_VLEES -> FoodPrimary to FoodOnPrimary
        FeedSource.ESB             -> EconPrimary to EconOnPrimary
    }
    Surface(
        color = bg,
        contentColor = fg,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Text(
            text = source.displayName,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
