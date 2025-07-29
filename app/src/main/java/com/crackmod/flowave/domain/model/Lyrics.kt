package com.crackmod.flowave.domain.model

// Новая модель для одной строки синхронизированного текста
data class LyricsLine(
    val startTimeMs: Long,
    val content: String
)

// Обновленная основная модель для текста
data class Lyrics(
    val lines: List<LyricsLine>,
    val isInstrumental: Boolean
)