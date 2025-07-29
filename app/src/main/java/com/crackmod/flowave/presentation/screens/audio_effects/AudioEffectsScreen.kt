// ПУТЬ: /app/src/main/java/com/crackmod/flowave/presentation/screens/audio_effects/AudioEffectsScreen.kt
package com.crackmod.flowave.presentation.screens.audio_effects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crackmod.flowave.presentation.components.FlowaveTopAppBar
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioEffectsScreen(
    onBackPress: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    viewModel: AudioEffectsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            FlowaveTopAppBar(
                title = "Аудиоэффекты",
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.equalizerProperties.hasEqualizer) {
                val eqSubtitle = if (uiState.equalizer.isEnabled) {
                    "Включен • ${uiState.equalizer.currentPreset}"
                } else {
                    "Выключен"
                }

                EffectCard(
                    title = "Эквалайзер",
                    subtitle = eqSubtitle,
                    isEnabled = uiState.equalizer.isEnabled,
                    onEnabledChange = viewModel::setEqualizerEnabled,
                    onClick = onNavigateToEqualizer
                )
            } else {
                Card {
                    Text(
                        "Эквалайзер не поддерживается на вашем устройстве.",
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            EffectCard(
                title = "Усиление басов",
                isEnabled = uiState.bassBoost.isEnabled,
                onEnabledChange = viewModel::setBassBoostEnabled
            ) {
                SliderWithValue(
                    value = uiState.bassBoost.strength.toFloat(),
                    onValueChange = { viewModel.setBassBoostStrength(it.roundToInt().toShort()) },
                    valueRange = 0f..1000f,
                    enabled = uiState.bassBoost.isEnabled
                )
            }

            EffectCard(
                title = "Объемный звук",
                isEnabled = uiState.virtualizer.isEnabled,
                onEnabledChange = viewModel::setVirtualizerEnabled
            ) {
                SliderWithValue(
                    value = uiState.virtualizer.strength.toFloat(),
                    onValueChange = { viewModel.setVirtualizerStrength(it.roundToInt().toShort()) },
                    valueRange = 0f..1000f,
                    enabled = uiState.virtualizer.isEnabled
                )
            }
        }
    }
}

@Composable
private fun EffectCard(
    title: String,
    isEnabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    val cardModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = cardModifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleLarge)
                    if (subtitle != null) {
                        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (onClick != null) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Перейти к настройке")
                } else {
                    Switch(checked = isEnabled, onCheckedChange = onEnabledChange)
                }
            }
            if (content != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun SliderWithValue(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = 99,
            enabled = enabled,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = (value / 100).roundToInt().toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}