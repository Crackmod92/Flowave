// ПУТЬ: com/crackmod/flowave/presentation/screens/audio_effects/AudioEffectsViewModel.kt
// КОД:

package com.crackmod.flowave.presentation.screens.audio_effects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crackmod.flowave.data.audio.AudioEffectsManager
import com.crackmod.flowave.domain.repository.BassBoostSettings
import com.crackmod.flowave.domain.repository.EqualizerSettings
import com.crackmod.flowave.domain.repository.SettingsRepository
import com.crackmod.flowave.domain.repository.VirtualizerSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

// Стандартные частоты для 10-полосного эквалайзера (в Герцах)
val standardFrequencies = listOf(31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)

data class AudioEffectsUiState(
    val equalizer: EqualizerSettings = EqualizerSettings(),
    val bassBoost: BassBoostSettings = BassBoostSettings(),
    val virtualizer: VirtualizerSettings = VirtualizerSettings(),
    val equalizerProperties: EqualizerProperties = EqualizerProperties()
)

data class EqualizerProperties(
    val hasEqualizer: Boolean = false,
    val minBandLevel: Short = -1500,
    val maxBandLevel: Short = 1500,
    val hardwareFrequencies: List<Int> = emptyList(), // в mHz
    val presets: List<String> = emptyList()
)

@HiltViewModel
class AudioEffectsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val audioEffectsManager: AudioEffectsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioEffectsUiState())
    val uiState: StateFlow<AudioEffectsUiState> = _uiState.asStateFlow()

    private var bandIndexMapping: Map<Int, Short> = emptyMap()

    init {
        loadInitialState()
        observeSettings()
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            val eq = audioEffectsManager.equalizer
            if (eq != null) {
                val properties = EqualizerProperties(
                    hasEqualizer = true,
                    minBandLevel = eq.bandLevelRange[0],
                    maxBandLevel = eq.bandLevelRange[1],
                    hardwareFrequencies = (0 until eq.numberOfBands).map { i ->
                        eq.getCenterFreq(i.toShort())
                    },
                    presets = (0 until eq.numberOfPresets).map { i ->
                        eq.getPresetName(i.toShort())
                    } + "Custom" // Добавляем "Custom" в список
                )
                // Создаем карту соответствия наших 10 полос к аппаратным
                bandIndexMapping = mapUiToHardwareBands(properties.hardwareFrequencies)
                _uiState.update { it.copy(equalizerProperties = properties) }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.equalizerSettings,
                settingsRepository.bassBoostSettings,
                settingsRepository.virtualizerSettings
            ) { eqSettings, bbSettings, vSettings ->
                AudioEffectsUiState(
                    equalizer = eqSettings,
                    bassBoost = bbSettings,
                    virtualizer = vSettings,
                    equalizerProperties = _uiState.value.equalizerProperties
                )
            }.collect { state ->
                _uiState.value = state
                applyAllEffects(state)
            }
        }
    }

    @Suppress("DEPRECATION") // ИСПРАВЛЕНИЕ: Подавляем предупреждение для setStrength
    private fun applyAllEffects(state: AudioEffectsUiState) {
        // Применяем эквалайзер
        audioEffectsManager.equalizer?.let { eq ->
            eq.enabled = state.equalizer.isEnabled
            if (state.equalizer.isEnabled) {
                // Проходим по всем 10 полосам нашего UI
                for (uiBandIndex in 0 until 10) {
                    // Находим соответствующую аппаратную полосу
                    val hardwareBandIndex = bandIndexMapping[uiBandIndex]
                    if (hardwareBandIndex != null) {
                        // Получаем уровень из настроек для нашей UI-полосы
                        val uiLevel = state.equalizer.bandLevels[uiBandIndex]?.toInt() ?: 0
                        // Добавляем уровень предусилителя
                        val finalLevel = (uiLevel + state.equalizer.preampLevel).toShort()
                        // Ограничиваем значение диапазоном устройства
                        val clampedLevel = finalLevel.coerceIn(
                            _uiState.value.equalizerProperties.minBandLevel,
                            _uiState.value.equalizerProperties.maxBandLevel
                        )
                        eq.setBandLevel(hardwareBandIndex, clampedLevel)
                    }
                }
            }
        }

        // Применяем BassBoost
        audioEffectsManager.bassBoost?.let { bb ->
            bb.enabled = state.bassBoost.isEnabled
            if (state.bassBoost.isEnabled) {
                bb.setStrength(state.bassBoost.strength)
            }
        }

        // Применяем Virtualizer
        audioEffectsManager.virtualizer?.let { v ->
            v.enabled = state.virtualizer.isEnabled
            if (state.virtualizer.isEnabled) {
                v.setStrength(state.virtualizer.strength)
            }
        }
    }

    // --- Методы для управления эквалайзером ---
    fun setEqualizerEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            val newSettings = _uiState.value.equalizer.copy(isEnabled = isEnabled)
            settingsRepository.setEqualizerSettings(newSettings)
        }
    }

    fun setBandLevel(bandIndex: Int, level: Short) {
        viewModelScope.launch {
            val currentLevels = _uiState.value.equalizer.bandLevels.toMutableMap()
            currentLevels[bandIndex] = level
            val newSettings = _uiState.value.equalizer.copy(
                bandLevels = currentLevels,
                currentPreset = "Custom" // Сбрасываем пресет на кастомный при ручном изменении
            )
            settingsRepository.setEqualizerSettings(newSettings)
        }
    }

    fun setPreampLevel(level: Short) {
        viewModelScope.launch {
            val newSettings = _uiState.value.equalizer.copy(preampLevel = level)
            settingsRepository.setEqualizerSettings(newSettings)
        }
    }

    fun setPreset(presetName: String) {
        viewModelScope.launch {
            val eq = audioEffectsManager.equalizer ?: return@launch
            if (presetName == "Custom") {
                // Не делаем ничего, пользователь будет настраивать сам
                val newSettings = _uiState.value.equalizer.copy(currentPreset = "Custom")
                settingsRepository.setEqualizerSettings(newSettings)
                return@launch
            }

            val presetIndex = (0 until eq.numberOfPresets).find {
                eq.getPresetName(it.toShort()) == presetName
            }

            if (presetIndex != null) {
                eq.usePreset(presetIndex.toShort())
                val newBandLevels = (0 until 10).associateWith { uiBandIndex ->
                    val hardwareBandIndex = bandIndexMapping[uiBandIndex]
                    if (hardwareBandIndex != null) {
                        eq.getBandLevel(hardwareBandIndex)
                    } else {
                        0
                    }
                }.mapValues { it.value.toShort() }

                val newSettings = _uiState.value.equalizer.copy(
                    bandLevels = newBandLevels,
                    preampLevel = 0, // Сбрасываем предусилитель при выборе пресета
                    currentPreset = presetName
                )
                settingsRepository.setEqualizerSettings(newSettings)
            }
        }
    }

    fun resetEqualizer() {
        viewModelScope.launch {
            val newSettings = _uiState.value.equalizer.copy(
                bandLevels = emptyMap(),
                preampLevel = 0,
                currentPreset = "Custom"
            )
            settingsRepository.setEqualizerSettings(newSettings)
        }
    }

    // --- Методы для BassBoost и Virtualizer ---
    fun setBassBoostEnabled(isEnabled: Boolean) = viewModelScope.launch {
        settingsRepository.setBassBoostSettings(_uiState.value.bassBoost.copy(isEnabled = isEnabled))
    }

    fun setBassBoostStrength(strength: Short) = viewModelScope.launch {
        settingsRepository.setBassBoostSettings(_uiState.value.bassBoost.copy(strength = strength))
    }

    fun setVirtualizerEnabled(isEnabled: Boolean) = viewModelScope.launch {
        settingsRepository.setVirtualizerSettings(_uiState.value.virtualizer.copy(isEnabled = isEnabled))
    }

    fun setVirtualizerStrength(strength: Short) = viewModelScope.launch {
        settingsRepository.setVirtualizerSettings(_uiState.value.virtualizer.copy(strength = strength))
    }

    // --- Логика мэппинга ---
    private fun mapUiToHardwareBands(hardwareFrequencies: List<Int>): Map<Int, Short> {
        return standardFrequencies.mapIndexed { uiIndex, uiFreq ->
            val closestHardwareBand = hardwareFrequencies.minByOrNull { abs(it - uiFreq * 1000) }
            val hardwareIndex = hardwareFrequencies.indexOf(closestHardwareBand).toShort()
            uiIndex to hardwareIndex
        }.toMap()
    }
}