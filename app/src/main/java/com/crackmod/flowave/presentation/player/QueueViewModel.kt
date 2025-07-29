// ПУТЬ: com/crackmod/flowave/presentation/player/QueueViewModel.txt

package com.crackmod.flowave.presentation.player

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.concurrent.futures.await
import com.crackmod.flowave.data.service.MusicService
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.domain.repository.MusicRepository
import com.crackmod.flowave.domain.util.toMediaItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class QueueViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QueueUiState())
    val uiState: StateFlow<QueueUiState> = _uiState.asStateFlow()

    private var mediaController: MediaController? = null

    init {
        initializeMediaController()
    }

    private fun initializeMediaController() {
        viewModelScope.launch {
            try {
                val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
                mediaController = MediaController.Builder(context, sessionToken).buildAsync().await().apply {
                    addListener(PlayerListener())
                    updateQueue()
                }
            } catch (e: Exception) {
                // Обработка ошибки
            }
        }
    }

    // ### ИЗМЕНЕНИЕ: Улучшена логика получения треков для сохранения порядка ###
    private suspend fun updateQueue() {
        val controller = mediaController ?: return
        val mediaItemCount = controller.mediaItemCount
        val currentIndex = controller.currentMediaItemIndex

        if (mediaItemCount == 0 || currentIndex == C.INDEX_UNSET) {
            _uiState.update { it.copy(nowPlaying = null, nextUp = emptyList()) }
            return
        }

        val mediaItems = List(mediaItemCount) { i -> controller.getMediaItemAt(i) }
        val mediaIds = mediaItems.mapNotNull { it.mediaId.toLongOrNull() }

        val tracksFromDb = withContext(Dispatchers.IO) {
            musicRepository.getTracksByIds(mediaIds)
        }

        // Гарантируем правильный порядок, который сейчас в плеере
        val tracksMap = tracksFromDb.associateBy { it.id }
        val orderedQueue = mediaIds.mapNotNull { id -> tracksMap[id] }

        if (orderedQueue.isNotEmpty() && currentIndex < orderedQueue.size) {
            val nowPlayingTrack = orderedQueue[currentIndex]
            val nextUpTracks = orderedQueue.subList(currentIndex + 1, orderedQueue.size)

            _uiState.update {
                it.copy(
                    nowPlaying = nowPlayingTrack,
                    nextUp = nextUpTracks
                )
            }
        } else {
            _uiState.update { it.copy(nowPlaying = null, nextUp = emptyList()) }
        }
    }

    fun addToQueue(track: Track) {
        if (!track.isAvailable) return
        mediaController?.addMediaItem(track.toMediaItem())
    }

    fun playTrackNext(track: Track) {
        if (!track.isAvailable) return
        mediaController?.let { controller ->
            val currentIndex = controller.currentMediaItemIndex
            if (currentIndex == C.INDEX_UNSET || currentIndex >= controller.mediaItemCount) {
                controller.addMediaItem(track.toMediaItem())
            } else {
                controller.addMediaItem(currentIndex + 1, track.toMediaItem())
            }
        }
    }

    fun moveTrackToPlayNext(relativeIndex: Int) {
        mediaController?.let { controller ->
            val currentIndex = controller.currentMediaItemIndex
            if (currentIndex == C.INDEX_UNSET) return

            val fromAbsolute = currentIndex + 1 + relativeIndex
            val toAbsolute = currentIndex + 1

            if (fromAbsolute in 0..<controller.mediaItemCount && fromAbsolute != toAbsolute) {
                controller.moveMediaItem(fromAbsolute, toAbsolute)
            }
        }
    }


    fun playTrackFromNextUp(relativeIndex: Int) {
        val currentIndex = mediaController?.currentMediaItemIndex ?: C.INDEX_UNSET
        if (currentIndex == C.INDEX_UNSET) return

        val absoluteIndex = currentIndex + 1 + relativeIndex
        mediaController?.let { controller ->
            if (absoluteIndex < controller.mediaItemCount && absoluteIndex != controller.currentMediaItemIndex) {
                controller.seekTo(absoluteIndex, 0)
                if (!controller.isPlaying) controller.play()
            }
        }
    }

    fun moveTrackInQueue(fromRelative: Int, toRelative: Int) {
        val currentIndex = mediaController?.currentMediaItemIndex ?: C.INDEX_UNSET
        if (currentIndex == C.INDEX_UNSET) return

        val fromAbsolute = currentIndex + 1 + fromRelative
        val toAbsolute = currentIndex + 1 + toRelative

        mediaController?.moveMediaItem(fromAbsolute, toAbsolute)
    }

    fun removeTrackFromNextUp(relativeIndex: Int) {
        val currentIndex = mediaController?.currentMediaItemIndex ?: C.INDEX_UNSET
        if (currentIndex == C.INDEX_UNSET) return

        val absoluteIndex = currentIndex + 1 + relativeIndex
        mediaController?.removeMediaItem(absoluteIndex)
    }

    override fun onCleared() {
        mediaController?.release()
        super.onCleared()
    }

    private inner class PlayerListener : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            // ### ИЗМЕНЕНИЕ: Это событие теперь надежно ###
            if (events.contains(Player.EVENT_TIMELINE_CHANGED) ||
                events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                viewModelScope.launch {
                    updateQueue()
                }
            }
        }
    }
}

data class QueueUiState(
    val nowPlaying: Track? = null,
    val nextUp: List<Track> = emptyList()
)