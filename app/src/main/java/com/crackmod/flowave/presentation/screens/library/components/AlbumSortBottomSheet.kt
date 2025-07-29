// ФАЙЛ: AlbumSortBottomSheet.kt
// ПУТЬ: com/crackmod/flowave/presentation/screens/library/components/AlbumSortBottomSheet.kt

package com.crackmod.flowave.presentation.screens.library.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crackmod.flowave.domain.util.AlbumSortBy
import com.crackmod.flowave.domain.util.SortOrder

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlbumSortBottomSheet(
    currentSortBy: AlbumSortBy,
    currentSortOrder: SortOrder,
    isGridView: Boolean,
    onSortByChange: (AlbumSortBy) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onLayoutChange: (isGrid: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                "Вид и сортировка",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Секция вида
            Text("Отображать как", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = isGridView, onClick = { onLayoutChange(true) }, label = { Text("Сетка") }, leadingIcon = { Icon(Icons.Default.ViewModule, null) })
                FilterChip(selected = !isGridView, onClick = { onLayoutChange(false) }, label = { Text("Список") }, leadingIcon = { Icon(Icons.Default.List, null) })
            }

            Spacer(Modifier.height(24.dp))

            // Секция сортировки
            Text("Сортировать по", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AlbumSortBy.entries.forEach { sortBy ->
                    FilterChip(
                        selected = currentSortBy == sortBy,
                        onClick = { onSortByChange(sortBy) },
                        label = { Text(sortBy.displayName) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Секция порядка
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