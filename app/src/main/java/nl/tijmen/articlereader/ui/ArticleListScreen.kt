package nl.tijmen.articlereader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PullToRefreshContainer
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import nl.tijmen.articlereader.data.FeedSource
import nl.tijmen.articlereader.ui.components.ArticleCard
import nl.tijmen.articlereader.ui.components.ErrorState
import nl.tijmen.articlereader.util.openInCustomTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListScreen(
    viewModel: ArticleViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val filters = ArticleFilter.entries

    val pullToRefreshState = rememberPullToRefreshState()

    // Trigger ViewModel refresh when user pulls
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(Unit) { viewModel.refresh() }
    }
    // Stop indicator when ViewModel is done
    LaunchedEffect(state.isRefreshing) {
        if (!state.isRefreshing) pullToRefreshState.endRefresh()
    }

    Column(modifier = modifier.fillMaxSize()) {

        // Segmented filter buttons
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            filters.forEachIndexed { index, filter ->
                SegmentedButton(
                    selected = state.selectedFilter == filter,
                    onClick = { viewModel.setFilter(filter) },
                    shape = SegmentedButtonDefaults.itemShape(index, filters.size),
                    label = { Text(filter.label) }
                )
            }
        }

        // Last refresh time
        if (state.lastRefreshTime > 0L) {
            Text(
                text = "Bijgewerkt: ${formatRefreshTime(state.lastRefreshTime)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 4.dp)
            )
        }

        // Error banners per source
        FeedSource.entries.forEach { source ->
            val sourceState = state.sourceStates[source]
            if (sourceState?.loadState == SourceLoadState.UNAVAILABLE ||
                sourceState?.loadState == SourceLoadState.ERROR
            ) {
                ErrorState(
                    message = "${source.displayName}: ${sourceState.error}",
                    onRetry = { viewModel.load(source) },
                    onOpenBrowser = if (sourceState.loadState == SourceLoadState.UNAVAILABLE) {
                        { context.openInCustomTab(source.siteUrl) }
                    } else null,
                    compact = true
                )
            }
        }

        // Article list with pull-to-refresh
        val articles = state.displayedArticles

        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            if (articles.isEmpty() && !state.isAnyLoading && !state.isRefreshing) {
                Text(
                    text = if (state.selectedFilter == ArticleFilter.SAVED)
                        "Nog niets opgeslagen"
                    else
                        "Geen artikelen beschikbaar",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(articles, key = { it.link }) { article ->
                        ArticleCard(
                            article = article,
                            isRead = article.link in state.readLinks,
                            isSaved = article.link in state.savedLinks,
                            onSave = { viewModel.toggleSave(article) },
                            onClick = {
                                viewModel.markAsRead(article.link)
                                context.openInCustomTab(article.link)
                            }
                        )
                    }
                }
            }

            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

private fun formatRefreshTime(timestamp: Long): String =
    SimpleDateFormat("HH:mm", Locale("nl", "NL")).format(Date(timestamp))
