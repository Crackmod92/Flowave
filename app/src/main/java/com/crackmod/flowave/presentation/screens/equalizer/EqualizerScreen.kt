// ПУТЬ: /app/src/main/java/com/crackmod/flowave/presentation/screens/equalizer/EqualizerScreen.kt
package com.crackmod.flowave.presentation.screens.equalizer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crackmod.flowave.presentation.components.FlowaveTopAppBar
import com.crackmod.flowave.presentation.screens.audio_effects.AudioEffectsViewModel
import com.crackmod.flowave.presentation.screens.audio_effects.standardFrequencies
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    onBackPress: () -> Unit,
    viewModel: AudioEffectsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val eqProps = uiState.equalizerProperties
    val eqSettings = uiState.equalizer

    var presetsMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            FlowaveTopAppBar(
                title = "Эквалайзер",
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    Switch(
                        checked = eqSettings.isEnabled,
                        onCheckedChange = viewModel::setEqualizerEnabled
                    )
                    IconButton(onClick = viewModel::resetEqualizer, enabled = eqSettings.isEnabled) {
                        Icon(Icons.Default.Refresh, contentDescription = "Сбросить")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ExposedDropdownMenuBox(
                expanded = presetsMenuExpanded,
                onExpandedChange = { if (eqSettings.isEnabled) presetsMenuExpanded = it },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = eqSettings.currentPreset,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Пресет") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = presetsMenuExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    enabled = eqSettings.isEnabled
                )
                ExposedDropdownMenu(
                    expanded = presetsMenuExpanded,
                    onDismissRequest = { presetsMenuExpanded = false }
                ) {
                    eqProps.presets.forEach { preset ->
                        DropdownMenuItem(
                            text = { Text(preset) },
                            onClick = {
                                viewModel.setPreset(preset)
                                presetsMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                standardFrequencies.forEachIndexed { index, freqHz ->
                    val level = eqSettings.bandLevels[index] ?: 0
                    EqualizerBand(
                        level = level,
                        minLevel = eqProps.minBandLevel,
                        maxLevel = eqProps.maxBandLevel,
                        frequencyLabel = formatFrequency(freqHz),
                        onLevelChangeFinished = { newLevel -> viewModel.setBandLevel(index, newLevel) },
                        enabled = eqSettings.isEnabled,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Column(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)) {
                var tempPreampLevel by remember { mutableFloatStateOf(eqSettings.preampLevel.toFloat()) }
                LaunchedEffect(eqSettings.preampLevel) {
                    tempPreampLevel = eqSettings.preampLevel.toFloat()
                }

                val preampDb = (tempPreampLevel / 100.0).toFloat()
                Text(
                    text = "Предусилитель: ${String.format(Locale.getDefault(), "%.1f", preampDb)} дБ",
                    style = MaterialTheme.typography.labelLarge
                )
                Slider(
                    value = tempPreampLevel,
                    onValueChange = { tempPreampLevel = it },
                    onValueChangeFinished = { viewModel.setPreampLevel(tempPreampLevel.roundToInt().toShort()) },
                    valueRange = eqProps.minBandLevel.toFloat()..eqProps.maxBandLevel.toFloat(),
                    enabled = eqSettings.isEnabled
                )
            }
        }
    }
}

@Composable
private fun RowScope.EqualizerBand(
    level: Short,
    minLevel: Short,
    maxLevel: Short,
    frequencyLabel: String,
    onLevelChangeFinished: (Short) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var tempLevel by remember { mutableFloatStateOf(level.toFloat()) }

    LaunchedEffect(level) {
        tempLevel = level.toFloat()
    }

    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val levelDb = (tempLevel / 100.0).roundToInt()
        Text(
            text = "$levelDb",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        VerticalSlider(
            value = tempLevel,
            onValueChange = { tempLevel = it },
            onValueChangeFinished = { onLevelChangeFinished(tempLevel.roundToInt().toShort()) },
            valueRange = minLevel.toFloat()..maxLevel.toFloat(),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            enabled = enabled
        )

        Text(
            text = frequencyLabel,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun VerticalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors()
) {
    Layout(
        modifier = modifier,
        content = {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps,
                enabled = enabled,
                onValueChangeFinished = onValueChangeFinished,
                colors = colors
            )
        }
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(
            Constraints.fixed(
                width = constraints.maxHeight,
                height = constraints.maxWidth
            )
        )
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.placeRelativeWithLayer(
                x = -(placeable.width - constraints.maxWidth) / 2,
                y = -(placeable.height - constraints.maxHeight) / 2,
            ) {
                rotationZ = -90f
            }
        }
    }
}

private fun formatFrequency(freqHz: Int): String {
    return if (freqHz >= 1000) {
        "${freqHz / 1000}k"
    } else {
        freqHz.toString()
    }
}