package com.crackmod.flowave.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String,
    val duration: Long,
    val year: Int?,
    val genre: String?,
    val trackNumber: Int?,
    val path: String,
    val contentUri: String?,
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

    // НОВЫЕ ПОЛЯ ДЛЯ КЭШИРОВАНИЯ ТЕКСТА
    val syncedLyrics: String? = null,
    val plainLyrics: String? = null,
    val areLyricsInstrumental: Boolean = false
)