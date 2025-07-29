// ПУТЬ: /app/src/main/java/com/crackmod/flowave/presentation/screens/settings/AudioSettingsScreen.kt
package com.crackmod.flowave.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.crackmod.flowave.presentation.components.FlowaveTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioSettingsScreen(
    onBackPress: () -> Unit,
    onNavigateToAudioEffects: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val enableNormalization by viewModel.enableVolumeNormalization.collectAsState()
    val gaplessPlaybackEnabled by viewModel.gaplessPlaybackEnabled.collectAsState()

    Scaffold(
        topBar = {
            FlowaveTopAppBar(
                title = "Звук и воспроизведение",
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
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
                    title = "Аудиоэффекты",
                    subtitle = "Эквалайзер, усиление басов, объемный звук",
                    onClick = onNavigateToAudioEffects
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Нормализация громкости",
                    subtitle = "Автоматическое выравнивание громкости треков",
                    isChecked = enableNormalization,
                    onCheckedChange = viewModel::setEnableVolumeNormalization
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Бесшовный переход",
                    subtitle = "Убирает тишину между треками",
                    isChecked = gaplessPlaybackEnabled,
                    onCheckedChange = viewModel::setGaplessPlaybackEnabled
                )
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        modifier = Modifier.clickable { onCheckedChange(!isChecked) },
        trailingContent = {
            Switch(checked = isChecked, onCheckedChange = onCheckedChange)
        }
    )
}