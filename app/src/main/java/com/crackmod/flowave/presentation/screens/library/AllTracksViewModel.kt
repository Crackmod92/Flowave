package com.crackmod.flowave.presentation.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crackmod.flowave.domain.util.SortOrder
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.domain.util.TrackSortBy
import com.crackmod.flowave.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class AllTracksUiState(
    val tracks: List<Track> = emptyList(),
    val sortBy: TrackSortBy = TrackSortBy.TITLE,
    val sortOrder: SortOrder = SortOrder.ASCENDING,
    // --- НОВОЕ ПОЛЕ ---
    val searchQuery: String = ""
)

@HiltViewModel
class AllTracksViewModel @Inject constructor(
    musicRepository: MusicRepository
) : ViewModel() {

    private val _sortBy = MutableStateFlow(TrackSortBy.TITLE)
    private val _sortOrder = MutableStateFlow(SortOrder.ASCENDING)
    // --- НОВЫЕ StateFlow и функция ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    val uiState: StateFlow<AllTracksUiState> = combine(
        musicRepository.getAllTracks(),
        _sortBy,
        _sortOrder,
        _searchQuery // --- Добавляем в combine ---
    ) { allTracks, sortBy, sortOrder, query ->
        val sortedTracks = when (sortBy) {
            TrackSortBy.TITLE -> if (sortOrder == SortOrder.ASCENDING) allTracks.sortedBy { it.title.lowercase() } else allTracks.sortedByDescending { it.title.lowercase() }
            TrackSortBy.ARTIST -> if (sortOrder == SortOrder.ASCENDING) allTracks.sortedBy { it.artist.lowercase() } else allTracks.sortedByDescending { it.artist.lowercase() }
            TrackSortBy.DATE_ADDED -> if (sortOrder == SortOrder.ASCENDING) allTracks.sortedBy { it.dateAdded } else allTracks.sortedByDescending { it.dateAdded }
            TrackSortBy.DURATION -> if (sortOrder == SortOrder.ASCENDING) allTracks.sortedBy { it.duration } else allTracks.sortedByDescending { it.duration }
            TrackSortBy.YEAR -> if (sortOrder == SortOrder.ASCENDING) allTracks.sortedBy { it.year ?: 0 } else allTracks.sortedByDescending { it.year ?: 0 }
        }

        // --- Логика фильтрации ---
        val filteredTracks = if (query.isBlank()) {
            sortedTracks
        } else {
            sortedTracks.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true) ||
                        it.album.contains(query, ignoreCase = true)
            }
        }

        AllTracksUiState(
            tracks = filteredTracks,
            sortBy = sortBy,
            sortOrder = sortOrder,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AllTracksUiState()
    )

    fun onSortByChange(newSortBy: TrackSortBy) {
        _sortBy.value = newSortBy
    }

    fun onSortOrderChange(newSortOrder: SortOrder) {
        _sortOrder.value = newSortOrder
    }
}