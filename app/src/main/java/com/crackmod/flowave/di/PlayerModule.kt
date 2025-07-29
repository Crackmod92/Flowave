package com.crackmod.flowave.di

import android.app.Service
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import com.crackmod.flowave.data.player.RenderersFactory
import com.crackmod.flowave.data.service.MusicService
// --- УДАЛЕНО ---
// import com.crackmod.flowave.presentation.player.ShufflePlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object PlayerModule {

    @Provides
    @ServiceScoped
    fun provideAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @OptIn(UnstableApi::class)
    @Provides
    @ServiceScoped
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes,
        renderersFactory: RenderersFactory
    ): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setRenderersFactory(renderersFactory)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setTrackSelector(DefaultTrackSelector(context))
            .build()
    }

    @Provides
    @ServiceScoped
    fun providePlayer(exoPlayer: ExoPlayer): Player {
        // ### ИЗМЕНЕНИЕ: Возвращаем чистый ExoPlayer ###
        return exoPlayer
    }

    @Provides
    @ServiceScoped
    fun provideMediaSession(
        player: Player,
        service: Service
    ): MediaLibrarySession {
        val callback = object : MediaLibrarySession.Callback {}
        return MediaLibrarySession.Builder(service as MusicService, player, callback)
            .build()
    }
}