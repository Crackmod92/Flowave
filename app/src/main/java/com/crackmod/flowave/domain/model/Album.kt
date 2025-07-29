package com.crackmod.flowave.domain.model

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore

data class Album(
    val id: Long, // <-- ИЗМЕНЕНИЕ: String -> Long
    val title: String,
    val artist: String,
    val year: Int?,
    val trackCount: Int,
    val albumArtPath: String?,
    val dateAdded: Long
) {
    fun Album.getAlbumArtUri(): Uri? {
        return try {
            ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, this.id)
        } catch (e: Exception) {
            null
        }
    }
    val displayTitle: String
        get() = if (title.isNotBlank()) title else "Unknown Album"

    val displayArtist: String
        get() = if (artist.isNotBlank()) artist else "Unknown Artist"
}