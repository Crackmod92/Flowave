package com.crackmod.flowave.presentation.player

import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.concurrent.futures.await
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Scale
import com.crackmod.flowave.data.service.MusicService
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.domain.repository.MusicRepository
import com.crackmod.flowave.domain.repository.SettingsRepository
import com.crackmod.flowave.domain.util.toMediaItem
import com.crackmod.flowave.domain.util.toTrack
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicRepository: MusicRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var mediaController: MediaController? = null
    private var positionUpdaterJob: Job? = null
    private val stateUpdateMutex = Mutex()

    private val _originalPlaybackList = MutableStateFlow<List<Track>>(emptyList())

    init {
        initializeMediaController()
        observeSettings()
        observeTrackChangesForColorUpdate()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.enableVolumeNormalization.collect { isEnabled ->
                mediaController?.let {
                    val newAttrs = AudioAttributes.Builder()
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .setUsage(C.USAGE_MEDIA)
                        .build()
                    it.setAudioAttributes(newAttrs, isEnabled)
                }
            }
        }
    }

    private fun initializeMediaController() {
        viewModelScope.launch {
            try {
                val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
                mediaController = MediaController.Builder(context, sessionToken).buildAsync().await().apply {
                    addListener(PlayerListener())
                }
                restoreLastSession()
            } catch (e: Exception) {
                _uiState.update { it.copy(playerError = "Failed to connect to media service: ${e.message}", isRestoring = false) }
            }
        }
    }

    private fun restoreLastSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoring = true) }
            val lastQueueIds = settingsRepository.lastQueue.first()
            if (lastQueueIds.isEmpty()) {
                _uiState.update { it.copy(isRestoring = false) }; return@launch
            }
            val originalQueueIds = settingsRepository.originalQueue.first()
            _originalPlaybackList.value = if (originalQueueIds.isNotEmpty()) musicRepository.getTracksByIds(originalQueueIds) else musicRepository.getTracksByIds(lastQueueIds)
            val lastQueue = musicRepository.getTracksByIds(lastQueueIds)
            if (lastQueue.isEmpty()) {
                _uiState.update { it.copy(isRestoring = false) }; return@launch
            }
            val lastTrackId = settingsRepository.lastTrackIndex.first().toLong()
            val lastPosition = settingsRepository.lastTrackPosition.first()
            val shuffleEnabled = settingsRepository.lastShuffleMode.first()
            val repeatMode = settingsRepository.lastRepeatMode.first()
            val mediaItems = lastQueue.map { it.toMediaItem() }
            val validStartIndex = lastQueue.indexOfFirst { it.id == lastTrackId }.coerceAtLeast(0)
            mediaController?.let { controller ->
                controller.setMediaItems(mediaItems, validStartIndex, lastPosition)
                controller.repeatMode = repeatMode
                controller.shuffleModeEnabled = false
                controller.prepare()
                _uiState.update { it.copy(shuffleModeEnabled = shuffleEnabled) }
                updateStateFromController()
            }
            _uiState.update { it.copy(isRestoring = false) }
        }
    }

    private fun persistCurrentSession() {
        viewModelScope.launch {
            val controller = mediaController ?: return@launch
            val currentTrack = uiState.value.currentTrack
            if (controller.mediaItemCount == 0 || currentTrack == null) {
                settingsRepository.setLastQueue(emptyList()); settingsRepository.setOriginalQueue(emptyList()); return@launch
            }
            val currentQueue = (0 until controller.mediaItemCount).mapNotNull { controller.getMediaItemAt(it).mediaId.toLongOrNull() }
            val originalQueue = _originalPlaybackList.value.map { it.id }
            settingsRepository.setLastQueue(currentQueue)
            settingsRepository.setOriginalQueue(originalQueue)
            settingsRepository.setLastTrackIndex(currentTrack.id.toInt())
            settingsRepository.setLastTrackPosition(if (controller.isPlaying || controller.currentPosition > 0) controller.currentPosition else 0)
            settingsRepository.setLastShuffleMode(uiState.value.shuffleModeEnabled)
            settingsRepository.setLastRepeatMode(controller.repeatMode)
        }
    }

    private fun observeTrackChangesForColorUpdate() {
        viewModelScope.launch {
            _uiState
                .map { it.currentTrack?.id } // Реагируем только на смену трека
                .distinctUntilChanged()
                .collect { trackId ->
                    val track = _uiState.value.currentTrack
                    if (track != null && track.id == trackId) {
                        generateDominantColorForTrack(track)
                    } else if (trackId == null) {
                        _uiState.update { it.copy(miniPlayerBackgroundColor = null, miniPlayerTextColor = null) }
                    }
                }
        }
    }

    fun playTrackList(tracks: List<Track>, startIndex: Int, shuffle: Boolean = false) {
        val availableTracks = tracks.filter { it.isAvailable }
        if (availableTracks.isEmpty()) {
            _uiState.update { it.copy(playerError = "Нет доступных треков для воспроизведения.") }; return
        }
        _originalPlaybackList.value = availableTracks
        val mediaItemsToPlay: List<MediaItem>
        val finalStartIndex: Int
        if (shuffle) {
            val randomStartIndex = availableTracks.indices.random()
            val startTrack = availableTracks[randomStartIndex]
            val shuffledList = availableTracks.shuffled().toMutableList()
            shuffledList.remove(startTrack)
            shuffledList.add(0, startTrack)
            mediaItemsToPlay = shuffledList.map { it.toMediaItem() }
            finalStartIndex = 0
        } else {
            mediaItemsToPlay = availableTracks.map { it.toMediaItem() }
            finalStartIndex = startIndex
        }
        mediaController?.let { controller ->
            controller.setMediaItems(mediaItemsToPlay, finalStartIndex, 0)
            controller.shuffleModeEnabled = false
            controller.prepare()
            controller.play()
        }
        _uiState.update { it.copy(shuffleModeEnabled = shuffle) }
    }

    fun toggleShuffleMode() {
        if (_uiState.value.isTogglingShuffle) return

        viewModelScope.launch {
            val controller = mediaController ?: return@launch
            val currentTrack = uiState.value.currentTrack ?: return@launch
            val originalList = _originalPlaybackList.value
            val currentIndex = controller.currentMediaItemIndex

            if (originalList.isEmpty() || currentIndex == C.INDEX_UNSET) return@launch

            _uiState.update { it.copy(isTogglingShuffle = true) }

            try {
                val newShuffleState = !uiState.value.shuffleModeEnabled

                // 1. (Background Thread) Готовим новый "хвост" очереди
                val newTail = withContext(Dispatchers.Default) {
                    if (newShuffleState) {
                        // Включаем Shuffle: перемешиваем все, кроме текущего трека
                        val otherTracks = originalList.filter { it.id != currentTrack.id }
                        otherTracks.shuffled().map { it.toMediaItem() }
                    } else {
                        // Выключаем Shuffle: восстанавливаем оригинальный порядок после текущего
                        val originalIndex = originalList.indexOfFirst { it.id == currentTrack.id }
                        if (originalIndex != -1) {
                            originalList.subList(originalIndex + 1, originalList.size).map { it.toMediaItem() }
                        } else {
                            // Если вдруг текущего трека нет в оригинальном списке, возвращаем пустой хвост
                            emptyList()
                        }
                    }
                }

                // 2. (Main Thread) Атомарно обновляем плейлист в плеере
                // Проверяем, есть ли что удалять
                if (controller.mediaItemCount > currentIndex + 1) {
                    controller.removeMediaItems(currentIndex + 1, controller.mediaItemCount)
                }
                // Проверяем, есть ли что добавлять
                if (newTail.isNotEmpty()) {
                    controller.addMediaItems(currentIndex + 1, newTail)
                }

                // 3. (Main Thread) Обновляем состояние UI
                _uiState.update { it.copy(shuffleModeEnabled = newShuffleState) }

            } finally {
                _uiState.update { it.copy(isTogglingShuffle = false) }
            }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        mediaController?.let {
            val pitch = it.playbackParameters.pitch
            it.playbackParameters = PlaybackParameters(speed, pitch)
        }
    }

    private fun startPositionUpdates() {
        positionUpdaterJob?.cancel()
        positionUpdaterJob = viewModelScope.launch {
            while (true) {
                mediaController?.let {
                    if (it.isPlaying) {
                        val currentPosition = it.currentPosition
                        if (_uiState.value.currentPosition != currentPosition) {
                            _uiState.update { state -> state.copy(currentPosition = currentPosition) }
                        }
                    }
                }
                delay(1000)
            }
        }
    }

    private suspend fun updateStateFromController() {
        stateUpdateMutex.withLock {
            val controller = mediaController ?: return
            val currentTrack = controller.currentMediaItem?.toTrack(musicRepository)
            _uiState.update {
                it.copy(
                    currentTrack = currentTrack,
                    isPlaying = controller.isPlaying,
                    duration = controller.duration.coerceAtLeast(0L),
                    currentPosition = controller.currentPosition.coerceAtLeast(0L),
                    isLoading = controller.playbackState == Player.STATE_BUFFERING,
                    repeatMode = controller.repeatMode,
                    playbackSpeed = controller.playbackParameters.speed
                )
            }
            if (controller.isPlaying) { startPositionUpdates() } else { positionUpdaterJob?.cancel() }
        }
    }

    private suspend fun generateDominantColorForTrack(track: Track) {
        val albumId = track.albumId ?: run {
            _uiState.update { it.copy(miniPlayerBackgroundColor = null, miniPlayerTextColor = null) }
            return
        }
        val albumArtUri = Uri.parse("content://media/external/audio/albumart/$albumId")

        withContext(Dispatchers.IO) {
            try {
                val request = ImageRequest.Builder(context)
                    .data(albumArtUri)
                    .scale(Scale.FILL)
                    .allowHardware(false)
                    .build()

                val imageResult = context.imageLoader.execute(request)
                val bitmap = (imageResult.drawable as? BitmapDrawable)?.bitmap ?: return@withContext

                Palette.from(bitmap).generate { palette ->
                    val swatch = palette?.mutedSwatch ?: palette?.dominantSwatch
                    if (swatch != null) {
                        // Обновляем состояние уже на Main потоке
                        viewModelScope.launch {
                            _uiState.update {
                                it.copy(
                                    miniPlayerBackgroundColor = Color(swatch.rgb),
                                    miniPlayerTextColor = Color(swatch.bodyTextColor)
                                )
                            }
                        }
                    } else {
                        viewModelScope.launch {
                            _uiState.update { it.copy(miniPlayerBackgroundColor = null, miniPlayerTextColor = null) }
                        }
                    }
                }
            } catch (e: Exception) {
                // ### ИСПРАВЛЕНИЕ: Используем withContext без @ ###
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(miniPlayerBackgroundColor = null, miniPlayerTextColor = null) }
                }
            }
        }
    }

    fun clearPlayerError() {
        _uiState.update { it.copy(playerError = null) }
    }

    fun toggleFavorite() = viewModelScope.launch {
        uiState.value.currentTrack?.let { track ->
            val newFavStatus = !track.isFavorite
            musicRepository.updateFavoriteStatus(track.id, newFavStatus)
            _originalPlaybackList.update { list ->
                list.map { if (it.id == track.id) it.copy(isFavorite = newFavStatus) else it }
            }
        }
    }

    fun stopAndClearPlayer() {
        mediaController?.let { it.stop(); it.clearMediaItems() }
        _originalPlaybackList.value = emptyList()
    }

    fun togglePlayPause() = mediaController?.let { if (it.isPlaying) it.pause() else it.play() }
    fun seekTo(position: Long) = mediaController?.seekTo(position)
    fun skipNext() = mediaController?.seekToNextMediaItem()
    fun skipPrevious() = mediaController?.seekToPreviousMediaItem()

    fun toggleRepeatMode() {
        mediaController?.let {
            it.repeatMode = when (it.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }

    fun formatTime(milliseconds: Long): String {
        if (milliseconds < 0) return "0:00"
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onCleared() {
        persistCurrentSession()
        mediaController?.release()
        super.onCleared()
    }

    private inner class PlayerListener : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            viewModelScope.launch {
                updateStateFromController()
                if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                    val newTrackId = player.currentMediaItem?.mediaId?.toLongOrNull()
                    if (newTrackId != null) { musicRepository.incrementPlayCount(newTrackId, System.currentTimeMillis()) }
                }
                if (events.containsAny(
                        Player.EVENT_TIMELINE_CHANGED, Player.EVENT_MEDIA_ITEM_TRANSITION,
                        Player.EVENT_PLAY_WHEN_READY_CHANGED, Player.EVENT_PLAYBACK_STATE_CHANGED,
                        Player.EVENT_REPEAT_MODE_CHANGED
                    )) {
                    persistCurrentSession()
                }
                if (events.contains(Player.EVENT_PLAYER_ERROR)) {
                    val error = player.playerError
                    val failedMediaId = player.currentMediaItem?.mediaId?.toLongOrNull()
                    if (error != null && failedMediaId != null) {
                        viewModelScope.launch {
                            musicRepository.updateTrackAvailability(failedMediaId, false)
                            val failedTrack = musicRepository.getTrackById(failedMediaId).firstOrNull()
                            val errorMessage = "Ошибка: '${failedTrack?.displayTitle ?: "трек"}' недоступен."
                            _uiState.update { it.copy(playerError = errorMessage) }
                        }
                    }
                }
            }
        }
    }
}

data class PlayerUiState(
    val isLoading: Boolean = false,
    val isPlaying: Boolean = false,
    val currentTrack: Track? = null,
    val duration: Long = 0L,
    val currentPosition: Long = 0L,
    val shuffleModeEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val playerError: String? = null,
    val playbackSpeed: Float = 1.0f,
    val isRestoring: Boolean = true,
    val isTogglingShuffle: Boolean = false,
    val miniPlayerBackgroundColor: Color? = null,
    val miniPlayerTextColor: Color? = null
) {
    val isPlayerVisible: Boolean
        get() = currentTrack != null
}