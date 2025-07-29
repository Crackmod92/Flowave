package com.crackmod.flowave.presentation.screens.library.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crackmod.flowave.domain.model.Playlist

@Composable
fun AddToPlaylistDialog(
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить в плейлист") },
        text = {
            LazyColumn {
                if (playlists.isEmpty()) {
                    item {
                        Text(
                            "У вас еще нет плейлистов. Создайте новый во вкладке 'Плейлисты'.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(playlists) { playlist ->
                        ListItem(
                            headlineContent = { Text(playlist.name) },
                            modifier = Modifier.clickable { onPlaylistSelected(playlist.id) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}