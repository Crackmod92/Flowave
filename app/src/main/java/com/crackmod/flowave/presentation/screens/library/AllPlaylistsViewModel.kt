// ПУТЬ: com/crackmod/flowave/presentation/screens/library/AllPlaylistsViewModel.txt

package com.crackmod.flowave.presentation.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crackmod.flowave.domain.model.Playlist
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.domain.repository.MusicRepository
import com.crackmod.flowave.domain.util.AppConstants
import com.crackmod.flowave.domain.util.PlaylistSortBy
import com.crackmod.flowave.domain.util.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AllPlaylistsUiState(
    val systemPlaylists: List<Playlist> = emptyList(),
    val userPlaylists: List<Playlist> = emptyList(),
    val sortBy: PlaylistSortBy = PlaylistSortBy.NAME,
    val sortOrder: SortOrder = SortOrder.ASCENDING,
    val isLoading: Boolean = true
)

@HiltViewModel
class AllPlaylistsViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _sortBy = MutableStateFlow(PlaylistSortBy.NAME)
    private val _sortOrder = MutableStateFlow(SortOrder.ASCENDING)

    private val _combinedData = combine(
        musicRepository.getAllPlaylists(),
        musicRepository.getFavoriteTrackCount(),
        musicRepository.getRecentlyAddedTracks().map { it.size },
        musicRepository.getMostPlayedTracks().map { it.size }
    ) { playlists, favCount, recentCount, mostPlayedCount ->
        Triple(playlists, favCount, Pair(recentCount, mostPlayedCount))
    }

    val uiState: StateFlow<AllPlaylistsUiState> = combine(
        _combinedData,
        _sortBy,
        _sortOrder
    ) { data, sortBy, sortOrder ->
        val (allPlaylists, favCount, counts) = data
        val (recentCount, mostPlayedCount) = counts

        val systemPlaylists = listOf(
            Playlist(AppConstants.FAVORITES_PLAYLIST_ID, "Избранное", null, 0, 0, favCount, true),
            Playlist(AppConstants.RECENTLY_ADDED_PLAYLIST_ID, "Недавно добавленные", null, 0, 0, recentCount, true),
            Playlist(AppConstants.MOST_PLAYED_PLAYLIST_ID, "Часто прослушиваемые", null, 0, 0, mostPlayedCount, true)
        )

        val userPlaylists = allPlaylists.filter { !it.isSystem }
        val sortedUserPlaylists = when (sortBy) {
            PlaylistSortBy.NAME -> if (sortOrder == SortOrder.ASCENDING) userPlaylists.sortedBy { it.name.lowercase() } else userPlaylists.sortedByDescending { it.name.lowercase() }
            PlaylistSortBy.DATE_CREATED -> if (sortOrder == SortOrder.ASCENDING) userPlaylists.sortedBy { it.dateCreated } else userPlaylists.sortedByDescending { it.dateCreated }
            PlaylistSortBy.TRACK_COUNT -> if (sortOrder == SortOrder.ASCENDING) userPlaylists.sortedBy { it.trackCount } else userPlaylists.sortedByDescending { it.trackCount }
        }

        AllPlaylistsUiState(
            systemPlaylists = systemPlaylists,
            userPlaylists = sortedUserPlaylists,
            sortBy = sortBy,
            sortOrder = sortOrder,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AllPlaylistsUiState()
    )

    fun onSortByChange(newSortBy: PlaylistSortBy) {
        _sortBy.value = newSortBy
    }

    fun onSortOrderChange(newSortOrder: SortOrder) {
        _sortOrder.value = newSortOrder
    }

    // --- ИЗМЕНЕНИЕ: Функция теперь возвращает Flow с треками случайного плейлиста ---
    fun getTracksForRandomPlaylist(): Flow<List<Track>> = flow {
        // --- ИЗМЕНЕНИЕ: Выбираем случайный плейлист из ВСЕХ (включая системные), где есть треки ---
        val allPlaylists = (uiState.value.systemPlaylists + uiState.value.userPlaylists)
            .filter { it.trackCount > 0 }

        if (allPlaylists.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        val randomPlaylist = allPlaylists.random()

        val tracksFlow: Flow<List<Track>> = when (randomPlaylist.id) {
            AppConstants.FAVORITES_PLAYLIST_ID -> musicRepository.getFavoriteTracks()
            AppConstants.RECENTLY_ADDED_PLAYLIST_ID -> musicRepository.getRecentlyAddedTracks()
            AppConstants.MOST_PLAYED_PLAYLIST_ID -> musicRepository.getMostPlayedTracks()
            else -> musicRepository.getPlaylistTracks(randomPlaylist.id)
        }

        tracksFlow.firstOrNull()?.let { tracks ->
            emit(tracks)
        } ?: emit(emptyList())
    }
}