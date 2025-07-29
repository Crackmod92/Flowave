// ПУТЬ: /app/src/main/java/com/crackmod/flowave/presentation/screens/settings/AppearanceSettingsScreen.kt
package com.crackmod.flowave.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crackmod.flowave.domain.repository.NowPlayingScreenStyle
import com.crackmod.flowave.domain.repository.ThemeSetting
import com.crackmod.flowave.presentation.components.FlowaveTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    onBackPress: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentTheme by viewModel.themeSetting.collectAsState()
    val currentNowPlayingStyle by viewModel.nowPlayingScreenStyle.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showNowPlayingStyleDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        ThemeChooserDialog(
            currentTheme = currentTheme,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = {
                viewModel.setTheme(it)
                showThemeDialog = false
            }
        )
    }

    if (showNowPlayingStyleDialog) {
        NowPlayingStyleChooserDialog(
            currentStyle = currentNowPlayingStyle,
            onDismiss = { showNowPlayingStyleDialog = false },
            onStyleSelected = {
                viewModel.setNowPlayingScreenStyle(it)
                showNowPlayingStyleDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            FlowaveTopAppBar(
                title = "Внешний вид",
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
                    title = "Тема",
                    subtitle = currentTheme.toDisplayString(),
                    onClick = { showThemeDialog = true }
                )
            }
            item {
                SettingsNavigationItem(
                    title = "Стиль экрана \"Сейчас играет\"",
                    subtitle = currentNowPlayingStyle.toDisplayString(),
                    onClick = { showNowPlayingStyleDialog = true }
                )
            }
        }
    }
}

@Composable
private fun ThemeChooserDialog(
    currentTheme: ThemeSetting,
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeSetting) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите тему") },
        text = {
            Column {
                ThemeSetting.entries.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTheme == theme,
                            onClick = { onThemeSelected(theme) }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(theme.toDisplayString())
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

@Composable
private fun NowPlayingStyleChooserDialog(
    currentStyle: NowPlayingScreenStyle,
    onDismiss: () -> Unit,
    onStyleSelected: (NowPlayingScreenStyle) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите стиль экрана \"Сейчас играет\"") },
        text = {
            Column {
                NowPlayingScreenStyle.entries.forEach { style ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStyleSelected(style) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentStyle == style,
                            onClick = { onStyleSelected(style) }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(style.toDisplayString())
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