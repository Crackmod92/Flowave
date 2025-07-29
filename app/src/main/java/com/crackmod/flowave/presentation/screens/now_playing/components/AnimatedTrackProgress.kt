package com.crackmod.flowave.presentation.screens.now_playing.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun AnimatedTrackProgress(
    position: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    formatTime: (Long) -> String,
    isPlaying: Boolean,
    // НОВЫЙ ПАРАМЕТР
    forceDarkThemeColors: Boolean = false
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableStateOf(position) }

    val animatedPosition by animateFloatAsState(
        targetValue = if (isDragging) dragPosition.toFloat() else position.toFloat(),
        animationSpec = if (isPlaying && !isDragging) {
            tween(1000, easing = LinearEasing)
        } else {
            snap()
        },
        label = "progress_animation"
    )

    // ИЗМЕНЕНИЕ: Логика выбора цвета
    val textColor = if (forceDarkThemeColors) {
        Color.White.copy(alpha = 0.8f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    }

    val inactiveTrackColor = if (forceDarkThemeColors) {
        Color.White.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = animatedPosition,
            onValueChange = {
                dragPosition = it.toLong()
                isDragging = true
            },
            onValueChangeFinished = {
                onSeek(dragPosition)
                isDragging = false
            },
            valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = inactiveTrackColor // Используем нашу переменную
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                formatTime(if (isDragging) dragPosition else position),
                style = MaterialTheme.typography.bodySmall,
                color = textColor // Используем нашу переменную
            )
            Text(
                formatTime(duration),
                style = MaterialTheme.typography.bodySmall,
                color = textColor // Используем нашу переменную
            )
        }
    }
}