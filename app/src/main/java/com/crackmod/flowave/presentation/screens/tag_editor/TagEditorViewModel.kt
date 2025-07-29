package com.crackmod.flowave.presentation.screens.tag_editor

import android.app.PendingIntent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crackmod.flowave.domain.exceptions.RecoverablePermissionException
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagEditorViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(TagEditorState())
    val state: StateFlow<TagEditorState> = _state.asStateFlow()

    private var currentTrackId: Long? = null

    fun loadTrack(trackId: Long) {
        // <- ГЛАВНОЕ ИЗМЕНЕНИЕ: Мы полностью удалили проверку `if (trackId == currentTrackId)`.
        // Теперь состояние будет сбрасываться КАЖДЫЙ РАЗ при вызове этого метода.
        currentTrackId = trackId

        viewModelScope.launch {
            _state.value = TagEditorState(isLoading = true)
            musicRepository.getTrackById(trackId)
                .catch { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
                .collect { track ->
                    _state.update {
                        it.copy(
                            track = track,
                            isLoading = false,
                            error = if (track == null) "Трек не найден" else null
                        )
                    }
                }
        }
    }

    fun saveTags(
        title: String,
        artist: String,
        album: String,
        albumArtist: String,
        year: Int?,
        genre: String?,
        trackNumber: Int?
    ) {
        val trackId = currentTrackId ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, saveSuccess = false) }
            try {
                musicRepository.updateTrackMetadata(
                    trackId = trackId,
                    newTitle = title.trim(),
                    newArtist = artist.trim(),
                    newAlbum = album.trim(),
                    newAlbumArtist = albumArtist.trim().ifBlank { artist.trim() },
                    newYear = year,
                    newGenre = genre?.trim()?.takeIf { it.isNotEmpty() },
                    newTrackNumber = trackNumber
                )
                _state.update { it.copy(isLoading = false, saveSuccess = true) }
            } catch (e: RecoverablePermissionException) {
                _state.update { it.copy(isLoading = false, permissionRequest = e.intent) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Неизвестная ошибка", saveSuccess = false) }
            }
        }
    }

    fun permissionRequestHandled() {
        _state.update { it.copy(permissionRequest = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class TagEditorState(
    val track: Track? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val permissionRequest: PendingIntent? = null
)