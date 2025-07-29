package com.crackmod.flowave.presentation.screens.library.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crackmod.flowave.domain.util.ArtistSortBy
import com.crackmod.flowave.domain.util.SortOrder

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ArtistSortBottomSheet(
    currentSortBy: ArtistSortBy,
    currentSortOrder: SortOrder,
    onSortByChange: (ArtistSortBy) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                "Сортировка",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text("Сортировать по", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ArtistSortBy.entries.forEach { sortBy ->
                    FilterChip(
                        selected = currentSortBy == sortBy,
                        onClick = { onSortByChange(sortBy) },
                        label = { Text(sortBy.displayName) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

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
            Spacer(Modifier.height(24.dp))
        }
    }
}