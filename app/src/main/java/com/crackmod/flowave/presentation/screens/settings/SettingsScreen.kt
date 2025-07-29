// ПУТЬ: /app/src/main/java/com/crackmod/flowave/presentation/screens/settings/SettingsScreen.kt
package com.crackmod.flowave.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.crackmod.flowave.presentation.components.FlowaveTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToAppearance: () -> Unit,
    onNavigateToAudio: () -> Unit,
    onNavigateToLibrary: () -> Unit
) {
    Scaffold(
        topBar = {
            FlowaveTopAppBar(
                title = "Настройки",
                navigationIcon = null // На главном экране настроек нет кнопки "назад"
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                SettingsNavigationItem(
                    title = "Внешний вид",
                    subtitle = "Темы, стиль плеера",
                    onClick = onNavigateToAppearance
                )
            }
            item {
                SettingsNavigationItem(
                    title = "Звук и воспроизведение",
                    subtitle = "Эквалайзер, кроссфейд, нормализация",
                    onClick = onNavigateToAudio
                )
            }
            item {
                SettingsNavigationItem(
                    title = "Медиатека",
                    subtitle = "Папки сканирования, метаданные",
                    onClick = onNavigateToLibrary
                )
            }
            // Здесь можно добавить "О приложении", "Помощь" и т.д.
        }
    }
}

// Переиспользуемый компонент для элементов навигации
@Composable
fun SettingsNavigationItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}