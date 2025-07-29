// ПУТЬ: com/crackmod/flowave/presentation/screens/library/components/TrackOptionsBottomSheet.txt

package com.crackmod.flowave.presentation.screens.library.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crackmod.flowave.domain.model.Track

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackOptionsBottomSheet(
    track: Track,
    onDismiss: () -> Unit,
    onPlayNext: (Track) -> Unit, // <-- НОВЫЙ ПАРАМЕТР
    onAddToQueue: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    onEditTags: (Track) -> Unit,
    onDelete: (Track) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                text = track.displayTitle,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = track.displayArtist,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()

            // --- НОВЫЙ ПУНКТ МЕНЮ ---
            ListItem(
                headlineContent = { Text("Играть следующим") },
                leadingContent = { Icon(Icons.AutoMirrored.Filled.PlaylistPlay, null) },
                modifier = Modifier.clickable {
                    onPlayNext(track)
                    onDismiss()
                }
            )
            ListItem(
                headlineContent = { Text("Добавить в очередь") },
                leadingContent = { Icon(Icons.AutoMirrored.Filled.QueueMusic, null) },
                modifier = Modifier.clickable {
                    onAddToQueue(track)
                    onDismiss()
                }
            )
            ListItem(
                headlineContent = { Text("Добавить в плейлист") },
                leadingContent = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null) },
                modifier = Modifier.clickable {
                    onAddToPlaylist(track)
                    onDismiss()
                }
            )
            ListItem(
                headlineContent = { Text("Редактировать теги") },
                leadingContent = { Icon(Icons.Default.Edit, null) },
                modifier = Modifier.clickable {
                    onEditTags(track)
                    onDismiss()
                }
            )
            ListItem(
                headlineContent = { Text("Удалить из медиатеки") },
                leadingContent = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                colors = ListItemDefaults.colors(headlineColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.clickable {
                    onDelete(track)
                    onDismiss()
                }
            )
        }
    }
}