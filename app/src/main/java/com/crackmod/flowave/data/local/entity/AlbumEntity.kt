package com.crackmod.flowave.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val year: Int?,
    val trackCount: Int,
    val albumArtPath: String?,
    val dateAdded: Long
)