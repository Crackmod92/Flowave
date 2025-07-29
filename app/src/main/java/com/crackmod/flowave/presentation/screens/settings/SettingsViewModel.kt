// ПУТЬ: com/crackmod/flowave/presentation/screens/settings/SettingsViewModel.kt
// КОД:

package com.crackmod.flowave.presentation.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crackmod.flowave.domain.repository.NowPlayingScreenStyle
import com.crackmod.flowave.domain.repository.SettingsRepository
import com.crackmod.flowave.domain.repository.ThemeSetting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// ЭТОТ ФАЙЛ БОЛЬШЕ НЕ НУЖЕН ЗДЕСЬ, НО ОСТАВИМ ДЛЯ ДРУГИХ ФАЙЛОВ
fun ThemeSetting.toDisplayString(): String {
    return when (this) {
        ThemeSetting.SYSTEM -> "Как в системе"
        ThemeSetting.LIGHT -> "Светлая"
        ThemeSetting.DARK -> "Темная"
        ThemeSetting.AMOLED -> "Черная (AMOLED)"
    }
}

fun NowPlayingScreenStyle.toDisplayString(): String {
    return when (this) {
        NowPlayingScreenStyle.SOLAR_FLARE -> "Солнечная вспышка"
        NowPlayingScreenStyle.PULSAR -> "Пульсар"
        NowPlayingScreenStyle.EVENT_HORIZON -> "Горизонт событий"
        NowPlayingScreenStyle.WARP_DRIVE -> "Варп-драйв"
        NowPlayingScreenStyle.AURORA -> "Аврора"
        NowPlayingScreenStyle.NEBULA -> "Туманность"
        NowPlayingScreenStyle.CONSTELLATION -> "Созвездие"
        NowPlayingScreenStyle.ASTEROID_BELT -> "Пояс астероидов"
        NowPlayingScreenStyle.GALACTIC_CORE -> "Галактическое ядро"
        NowPlayingScreenStyle.SPACE_ODYSSEY -> "Космическая одиссея"
    }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val settingsRepository: SettingsRepository // Делаем публичным для дочерних экранов
) : ViewModel() {

    // --- Функции для изменения настроек ---
    fun setTheme(theme: ThemeSetting) = viewModelScope.launch { settingsRepository.setThemeSetting(theme) }
    fun setScanFolders(folders: List<Uri>) = viewModelScope.launch { settingsRepository.setScanFolders(folders) }
    fun setNowPlayingScreenStyle(style: NowPlayingScreenStyle) = viewModelScope.launch { settingsRepository.setNowPlayingScreenStyle(style) }
    fun setEnableVolumeNormalization(isEnabled: Boolean) = viewModelScope.launch { settingsRepository.setEnableVolumeNormalization(isEnabled) }
    fun setGaplessPlaybackEnabled(isEnabled: Boolean) = viewModelScope.launch { settingsRepository.setGaplessPlaybackEnabled(isEnabled) }

    // --- Прямые StateFlow для дочерних экранов ---
    val themeSetting: StateFlow<ThemeSetting> = settingsRepository.themeSetting
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeSetting.SYSTEM)

    val nowPlayingScreenStyle: StateFlow<NowPlayingScreenStyle> = settingsRepository.nowPlayingScreenStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NowPlayingScreenStyle.SOLAR_FLARE)

    val scanFolders: StateFlow<List<Uri>> = settingsRepository.scanFolders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val enableVolumeNormalization: StateFlow<Boolean> = settingsRepository.enableVolumeNormalization
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val gaplessPlaybackEnabled: StateFlow<Boolean> = settingsRepository.gaplessPlaybackEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
}