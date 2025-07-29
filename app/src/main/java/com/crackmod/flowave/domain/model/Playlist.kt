// domain/model/Playlist.kt
package com.crackmod.flowave.domain.model

data class Playlist(
    val id: String,
    val name: String,
    val description: String?,
    val dateCreated: Long,
    val dateModified: Long,
    val trackCount: Int = 0,
    val isSystem: Boolean = false
)
