package nl.tijmen.articlereader.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.tijmen.articlereader.data.Article
import nl.tijmen.articlereader.data.FeedSource
import nl.tijmen.articlereader.data.FeedUnavailableException
import nl.tijmen.articlereader.data.LocalRepository
import nl.tijmen.articlereader.data.RssRepository
import java.util.concurrent.atomic.AtomicInteger

enum class SourceLoadState { LOADING, SUCCESS, ERROR, UNAVAILABLE }

data class SourceState(
    val loadState: SourceLoadState = SourceLoadState.LOADING,
    val error: String? = null
)

data class CombinedUiState(
    val articles: List<Article> = emptyList(),
    val sourceStates: Map<FeedSource, SourceState> = FeedSource.entries.associateWith { SourceState() },
    val selectedFilter: ArticleFilter = ArticleFilter.ALL,
    val savedArticles: List<Article> = emptyList(),
    val savedLinks: Set<String> = emptySet(),
    val readLinks: Set<String> = emptySet(),
    val lastRefreshTime: Long = 0L,
    val isRefreshing: Boolean = false
) {
    val displayedArticles: List<Article>
        get() = when (selectedFilter) {
            ArticleFilter.ALL -> articles.sortedByDescending { it.timestamp }
            ArticleFilter.STUK_ROOD_VLEES -> articles
                .filter { it.source == FeedSource.STUK_ROOD_VLEES }
                .sortedByDescending { it.timestamp }
            ArticleFilter.ESB -> articles
                .filter { it.source == FeedSource.ESB }
                .sortedByDescending { it.timestamp }
            ArticleFilter.SAVED -> savedArticles.sortedByDescending { it.timestamp }
        }

    val isAnyLoading: Boolean
        get() = sourceStates.values.any { it.loadState == SourceLoadState.LOADING }
}

class ArticleViewModel(application: Application) : AndroidViewModel(application) {

    private val rssRepo = RssRepository()
    private val localRepo = LocalRepository(application)

    private val articlesBySource = mutableMapOf<FeedSource, List<Article>>()
    private val refreshCount = AtomicInteger(0)

    private val _state = MutableStateFlow(CombinedUiState())
    val state: StateFlow<CombinedUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            localRepo.savedArticles.collect { articles ->
                _state.update { it.copy(savedArticles = articles) }
            }
        }
        viewModelScope.launch {
            localRepo.savedLinks.collect { links ->
                _state.update { it.copy(savedLinks = links) }
            }
        }
        viewModelScope.launch {
            localRepo.readLinks.collect { links ->
                _state.update { it.copy(readLinks = links) }
            }
        }
        FeedSource.entries.forEach { load(it) }
    }

    fun load(source: FeedSource) {
        _state.update { current ->
            current.copy(sourceStates = current.sourceStates + (source to SourceState(SourceLoadState.LOADING)))
        }
        viewModelScope.launch {
            try {
                val articles = rssRepo.fetchArticles(source)
                articlesBySource[source] = articles
                _state.update { current ->
                    current.copy(
                        articles = articlesBySource.values.flatten(),
                        sourceStates = current.sourceStates + (source to SourceState(SourceLoadState.SUCCESS)),
                        lastRefreshTime = System.currentTimeMillis()
                    )
                }
            } catch (e: FeedUnavailableException) {
                _state.update { current ->
                    current.copy(
                        sourceStates = current.sourceStates + (source to SourceState(
                            loadState = SourceLoadState.UNAVAILABLE,
                            error = "Feed niet beschikbaar"
                        ))
                    )
                }
            } catch (e: Exception) {
                _state.update { current ->
                    current.copy(
                        sourceStates = current.sourceStates + (source to SourceState(
                            loadState = SourceLoadState.ERROR,
                            error = e.message ?: "Onbekende fout"
                        ))
                    )
                }
            } finally {
                if (refreshCount.get() > 0 && refreshCount.decrementAndGet() == 0) {
                    _state.update { it.copy(isRefreshing = false) }
                }
            }
        }
    }

    fun refresh() {
        refreshCount.set(FeedSource.entries.size)
        _state.update { it.copy(isRefreshing = true) }
        FeedSource.entries.forEach { load(it) }
    }

    fun setFilter(filter: ArticleFilter) {
        _state.update { it.copy(selectedFilter = filter) }
    }

    fun toggleSave(article: Article) {
        viewModelScope.launch {
            if (article.link in _state.value.savedLinks) {
                localRepo.unsaveArticle(article.link)
            } else {
                localRepo.saveArticle(article)
            }
        }
    }

    fun markAsRead(link: String) {
        viewModelScope.launch {
            localRepo.markAsRead(link)
        }
    }
}
