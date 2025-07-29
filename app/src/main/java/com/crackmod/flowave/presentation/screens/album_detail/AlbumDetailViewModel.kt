package com.crackmod.flowave.presentation.screens.album_detail

import android.content.ContentUris
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Scale
import com.crackmod.flowave.domain.model.Album
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(AlbumDetailState())
    val state: StateFlow<AlbumDetailState> = _state.asStateFlow()

    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation = _showDeleteConfirmation.asStateFlow()

    private val albumId: Long = savedStateHandle.get<Long>("albumId")!!

    init {
        loadAlbumDetails()
    }

    private fun loadAlbumDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            musicRepository.getAlbumById(albumId).flatMapLatest { album ->
                if (album == null) {
                    flowOf(AlbumDetailState(isLoading = false, error = "Альбом не найден"))
                } else {
                    val tracksFlow = musicRepository.getTracksByAlbum(albumId)
                    // --- ИЗМЕНЕНИЕ: Запрашиваем другие альбомы этого исполнителя ---
                    val otherAlbumsFlow = musicRepository.getAlbumsByArtist(album.artist)
                        .map { allAlbums ->
                            // Убираем текущий альбом из списка рекомендаций
                            allAlbums.filter { it.id != album.id }
                        }

                    // Генерируем палитру для фона
                    val albumArtUri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, album.id)
                    generatePalette(albumArtUri)

                    combine(tracksFlow, otherAlbumsFlow) { tracks, otherAlbums ->
                        AlbumDetailState(
                            album = album,
                            tracks = tracks,
                            otherAlbumsByArtist = otherAlbums, // <-- Добавляем в стейт
                            isLoading = false
                        )
                    }
                }
            }.catch { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    private fun generatePalette(albumArtUri: Uri) {
        viewModelScope.launch {
            try {
                val request = ImageRequest.Builder(context)
                    .data(albumArtUri)
                    .scale(Scale.FILL)
                    .allowHardware(false)
                    .build()

                val imageResult = context.imageLoader.execute(request)
                val bitmap = (imageResult.drawable as? BitmapDrawable)?.bitmap ?: return@launch

                val palette = Palette.from(bitmap).generate()
                val dominantColor = palette.getDominantColor(0)
                if (dominantColor != 0) {
                    _state.update { it.copy(dominantColor = Color(dominantColor)) }
                }
            } catch (e: Exception) {
                // Ошибка генерации палитры, ничего страшного
            }
        }
    }

    // Остальные функции без изменений
    fun onDeleteRequest() { _showDeleteConfirmation.value = true }
    fun onDismissDeleteConfirmation() { _showDeleteConfirmation.value = false }
    fun deleteAlbum() {
        viewModelScope.launch {
            try {
                musicRepository.deleteAlbum(albumId)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }
}

data class AlbumDetailState(
    val album: Album? = null,
    val tracks: List<Track> = emptyList(),
    // --- ИЗМЕНЕНИЕ: Новое поле для рекомендаций ---
    val otherAlbumsByArtist: List<Album> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val dominantColor: Color? = null
) {
    val totalDurationFormatted: String
        get() {
            val totalMillis = tracks.sumOf { it.duration }
            if (totalMillis <= 0) return ""
            val hours = TimeUnit.MILLISECONDS.toHours(totalMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(totalMillis) % 60
            return if (hours > 0) {
                "${hours} ч ${minutes} мин"
            } else {
                "${minutes} мин"
            }
        }
}