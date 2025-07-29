package com.crackmod.flowave.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey val id: Long, // <-- ИЗМЕНЕНИЕ: String -> Long
    val name: String,
    val albumCount: Int,
    val trackCount: Int,
    val dateAdded: Long
)