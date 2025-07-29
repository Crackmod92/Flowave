package com.crackmod.flowave.data.service

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.crackmod.flowave.data.audio.AudioEffectsManager
import com.crackmod.flowave.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaLibraryService() {

    @Inject
    lateinit var player: ExoPlayer

    @Inject
    lateinit var mediaSession: MediaLibrarySession

    @Inject
    lateinit var audioEffectsManager: AudioEffectsManager

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        player.addListener(PlayerListener())

        if (player.audioSessionId != 0) {
            audioEffectsManager.attach(player.audioSessionId)
        }

        observeSettings()
    }

    @OptIn(UnstableApi::class)
    private fun observeSettings() {
        serviceScope.launch {
            settingsRepository.gaplessPlaybackEnabled.collectLatest { isEnabled ->
                player.skipSilenceEnabled = isEnabled
            }
        }
    }

    @UnstableApi
    private inner class PlayerListener : Player.Listener {
        // Нам все еще нужен этот слушатель для аудиоэффектов
        override fun onAudioSessionIdChanged(audioSessionId: Int) {
            if (audioSessionId != 0) {
                audioEffectsManager.attach(audioSessionId)
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.playWhenReady) {
            player.release()
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        audioEffectsManager.release()
        mediaSession.release()
        player.release()
    }
}