package com.crackmod.flowave.presentation.screens.permissions

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PermissionScreen(
    onPermissionGranted: () -> Unit,
    viewModel: PermissionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Лаунчер для старых разрешений (Android 10)
    val legacyPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.onPermissionResult(permissions.values.all { it })
    }

    // Лаунчер для нового разрешения (Android 11+)
    val storageManagerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // После возврата из настроек снова проверяем разрешение
        viewModel.checkPermissions()
    }

    LaunchedEffect(Unit) {
        viewModel.checkPermissions()
    }

    LaunchedEffect(state.hasPermissions) {
        if (state.hasPermissions) {
            onPermissionGranted()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.size(120.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(60.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Folder, "Folder", Modifier.size(60.dp), MaterialTheme.colorScheme.onPrimary)
            }
        }
        Spacer(Modifier.height(32.dp))
        Text(
            "Требуется доступ к файлам",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Flowave - это локальный музыкальный плеер. Для сканирования, воспроизведения и редактирования тегов вашей музыки, приложению необходим полный доступ к файловой системе.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {
                if (state.isLegacyPermission) {
                    legacyPermissionLauncher.launch(viewModel.getLegacyPermissions())
                } else {
                    // Создаем интент для открытия системных настроек
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:${context.packageName}")
                    storageManagerLauncher.launch(intent)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Предоставить доступ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}