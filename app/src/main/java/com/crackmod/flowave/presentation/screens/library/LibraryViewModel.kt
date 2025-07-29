// ПУТЬ: com/crackmod/flowave/presentation/screens/library/LibraryViewModel.kt

package com.crackmod.flowave.presentation.screens.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crackmod.flowave.domain.model.*
import com.crackmod.flowave.domain.repository.MusicRepository
import com.crackmod.flowave.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BannerState(
    val isVisible: Boolean = false,
    val message: String = "",
    val isError: Boolean = false
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    val settingsRepository: SettingsRepository
) : ViewModel() {

    // --- ИЗМЕНЕНИЕ 1: Убираем сложный приватный _state и создаем простой флаг ---
    private val _isScanning = MutableStateFlow(false)

    // --- ИЗМЕНЕНИЕ 2: Добавляем _isScanning в combine, чтобы он стал триггером ---
    val state: StateFlow<LibraryUiState> = combine(
        musicRepository.getAllTracks(),
        musicRepository.getAllAlbums(),
        musicRepository.getAllArtists(),
        musicRepository.getAllPlaylists(),
        _isScanning // <--- Вот он!
    ) { tracks, albums, artists, playlists, isScanning ->
        LibraryUiState(
            isLoading = false, // Загрузка завершена, как только первый combine отработает
            isScanning = isScanning,
            tracks = tracks,
            albums = albums,
            artists = artists,
            playlists = playlists
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState(isLoading = true) // Начальное состояние по-прежнему "загрузка"
    )

    private val _bannerState = MutableStateFlow(BannerState())
    val bannerState: StateFlow<BannerState> = _bannerState.asStateFlow()

    private val _showCreatePlaylistDialog = MutableStateFlow(false)
    val showCreatePlaylistDialog = _showCreatePlaylistDialog.asStateFlow()

    private val _trackToAdd = MutableStateFlow<Track?>(null)
    val trackToAdd = _trackToAdd.asStateFlow()

    private val _selectedTrackForOptions = MutableStateFlow<Track?>(null)
    val selectedTrackForOptions = _selectedTrackForOptions.asStateFlow()

    private val _trackToEdit = MutableStateFlow<Track?>(null)
    val trackToEdit = _trackToEdit.asStateFlow()

    private val _trackToDelete = MutableStateFlow<Track?>(null)
    val trackToDelete = _trackToDelete.asStateFlow()

    val mostPlayedTracks: StateFlow<List<Track>> = musicRepository.getMostPlayedTracks(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyAddedTracks: StateFlow<List<Track>> = musicRepository.getRecentlyAddedTracks(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyAddedAlbums: StateFlow<List<Album>> = musicRepository.getRecentlyAddedAlbums(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteCount: StateFlow<Int> = musicRepository.getFavoriteTrackCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)


    fun showBanner(message: String, isError: Boolean = false) {
        viewModelScope.launch {
            _bannerState.value = BannerState(isVisible = true, message = message, isError = isError)
            delay(3000L)
            _bannerState.value = BannerState(isVisible = false)
        }
    }

    fun scanLibrary(onResult: (Result<Int>) -> Unit) {
        viewModelScope.launch {
            // --- ИЗМЕНЕНИЕ 3: Теперь мы управляем простым значением ---
            _isScanning.value = true

            val result = try {
                val foldersToScan = settingsRepository.scanFolders.first()
                musicRepository.scanMediaLibrary(foldersToScan)
            } catch (e: Exception) {
                Result.failure(e)
            }

            if (result.isSuccess) {
                settingsRepository.setInitialScanCompleted(true)
            }

            // Данные обновятся автоматически через combine
            _isScanning.value = false

            onResult(result)
        }
    }

    fun onDeleteTrackRequest(track: Track) {
        onDismissTrackOptions()
        _trackToDelete.value = track
    }

    fun onDismissDeleteTrackConfirmation() {
        _trackToDelete.value = null
    }

    fun confirmDeleteTrack() {
        viewModelScope.launch {
            _trackToDelete.value?.let { track ->
                try {
                    musicRepository.deleteTrack(track.id)
                } catch (e: Exception) {
                    // Ошибки теперь можно отображать через баннер
                    showBanner("Ошибка удаления: ${e.message}", isError = true)
                } finally {
                    onDismissDeleteTrackConfirmation()
                }
            }
        }
    }

    fun onEditTagsRequest(track: Track) {
        _trackToEdit.value = track
    }

    fun onEditTagsDismiss() {
        _trackToEdit.value = null
    }

    fun onTrackOptionsClick(track: Track) {
        _selectedTrackForOptions.value = track
    }

    fun onDismissTrackOptions() {
        _selectedTrackForOptions.value = null
    }

    fun onAddTrackToPlaylistRequest(track: Track) {
        _selectedTrackForOptions.value = null
        _trackToAdd.value = track
    }

    fun onDismissPlaylistSelection() {
        _trackToAdd.value = null
    }

    fun addTrackToPlaylist(playlistId: String) {
        viewModelScope.launch {
            _trackToAdd.value?.let { track ->
                musicRepository.addTrackToPlaylist(playlistId, track.id)
            }
            onDismissPlaylistSelection()
        }
    }

    fun onShowCreatePlaylistDialog() {
        _showCreatePlaylistDialog.value = true
    }

    fun onDismissCreatePlaylistDialog() {
        _showCreatePlaylistDialog.value = false
    }

    fun createPlaylist(name: String, description: String? = null) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                musicRepository.createPlaylist(name, description)
                _showCreatePlaylistDialog.value = false
            }
        }
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            musicRepository.updateFavoriteStatus(track.id, !track.isFavorite)
        }
    }
}

data class LibraryUiState(
    val isLoading: Boolean = false,
    val isScanning: Boolean = false,
    val tracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val error: String? = null
)