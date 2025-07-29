package com.crackmod.flowave.presentation.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crackmod.flowave.domain.model.Artist
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.domain.repository.MusicRepository
import com.crackmod.flowave.domain.util.ArtistSortBy
import com.crackmod.flowave.domain.util.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AllArtistsUiState(
    val artists: List<Artist> = emptyList(),
    val sortBy: ArtistSortBy = ArtistSortBy.NAME,
    val sortOrder: SortOrder = SortOrder.ASCENDING,
    // --- НОВОЕ ПОЛЕ ---
    val searchQuery: String = ""
)

@HiltViewModel
class AllArtistsViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _sortBy = MutableStateFlow(ArtistSortBy.NAME)
    private val _sortOrder = MutableStateFlow(SortOrder.ASCENDING)
    // --- НОВЫЕ StateFlow и функция ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    val uiState: StateFlow<AllArtistsUiState> = combine(
        musicRepository.getAllArtists(),
        _sortBy,
        _sortOrder,
        _searchQuery // --- Добавляем в combine ---
    ) { allArtists, sortBy, sortOrder, query ->
        val sortedArtists = when (sortBy) {
            ArtistSortBy.NAME -> if (sortOrder == SortOrder.ASCENDING) allArtists.sortedBy { it.name.lowercase() } else allArtists.sortedByDescending { it.name.lowercase() }
            ArtistSortBy.TRACK_COUNT -> if (sortOrder == SortOrder.ASCENDING) allArtists.sortedBy { it.trackCount } else allArtists.sortedByDescending { it.trackCount }
            ArtistSortBy.ALBUM_COUNT -> if (sortOrder == SortOrder.ASCENDING) allArtists.sortedBy { it.albumCount } else allArtists.sortedByDescending { it.albumCount }
        }

        // --- Логика фильтрации ---
        val filteredArtists = if (query.isBlank()) {
            sortedArtists
        } else {
            sortedArtists.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }

        AllArtistsUiState(
            artists = filteredArtists,
            sortBy = sortBy,
            sortOrder = sortOrder,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AllArtistsUiState()
    )

    fun onSortByChange(newSortBy: ArtistSortBy) {
        _sortBy.value = newSortBy
    }

    fun onSortOrderChange(newSortOrder: SortOrder) {
        _sortOrder.value = newSortOrder
    }

    fun getTracksForRandomArtist(): Flow<List<Track>> = flow {
        val allArtists = uiState.value.artists
        if (allArtists.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        val randomArtist = allArtists.random()
        musicRepository.getTracksByArtist(randomArtist.id).firstOrNull()?.let { tracks ->
            emit(tracks)
        } ?: emit(emptyList())
    }
}