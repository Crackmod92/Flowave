package com.crackmod.flowave.presentation.screens.artist_detail

import androidx.lifecycle.SavedStateHandle
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

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(ArtistDetailState())
    val state: StateFlow<ArtistDetailState> = _state.asStateFlow()

    // <- ФИКС: Получаем ID как Long из SavedStateHandle.
    private val artistId: Long? = savedStateHandle.get<Long>("artistId")

    init {
        if (artistId != null) {
            loadArtistDetails(artistId)
        } else {
            _state.value = ArtistDetailState(error = "Artist ID not found", isLoading = false)
        }
    }

    private fun loadArtistDetails(artistId: Long) { // <- ФИКС: Принимаем Long
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val artistFlow = musicRepository.getArtistById(artistId)
            val tracksFlow = musicRepository.getTracksByArtist(artistId)

            artistFlow.flatMapLatest { artist ->
                if (artist == null) {
                    flowOf(ArtistDetailState(isLoading = false, error = "Артист не найден"))
                } else {
                    val albumsFlow = musicRepository.getAlbumsByArtist(artist.name)
                    combine(tracksFlow, albumsFlow) { tracks, albums ->
                        ArtistDetailState(
                            artist = artist,
                            tracks = tracks,
                            albums = albums,
                            isLoading = false
                        )
                    }
                }
            }.catch { e ->
                emit(ArtistDetailState(isLoading = false, error = e.message ?: "Неизвестная ошибка"))
            }.collect { newState ->
                _state.value = newState
            }
        }
    }
}

data class ArtistDetailState(
    val artist: Artist? = null,
    val tracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)