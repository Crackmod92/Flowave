package com.crackmod.flowave.presentation.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crackmod.flowave.domain.model.Album
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.domain.repository.MusicRepository
import com.crackmod.flowave.domain.repository.SettingsRepository
import com.crackmod.flowave.domain.util.AlbumSortBy
import com.crackmod.flowave.domain.util.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AllAlbumsUiState(
    val albums: List<Album> = emptyList(),
    val sortBy: AlbumSortBy = AlbumSortBy.TITLE,
    val sortOrder: SortOrder = SortOrder.ASCENDING,
    val isGridView: Boolean = true,
    // --- НОВОЕ ПОЛЕ ---
    val searchQuery: String = ""
)

@HiltViewModel
class AllAlbumsViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _sortBy = MutableStateFlow(AlbumSortBy.TITLE)
    private val _sortOrder = MutableStateFlow(SortOrder.ASCENDING)
    // --- НОВЫЕ StateFlow и функция ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    val uiState: StateFlow<AllAlbumsUiState> = combine(
        musicRepository.getAllAlbums(),
        _sortBy,
        _sortOrder,
        settingsRepository.albumListViewTypeIsGrid,
        _searchQuery // --- Добавляем в combine ---
    ) { allAlbums, sortBy, sortOrder, isGrid, query ->
        val sortedAlbums = when (sortBy) {
            AlbumSortBy.TITLE -> if (sortOrder == SortOrder.ASCENDING) allAlbums.sortedBy { it.title.lowercase() } else allAlbums.sortedByDescending { it.title.lowercase() }
            AlbumSortBy.ARTIST -> if (sortOrder == SortOrder.ASCENDING) allAlbums.sortedBy { it.artist.lowercase() } else allAlbums.sortedByDescending { it.artist.lowercase() }
            AlbumSortBy.YEAR -> if (sortOrder == SortOrder.ASCENDING) allAlbums.sortedBy { it.year ?: 0 } else allAlbums.sortedByDescending { it.year ?: 0 }
            AlbumSortBy.DATE_ADDED -> if (sortOrder == SortOrder.ASCENDING) allAlbums.sortedBy { it.dateAdded } else allAlbums.sortedByDescending { it.dateAdded }
        }

        // --- Логика фильтрации ---
        val filteredAlbums = if (query.isBlank()) {
            sortedAlbums
        } else {
            sortedAlbums.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true)
            }
        }

        AllAlbumsUiState(
            albums = filteredAlbums,
            sortBy = sortBy,
            sortOrder = sortOrder,
            isGridView = isGrid,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AllAlbumsUiState()
    )

    fun onSortByChange(newSortBy: AlbumSortBy) {
        _sortBy.value = newSortBy
    }

    fun onSortOrderChange(newSortOrder: SortOrder) {
        _sortOrder.value = newSortOrder
    }

    fun onLayoutChange(isGridView: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAlbumListViewType(isGridView)
        }
    }

    fun getTracksForRandomAlbum(): Flow<List<Track>> = flow {
        val allAlbums = uiState.value.albums
        if (allAlbums.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        val randomAlbum = allAlbums.random()
        musicRepository.getTracksByAlbum(randomAlbum.id).firstOrNull()?.let { tracks ->
            emit(tracks)
        } ?: emit(emptyList())
    }
}