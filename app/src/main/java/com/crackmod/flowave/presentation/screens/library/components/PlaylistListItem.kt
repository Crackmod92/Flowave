package com.crackmod.flowave.presentation.screens.library.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.crackmod.flowave.R
import com.crackmod.flowave.domain.model.Playlist

@Composable
fun PlaylistListItem(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(playlist.name) },
        supportingContent = { Text("${playlist.trackCount} треков") },
        leadingContent = {
            Icon(
                painter = painterResource(id = R.drawable.ic_flowave_playlist),
                contentDescription = "Плейлист",
                modifier = Modifier.size(24.dp)
            )
        },
        modifier = modifier.clickable(onClick = onClick)
    )
}