package com.crackmod.flowave.domain.model

import android.net.Uri

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String,
    val duration: Long,
    val year: Int?,
    val genre: String?,
    val trackNumber: Int?,
    val path: String,
    val contentUri: Uri?,
    val dateAdded: Long,
    val dateModified: Long,
    val size: Long,
    val albumArtPath: String?,
    val albumId: Long?,
    val artistId: Long?,
    val bitrate: String?,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayed: Long? = null,
    val isAvailable: Boolean = true,

    // НОВЫЕ ПОЛЯ
    val syncedLyrics: String? = null,
    val plainLyrics: String? = null,
    val areLyricsInstrumental: Boolean = false
) {
    val uri: Uri get() = contentUri ?: Uri.parse(path)

    val displayArtist: String
        get() = if (artist.isNotBlank()) artist else "Unknown Artist"

    val displayAlbum: String
        get() = if (album.isNotBlank()) album else "Unknown Album"

    val displayTitle: String
        get() = if (title.isNotBlank()) title else "Unknown Track"

    val durationFormatted: String
        get() = formatDuration(duration)

    // Проверка, есть ли у нас хоть какой-то кэшированный текст
    val hasLyrics: Boolean
        get() = areLyricsInstrumental || !syncedLyrics.isNullOrBlank() || !plainLyrics.isNullOrBlank()

    private fun formatDuration(duration: Long): String {
        val minutes = duration / 60000
        val seconds = (duration % 60000) / 1000
        return String.format("%d:%02d", minutes, seconds)
    }
}