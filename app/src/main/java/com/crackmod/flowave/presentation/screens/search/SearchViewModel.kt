package com.crackmod.flowave.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crackmod.flowave.domain.model.Album
import com.crackmod.flowave.domain.model.Artist
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// НОВЫЙ SearchUiState для более сложной логики
data class SearchUiState(
    val topResult: Any? = null, // Может быть Track, Album или Artist
    val tracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val recentSearches: List<String> = emptyList(), // Для будущей реализации
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val hasResults: Boolean
        get() = topResult != null || tracks.isNotEmpty() || albums.isNotEmpty() || artists.isNotEmpty()
}


@HiltViewModel
class SearchViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeSearchQuery()
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(350) // Небольшая задержка для комфортного ввода
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        // Показываем последние запросы (пока пустой список)
                        flowOf(SearchUiState(isLoading = false, recentSearches = emptyList()))
                    } else {
                        _uiState.update { it.copy(isLoading = true) }
                        performSearch(query)
                    }
                }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    private fun performSearch(query: String): Flow<SearchUiState> {
        val tracksFlow = musicRepository.searchTracks(query)
        val albumsFlow = musicRepository.searchAlbums(query)
        val artistsFlow = musicRepository.searchArtists(query)

        return combine(tracksFlow, albumsFlow, artistsFlow) { tracks, albums, artists ->
            // Логика для определения "Лучшего результата"
            val topResult = findTopResult(query, tracks, albums, artists)

            // Убираем лучший результат из основного списка, чтобы избежать дублирования
            val filteredTracks = if (topResult is Track) tracks.filter { it.id != topResult.id } else tracks
            val filteredAlbums = if (topResult is Album) albums.filter { it.id != topResult.id } else albums
            val filteredArtists = if (topResult is Artist) artists.filter { it.id != topResult.id } else artists

            SearchUiState(
                topResult = topResult,
                tracks = filteredTracks,
                albums = filteredAlbums,
                artists = filteredArtists,
                isLoading = false
            )
        }
    }

    // Простая эвристика для определения лучшего результата
    private fun findTopResult(query: String, tracks: List<Track>, albums: List<Album>, artists: List<Artist>): Any? {
        // Приоритет исполнителю, если есть точное совпадение
        artists.firstOrNull { it.name.equals(query, ignoreCase = true) }?.let { return it }
        // Затем альбому
        albums.firstOrNull { it.title.equals(query, ignoreCase = true) }?.let { return it }
        // Затем треку
        tracks.firstOrNull { it.title.equals(query, ignoreCase = true) }?.let { return it }

        // Если точных совпадений нет, берем первый попавшийся результат из самого релевантного списка
        return artists.firstOrNull() ?: albums.firstOrNull() ?: tracks.firstOrNull()
    }


    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}