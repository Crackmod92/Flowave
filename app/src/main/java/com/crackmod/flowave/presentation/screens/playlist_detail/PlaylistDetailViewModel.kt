package com.crackmod.flowave.presentation.screens.playlist_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crackmod.flowave.domain.model.Playlist
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.domain.repository.MusicRepository
import com.crackmod.flowave.domain.util.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(PlaylistDetailState())
    val state: StateFlow<PlaylistDetailState> = _state.asStateFlow()

    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation = _showDeleteConfirmation.asStateFlow()

    private val playlistId: String = savedStateHandle.get<String>("playlistId") ?: ""

    init {
        loadPlaylistDetails()
    }

    private fun loadPlaylistDetails() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val tracksFlow: Flow<List<Track>> = when (playlistId) {
                AppConstants.FAVORITES_PLAYLIST_ID -> musicRepository.getFavoriteTracks()
                AppConstants.RECENTLY_ADDED_PLAYLIST_ID -> musicRepository.getRecentlyAddedTracks()
                AppConstants.MOST_PLAYED_PLAYLIST_ID -> musicRepository.getMostPlayedTracks()
                else -> musicRepository.getPlaylistTracks(playlistId)
            }

            // --- ИЗМЕНЕНИЕ: Упрощаем логику получения плейлиста ---
            val playlistFlow: Flow<Playlist?> = musicRepository.getPlaylistById(playlistId)

            combine(playlistFlow, tracksFlow) { playlist, tracks ->
                val finalPlaylist = when (playlistId) {
                    AppConstants.FAVORITES_PLAYLIST_ID ->
                        Playlist(AppConstants.FAVORITES_PLAYLIST_ID, "Избранное", null, 0, 0, tracks.size, true)
                    AppConstants.RECENTLY_ADDED_PLAYLIST_ID ->
                        Playlist(AppConstants.RECENTLY_ADDED_PLAYLIST_ID, "Недавно добавленные", null, 0, 0, tracks.size, true)
                    AppConstants.MOST_PLAYED_PLAYLIST_ID ->
                        Playlist(AppConstants.MOST_PLAYED_PLAYLIST_ID, "Часто прослушиваемые", null, 0, 0, tracks.size, true)
                    else -> playlist?.copy(trackCount = tracks.size)
                }

                PlaylistDetailState(
                    playlist = finalPlaylist,
                    tracks = tracks,
                    isLoading = false,
                    error = if (finalPlaylist == null) "Плейлист не найден" else null
                )
            }.catch { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun onShowDeleteConfirmation() {
        _showDeleteConfirmation.value = true
    }

    fun onDismissDeleteConfirmation() {
        _showDeleteConfirmation.value = false
    }

    fun deletePlaylist() {
        if (state.value.playlist?.isSystem == false) {
            viewModelScope.launch {
                musicRepository.deletePlaylist(playlistId)
            }
        }
    }

    // --- НОВАЯ ФУНКЦИЯ ---
    fun removeTrackFromPlaylist(trackId: Long) {
        if (state.value.playlist?.isSystem == false) {
            viewModelScope.launch {
                musicRepository.removeTrackFromPlaylist(playlistId, trackId)
            }
        }
    }
}

data class PlaylistDetailState(
    val playlist: Playlist? = null,
    val tracks: List<Track> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)