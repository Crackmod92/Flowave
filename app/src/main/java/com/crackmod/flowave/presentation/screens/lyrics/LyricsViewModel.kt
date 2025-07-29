package com.crackmod.flowave.presentation.screens.lyrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crackmod.flowave.domain.model.Lyrics
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LyricsUiState {
    object Loading : LyricsUiState()
    data class Success(val track: Track, val lyrics: Lyrics) : LyricsUiState()
    data class NotFound(val track: Track) : LyricsUiState()
    data class Error(val message: String) : LyricsUiState()
}

@HiltViewModel
class LyricsViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LyricsUiState>(LyricsUiState.Loading)
    val uiState: StateFlow<LyricsUiState> = _uiState.asStateFlow()

    private var currentTrackId: Long? = null

    fun fetchLyrics(trackId: Long) {
        // Предотвращаем повторную загрузку, если BottomSheet просто скрыли и снова показали для того же трека
        if (trackId == currentTrackId && _uiState.value !is LyricsUiState.Error) {
            return
        }
        currentTrackId = trackId

        viewModelScope.launch {
            _uiState.value = LyricsUiState.Loading
            val track = musicRepository.getTrackById(trackId).firstOrNull()

            if (track == null) {
                _uiState.value = LyricsUiState.Error("Трек не найден в медиатеке.")
                return@launch
            }

            musicRepository.getLyricsForTrack(track).collect { result ->
                result.onSuccess { lyrics ->
                    _uiState.value = LyricsUiState.Success(track, lyrics)
                }.onFailure { exception ->
                    if (exception.message == "Текст не найден") {
                        _uiState.value = LyricsUiState.NotFound(track)
                    } else {
                        _uiState.value = LyricsUiState.Error(exception.message ?: "Неизвестная ошибка")
                    }
                }
            }
        }
    }
}