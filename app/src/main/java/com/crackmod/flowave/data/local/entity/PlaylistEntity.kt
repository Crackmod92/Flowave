package com.crackmod.flowave.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val dateCreated: Long,
    val dateModified: Long,
    val trackCount: Int = 0,
    val isSystem: Boolean = false // for "Favorites", "Recently Added", etc.
)

@Entity(
    tableName = "playlist_tracks",
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("playlistId"),
        Index("trackId")
    ]
)
data class PlaylistTrackEntity(
    @PrimaryKey val id: String,
    val playlistId: String,
    val trackId: Long,
    val position: Int,
    val dateAdded: Long
)
