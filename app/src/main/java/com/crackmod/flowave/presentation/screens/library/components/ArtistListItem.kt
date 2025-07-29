package com.crackmod.flowave.presentation.screens.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crackmod.flowave.domain.model.Artist

@Composable
fun ArtistListItem(
    artist: Artist,
    onArtistClick: (Artist) -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier.clickable { onArtistClick(artist) },
        headlineContent = { Text(artist.displayName, fontWeight = FontWeight.SemiBold) },
        supportingContent = {
            Text(
                "${formatCountable(artist.albumCount, "альбом", "альбома", "альбомов")} • " +
                        formatCountable(artist.trackCount, "трек", "трека", "треков")
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = artist.name.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    )
}

private fun formatCountable(count: Int, one: String, few: String, many: String): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "$count $one"
        count % 10 in 2..4 && count % 100 !in 12..14 -> "$count $few"
        else -> "$count $many"
    }
}