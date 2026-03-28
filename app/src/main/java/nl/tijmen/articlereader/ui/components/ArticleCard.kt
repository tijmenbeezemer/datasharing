package nl.tijmen.articlereader.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.tijmen.articlereader.data.Article
import nl.tijmen.articlereader.data.FeedSource
import nl.tijmen.articlereader.ui.theme.EconOnPrimary
import nl.tijmen.articlereader.ui.theme.EconPrimary
import nl.tijmen.articlereader.ui.theme.FoodOnPrimary
import nl.tijmen.articlereader.ui.theme.FoodPrimary

@Composable
fun ArticleCard(
    article: Article,
    onClick: () -> Unit,
    isRead: Boolean = false,
    isSaved: Boolean = false,
    onSave: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isRead) 0.55f else 1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp)) {
            // Top row: source badge + date + bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                if (onSave != null) {
                    IconButton(onClick = onSave, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark
                            else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (isSaved) "Verwijder uit opgeslagen"
                            else "Opslaan voor later",
                            tint = if (isSaved) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Title
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 12.dp)
            )

            // Description
            if (article.description.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = article.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        }
    }
}

@Composable
fun SourceBadge(source: FeedSource, modifier: Modifier = Modifier) {
    val (bg, fg) = when (source) {
        FeedSource.STUK_ROOD_VLEES -> FoodPrimary to FoodOnPrimary
        FeedSource.ESB -> EconPrimary to EconOnPrimary
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
