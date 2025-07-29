package com.crackmod.flowave.data.mapper

import com.crackmod.flowave.data.remote.dto.LyricsResponse
import com.crackmod.flowave.domain.model.Lyrics
import com.crackmod.flowave.domain.model.LyricsLine
import java.util.regex.Pattern

fun LyricsResponse.toDomain(): Lyrics {
    return Lyrics(
        lines = parseLyrics(plainLyrics, syncedLyrics),
        isInstrumental = instrumental
    )
}

private fun parseLyrics(plain: String?, synced: String?): List<LyricsLine> {
    if (!synced.isNullOrBlank()) {
        val parsedLines = parseSyncedLyrics(synced)
        if (parsedLines.isNotEmpty()) return parsedLines
    }
    if (!plain.isNullOrBlank()) {
        return plain.lines().map { LyricsLine(0, it) }
    }
    return emptyList()
}

private fun parseSyncedLyrics(syncedLyrics: String): List<LyricsLine> {
    val lines = mutableListOf<LyricsLine>()
    val pattern = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)")

    syncedLyrics.lines().forEach { line ->
        val matcher = pattern.matcher(line)
        if (matcher.matches()) {
            try {
                val minutes = matcher.group(1)!!.toLong()
                val seconds = matcher.group(2)!!.toLong()
                val millis = matcher.group(3)!!.toLong()
                val content = matcher.group(4)!!.trim()

                val totalMillis = (minutes * 60 + seconds) * 1000 + if (millis.toString().length == 2) millis * 10 else millis
                lines.add(LyricsLine(totalMillis, content))
            } catch (e: Exception) {
                println("Could not parse lyric line: $line")
            }
        }
    }
    return lines.sortedBy { it.startTimeMs }
}