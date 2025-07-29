package com.crackmod.flowave.presentation

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.crackmod.flowave.presentation.screens.library.LibraryViewModel
import com.crackmod.flowave.presentation.screens.library.components.HomeScreenSkeleton
import com.crackmod.flowave.presentation.screens.library.components.shimmerBackground
import com.crackmod.flowave.presentation.screens.permissions.PermissionScreen
import com.crackmod.flowave.presentation.screens.permissions.PermissionViewModel
import com.crackmod.flowave.ui.theme.FlowaveTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // УДАЛИЛИ @Inject lateinit var permissionHandler: PermissionHandler, т.к. он больше не нужен напрямую в Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = if (isNightMode) {
                SystemBarStyle.dark(Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            }
        )

        setContent {
            FlowaveTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }
}


@Composable
private fun AppContent() {
    // ИЗМЕНЕНИЕ: hasPermissions теперь nullable Boolean, чтобы отслеживать состояние "проверка..."
    var hasPermissions by remember { mutableStateOf<Boolean?>(null) }
    val permissionViewModel: PermissionViewModel = hiltViewModel()
    val permissionState by permissionViewModel.state.collectAsState()

    val libraryViewModel: LibraryViewModel = hiltViewModel()
    val settingsRepository = libraryViewModel.settingsRepository

    // Этот LaunchedEffect запускается один раз и проверяет разрешения
    LaunchedEffect(Unit) {
        permissionViewModel.checkPermissions()
    }

    LaunchedEffect(permissionState.initialCheckCompleted) {
        if (permissionState.initialCheckCompleted) {
            hasPermissions = permissionState.hasPermissions
            if (permissionState.hasPermissions) {
                val initialScanDone = settingsRepository.initialScanCompleted.first()
                if (!initialScanDone) {
                    libraryViewModel.scanLibrary { /* no-op */ }
                }
            }
        }
    }

    // --- ГЛАВНОЕ ИЗМЕНЕНИЕ: НОВАЯ ЛОГИКА ОТОБРАЖЕНИЯ ---
    when (hasPermissions) {
        // Состояние "проверка...": показываем скелетный UI
        null -> {
            Scaffold { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    HomeScreenSkeleton(modifier = Modifier.fillMaxSize().shimmerBackground())
                }
            }
        }
        // Состояние "разрешений нет": показываем экран запроса
        false -> {
            PermissionScreen(
                onPermissionGranted = { hasPermissions = true },
                viewModel = permissionViewModel
            )
        }
        // Состояние "разрешения есть": показываем главный экран приложения
        true -> {
            MainScreen()
        }
    }
}