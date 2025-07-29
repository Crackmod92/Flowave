package com.crackmod.flowave.data.mapper

import android.net.Uri
import com.crackmod.flowave.data.local.entity.*
import com.crackmod.flowave.domain.model.*

fun TrackEntity.toDomain(): Track {
    return Track(
        id = id,
        title = title,
        artist = artist,
        album = album,
        albumArtist = albumArtist,
        duration = duration,
        year = year,
        genre = genre,
        trackNumber = trackNumber,
        path = path,
        contentUri = contentUri?.let { Uri.parse(it) },
        dateAdded = dateAdded,
        dateModified = dateModified,
        size = size,
        albumArtPath = albumArtPath,
        albumId = albumId,
        artistId = artistId,
        bitrate = bitrate,
        isFavorite = isFavorite,
        playCount = playCount,
        lastPlayed = lastPlayed,
        isAvailable = isAvailable,
        // Маппинг новых полей
        syncedLyrics = syncedLyrics,
        plainLyrics = plainLyrics,
        areLyricsInstrumental = areLyricsInstrumental
    )
}

fun Track.toEntity(): TrackEntity {
    return TrackEntity(
        id = id,
        title = title,
        artist = artist,
        album = album,
        albumArtist = albumArtist,
        duration = duration,
        year = year,
        genre = genre,
        trackNumber = trackNumber,
        path = path,
        contentUri = contentUri?.toString(),
        dateAdded = dateAdded,
        dateModified = dateModified,
        size = size,
        albumArtPath = albumArtPath,
        albumId = albumId,
        artistId = artistId,
        bitrate = bitrate,
        isFavorite = isFavorite,
        playCount = playCount,
        lastPlayed = lastPlayed,
        isAvailable = isAvailable,
        // Маппинг новых полей
        syncedLyrics = syncedLyrics,
        plainLyrics = plainLyrics,
        areLyricsInstrumental = areLyricsInstrumental
    )
}

fun AlbumEntity.toDomain(): Album {
    return Album(
        id = id,
        title = title,
        artist = artist,
        year = year,
        trackCount = trackCount,
        albumArtPath = albumArtPath,
        dateAdded = dateAdded
    )
}

fun Album.toEntity(): AlbumEntity {
    return AlbumEntity(
        id = id,
        title = title,
        artist = artist,
        year = year,
        trackCount = trackCount,
        albumArtPath = albumArtPath,
        dateAdded = dateAdded
    )
}

fun ArtistEntity.toDomain(): Artist {
    return Artist(
        id = id,
        name = name,
        albumCount = albumCount,
        trackCount = trackCount,
        dateAdded = dateAdded
    )
}

fun Artist.toEntity(): ArtistEntity {
    return ArtistEntity(
        id = id,
        name = name,
        albumCount = albumCount,
        trackCount = trackCount,
        dateAdded = dateAdded
    )
}

fun PlaylistEntity.toDomain(): Playlist {
    return Playlist(
        id = id,
        name = name,
        description = description,
        dateCreated = dateCreated,
        dateModified = dateModified,
        trackCount = trackCount,
        isSystem = isSystem
    )
}

fun Playlist.toEntity(): PlaylistEntity {
    return PlaylistEntity(
        id = id,
        name = name,
        description = description,
        dateCreated = dateCreated,
        dateModified = dateModified,
        trackCount = trackCount,
        isSystem = isSystem
    )
}