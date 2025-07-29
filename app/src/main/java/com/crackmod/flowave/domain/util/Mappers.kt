// СОЗДАЙ НОВЫЙ ФАЙЛ: domain/util/Mappers.kt
package com.crackmod.flowave.domain.util

import android.content.ContentUris
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.domain.repository.MusicRepository
import kotlinx.coroutines.flow.firstOrNull

fun Track.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId(this.id.toString())
        .setUri(this.uri)
        .setMediaMetadata(
            androidx.media3.common.MediaMetadata.Builder()
                .setTitle(this.title)
                .setArtist(this.artist)
                .setAlbumTitle(this.album)
                .setArtworkUri(this.albumId?.let {
                    ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, it)
                })
                .setExtras(android.os.Bundle().apply {
                    putLong("track_id", this@toMediaItem.id)
                })
                .build()
        )
        .build()
}

suspend fun MediaItem.toTrack(musicRepository: MusicRepository): Track? {
    val trackId = this.mediaMetadata.extras?.getLong("track_id") ?: this.mediaId.toLongOrNull()
    return trackId?.let { musicRepository.getTrackById(it).firstOrNull() }
}