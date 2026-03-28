package nl.tijmen.articlereader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.tijmen.articlereader.data.Article
import nl.tijmen.articlereader.data.FeedSource
import nl.tijmen.articlereader.data.FeedUnavailableException
import nl.tijmen.articlereader.data.RssRepository

enum class SourceLoadState { LOADING, SUCCESS, ERROR, UNAVAILABLE }

data class SourceState(
    val loadState: SourceLoadState = SourceLoadState.LOADING,
    val error: String? = null
)

data class CombinedUiState(
    val articles: List<Article> = emptyList(),
    val sourceStates: Map<FeedSource, SourceState> = FeedSource.entries.associateWith { SourceState() },
    val activeFilters: Set<FeedSource> = FeedSource.entries.toSet()
) {
    val filteredArticles: List<Article>
        get() = articles
            .filter { it.source in activeFilters }
            .sortedByDescending { it.timestamp }

    val isAnyLoading: Boolean
        get() = sourceStates.values.any { it.loadState == SourceLoadState.LOADING }
}

class ArticleViewModel : ViewModel() {

    private val repository = RssRepository()
    private val articlesBySource = mutableMapOf<FeedSource, List<Article>>()

    private val _state = MutableStateFlow(CombinedUiState())
    val state: StateFlow<CombinedUiState> = _state.asStateFlow()

    init {
        FeedSource.entries.forEach { load(it) }
    }

    fun load(source: FeedSource) {
        updateSourceState(source, SourceState(SourceLoadState.LOADING))
        viewModelScope.launch {
            try {
                val articles = repository.fetchArticles(source)
                articlesBySource[source] = articles
                updateSourceState(source, SourceState(SourceLoadState.SUCCESS))
            } catch (e: FeedUnavailableException) {
                articlesBySource.remove(source)
                updateSourceState(source, SourceState(
                    loadState = SourceLoadState.UNAVAILABLE,
                    error = "Feed niet beschikbaar"
                ))
            } catch (e: Exception) {
                articlesBySource.remove(source)
                updateSourceState(source, SourceState(
                    loadState = SourceLoadState.ERROR,
                    error = e.message ?: "Onbekende fout"
                ))
            }
        }
    }

    fun toggleFilter(source: FeedSource) {
        _state.update { current ->
            val newFilters = if (source in current.activeFilters) {
                // Don't allow deselecting the last active filter
                if (current.activeFilters.size > 1) current.activeFilters - source
                else current.activeFilters
            } else {
                current.activeFilters + source
            }
            current.copy(activeFilters = newFilters)
        }
    }

    private fun updateSourceState(source: FeedSource, sourceState: SourceState) {
        _state.update { current ->
            current.copy(
                articles = articlesBySource.values.flatten(),
                sourceStates = current.sourceStates + (source to sourceState)
            )
        }
    }
}
