package com.crackmod.flowave.presentation.screens.library.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crackmod.flowave.domain.util.SortOrder
import com.crackmod.flowave.domain.util.TrackSortBy

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TrackSortBottomSheet(
    currentSortBy: TrackSortBy,
    currentSortOrder: SortOrder,
    onSortByChange: (TrackSortBy) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onPlayClick: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "Сортировка",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Секция "Сортировать по"
            Text("Сортировать по", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TrackSortBy.entries.forEach { sortBy ->
                    FilterChip(
                        selected = currentSortBy == sortBy,
                        onClick = { onSortByChange(sortBy) },
                        label = { Text(sortBy.displayName) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Секция "Порядок"
            Text("Порядок", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SortOrder.entries.forEach { sortOrder ->
                    FilterChip(
                        selected = currentSortOrder == sortOrder,
                        onClick = { onSortOrderChange(sortOrder) },
                        label = { Text(sortOrder.displayName) }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Кнопка "Воспроизвести"
            Button(
                onClick = {
                    onPlayClick()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Воспроизвести")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}