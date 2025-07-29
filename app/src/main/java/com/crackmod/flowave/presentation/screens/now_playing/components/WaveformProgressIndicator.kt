package com.crackmod.flowave.presentation.screens.now_playing.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun WaveformProgressIndicator(
    position: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    formatTime: (Long) -> String,
    modifier: Modifier = Modifier,
    trackId: Long,
    barCount: Int = 100,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
) {
    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableStateOf(0f) }

    val amplitudes = remember(trackId) {
        List(barCount) { Random.nextFloat().coerceIn(0.1f, 1.0f) }
    }

    val progressValue = if (duration > 0) {
        position.toFloat() / duration.toFloat()
    } else {
        0f
    }

    val currentProgress = if (isSeeking) {
        seekPosition
    } else {
        progressValue
    }.coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = currentProgress,
        animationSpec = if (isSeeking) tween(0) else tween(1000, easing = LinearEasing),
        label = "waveform_progress_anim"
    )

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .pointerInput(trackId, duration) {
                    if (duration > 0) { // Allow gestures only if duration is valid
                        detectTapGestures { offset ->
                            val progress = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeek((progress * duration).toLong())
                        }
                    }
                }
                .pointerInput(trackId, duration) {
                    if (duration > 0) { // Allow gestures only if duration is valid
                        detectHorizontalDragGestures(
                            onDragStart = { isSeeking = true },
                            onDragEnd = {
                                isSeeking = false
                                onSeek((seekPosition * duration).toLong())
                            },
                            onHorizontalDrag = { change, _ ->
                                change.consume()
                                seekPosition = (change.position.x / size.width).coerceIn(0f, 1f)
                            }
                        )
                    }
                }
        ) {
            val barWidth = size.width / (barCount * 2 - 1)
            val gapWidth = barWidth

            val activeBars = (animatedProgress * barCount).toInt()

            for (i in 0 until barCount) {
                val barHeight = amplitudes[i] * size.height
                val startY = (size.height - barHeight) / 2
                val startX = i * (barWidth + gapWidth)

                drawRoundRect(
                    color = if (i < activeBars) activeColor else inactiveColor,
                    topLeft = Offset(startX, startY),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val displayPosition = if (isSeeking) (seekPosition * duration).toLong() else position
            Text(formatTime(displayPosition), style = MaterialTheme.typography.labelSmall)
            Text(formatTime(duration), style = MaterialTheme.typography.labelSmall)
        }
    }
}