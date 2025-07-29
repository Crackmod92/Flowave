package com.crackmod.flowave.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LyricsResponse(
    val id: Long,
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val duration: Double,
    val instrumental: Boolean,
    val plainLyrics: String?,
    val syncedLyrics: String?
)