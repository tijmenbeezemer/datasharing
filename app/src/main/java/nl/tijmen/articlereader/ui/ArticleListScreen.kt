package nl.tijmen.articlereader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
    source: FeedSource,
    viewModel: ArticleViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.stateFor(source).collectAsState()
    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize()) {
        when (val s = state) {
            is FeedUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is FeedUiState.Success -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(s.articles, key = { it.link }) { article ->
                        ArticleCard(
                            article = article,
                            onClick = { context.openInCustomTab(article.link) }
                        )
                    }
                }
            }
            is FeedUiState.Error -> {
                ErrorState(
                    message = s.message,
                    onRetry = { viewModel.load(source) },
                    onOpenBrowser = if (s.isFeedUnavailable) {
                        { context.openInCustomTab(source.siteUrl) }
                    } else null,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
