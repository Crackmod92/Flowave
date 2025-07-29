package com.crackmod.flowave.domain.model

data class Artist(
    val id: Long, // <-- ИЗМЕНЕНИЕ: String -> Long
    val name: String,
    val albumCount: Int,
    val trackCount: Int,
    val dateAdded: Long
) {
    val displayName: String
        get() = if (name.isNotBlank()) name else "Unknown Artist"
}