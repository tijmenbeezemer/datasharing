package nl.tijmen.articlereader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import nl.tijmen.articlereader.data.FeedSource
import nl.tijmen.articlereader.ui.components.ArticleCard
import nl.tijmen.articlereader.ui.components.ErrorState
import nl.tijmen.articlereader.util.openInCustomTab

@Composable
fun ArticleListScreen(
    viewModel: ArticleViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxSize()) {
        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FeedSource.entries.forEach { source ->
                val isSelected = source in state.activeFilters
                val sourceState = state.sourceStates[source]
                val isLoading = sourceState?.loadState == SourceLoadState.LOADING

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.toggleFilter(source) },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(source.displayName)
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp
                                )
                            }
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Error banners for unavailable feeds
        FeedSource.entries.forEach { source ->
            val sourceState = state.sourceStates[source]
            if (sourceState?.loadState == SourceLoadState.UNAVAILABLE ||
                sourceState?.loadState == SourceLoadState.ERROR) {
                FeedErrorBanner(
                    source = source,
                    message = sourceState.error ?: "Fout",
                    isUnavailable = sourceState.loadState == SourceLoadState.UNAVAILABLE,
                    onRetry = { viewModel.load(source) },
                    onOpenBrowser = { context.openInCustomTab(source.siteUrl) }
                )
            }
        }

        val articles = state.filteredArticles

        if (articles.isEmpty() && !state.isAnyLoading) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Geen artikelen beschikbaar",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(articles, key = { it.link }) { article ->
                    ArticleCard(
                        article = article,
                        onClick = { context.openInCustomTab(article.link) },
                        showSourceBadge = true
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedErrorBanner(
    source: FeedSource,
    message: String,
    isUnavailable: Boolean,
    onRetry: () -> Unit,
    onOpenBrowser: () -> Unit
) {
    ErrorState(
        message = "${source.displayName}: $message",
        onRetry = onRetry,
        onOpenBrowser = if (isUnavailable) onOpenBrowser else null,
        compact = true
    )
}
