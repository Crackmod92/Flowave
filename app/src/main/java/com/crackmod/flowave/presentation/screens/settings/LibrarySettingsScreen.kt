// ПУТЬ: /app/src/main/java/com/crackmod/flowave/presentation/screens/settings/LibrarySettingsScreen.kt
package com.crackmod.flowave.presentation.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crackmod.flowave.presentation.components.FlowaveTopAppBar
import com.crackmod.flowave.presentation.screens.home.components.ScanStatusBanner
import com.crackmod.flowave.presentation.screens.library.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrarySettingsScreen(
    onBackPress: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    val scanFolders by viewModel.scanFolders.collectAsState()
    val bannerState by libraryViewModel.bannerState.collectAsState()
    var showScanFoldersDialog by remember { mutableStateOf(false) }

    if (showScanFoldersDialog) {
        val context = LocalContext.current
        val openDirectoryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree()
        ) { uri: Uri? ->
            uri?.let { newFolderUri ->
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(newFolderUri, takeFlags)
                val updatedFolders = (scanFolders + newFolderUri).distinct()
                viewModel.setScanFolders(updatedFolders)
            }
        }

        ScanFoldersDialog(
            currentFolders = scanFolders,
            onDismiss = { showScanFoldersDialog = false },
            onAddFolder = { openDirectoryLauncher.launch(null) },
            onRemoveFolder = { folderUri ->
                val updatedFolders = scanFolders.filter { it != folderUri }
                viewModel.setScanFolders(updatedFolders)
            }
        )
    }

    Scaffold(
        topBar = {
            FlowaveTopAppBar(
                title = "Медиатека",
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    SettingsNavigationItem(
                        title = "Папки для сканирования",
                        subtitle = if (scanFolders.isEmpty()) "По умолчанию (Music, Download)" else "Выбрано папок: ${scanFolders.size}",
                        onClick = { showScanFoldersDialog = true }
                    )
                }
                item {
                    SettingsNavigationItem(
                        title = "Пересканировать медиатеку",
                        subtitle = "Запустить полное сканирование по выбранным папкам",
                        onClick = {
                            libraryViewModel.scanLibrary { result ->
                                result.onSuccess { count ->
                                    libraryViewModel.showBanner("Сканирование завершено. Найдено $count треков.")
                                }.onFailure { error ->
                                    libraryViewModel.showBanner("Ошибка сканирования: ${error.message}", isError = true)
                                }
                            }
                        }
                    )
                }
            }

            ScanStatusBanner(
                isVisible = bannerState.isVisible,
                message = bannerState.message,
                isError = bannerState.isError,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(paddingValues)
            )
        }
    }
}


@Composable
private fun ScanFoldersDialog(
    currentFolders: List<Uri>,
    onDismiss: () -> Unit,
    onAddFolder: () -> Unit,
    onRemoveFolder: (Uri) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Папки для сканирования") },
        text = {
            Column {
                if (currentFolders.isEmpty()) {
                    Text("Не выбрано ни одной папки. Будут сканироваться стандартные системные папки (Музыка, Загрузки).")
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Text("Выбранные папки:")
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(currentFolders) { uri ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = getDisplayNameForUri(uri),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { onRemoveFolder(uri) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Удалить папку")
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(onClick = onAddFolder, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Добавить папку")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Готово")
            }
        }
    )
}

private fun getDisplayNameForUri(uri: Uri): String {
    val path = uri.path
    if (path != null && path.contains(":")) {
        val folderPath = path.substringAfterLast(':')
        return Uri.decode(folderPath) ?: folderPath
    }
    return uri.lastPathSegment ?: "Неизвестная папка"
}