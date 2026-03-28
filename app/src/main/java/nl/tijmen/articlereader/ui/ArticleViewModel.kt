package nl.tijmen.articlereader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import nl.tijmen.articlereader.data.Article
import nl.tijmen.articlereader.data.FeedSource
import nl.tijmen.articlereader.data.FeedUnavailableException
import nl.tijmen.articlereader.data.RssRepository

sealed class FeedUiState {
    object Loading : FeedUiState()
    data class Success(val articles: List<Article>) : FeedUiState()
    data class Error(val message: String, val isFeedUnavailable: Boolean = false) : FeedUiState()
}

class ArticleViewModel : ViewModel() {

    private val repository = RssRepository()

    private val _states = mapOf(
        FeedSource.STUK_ROOD_VLEES to MutableStateFlow<FeedUiState>(FeedUiState.Loading),
        FeedSource.ESB to MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    )

    fun stateFor(source: FeedSource): StateFlow<FeedUiState> =
        _states[source] ?: error("Unknown source $source")

    init {
        FeedSource.entries.forEach { load(it) }
    }

    fun load(source: FeedSource) {
        _states[source]!!.value = FeedUiState.Loading
        viewModelScope.launch {
            try {
                val articles = repository.fetchArticles(source)
                _states[source]!!.value = if (articles.isEmpty())
                    FeedUiState.Error("Geen artikelen gevonden")
                else
                    FeedUiState.Success(articles)
            } catch (e: FeedUnavailableException) {
                _states[source]!!.value = FeedUiState.Error(
                    message = "Feed niet beschikbaar \u2014 open de site in je browser",
                    isFeedUnavailable = true
                )
            } catch (e: Exception) {
                _states[source]!!.value = FeedUiState.Error(
                    message = e.message ?: "Onbekende fout"
                )
            }
        }
    }
}
