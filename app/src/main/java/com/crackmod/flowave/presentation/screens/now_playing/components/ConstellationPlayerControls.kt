package com.crackmod.flowave.presentation.screens.now_playing.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ConstellationPlayerControls(
    isPlaying: Boolean,
    shuffleModeEnabled: Boolean,
    repeatMode: Int,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HexagonButton(
            onClick = onToggleShuffle,
            isActive = shuffleModeEnabled,
            icon = Icons.Default.Shuffle,
            contentDescription = "Перемешать",
            accentColor = accentColor,
            modifier = Modifier.size(52.dp)
        )

        HexagonButton(
            onClick = onSkipPrevious,
            icon = Icons.Default.SkipPrevious,
            contentDescription = "Предыдущий",
            accentColor = accentColor,
            modifier = Modifier.size(60.dp)
        )

        HexagonButton(
            onClick = onTogglePlayPause,
            isActive = true, // Main button is always "active"
            icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = "Воспроизвести/Пауза",
            accentColor = accentColor,
            modifier = Modifier.size(76.dp)
        )

        HexagonButton(
            onClick = onSkipNext,
            icon = Icons.Default.SkipNext,
            contentDescription = "Следующий",
            accentColor = accentColor,
            modifier = Modifier.size(60.dp)
        )

        HexagonButton(
            onClick = onToggleRepeat,
            isActive = repeatMode != Player.REPEAT_MODE_OFF,
            icon = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
            contentDescription = "Повторить",
            accentColor = accentColor,
            modifier = Modifier.size(52.dp)
        )
    }
}

@Composable
private fun HexagonButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isActive: Boolean = false // Default to false for skip buttons
) {
    val haptic = LocalHapticFeedback.current

    IconButton(
        onClick = {
            val hapticType = if (icon == Icons.Default.PlayArrow || icon == Icons.Default.Pause) {
                HapticFeedbackType.TextHandleMove
            } else {
                HapticFeedbackType.LongPress
            }
            haptic.performHapticFeedback(hapticType)
            onClick()
        },
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val hexagonPath = createHexagonPath(size)
            val strokeColor = if (isActive) accentColor else Color.White.copy(alpha = 0.5f)
            val iconColor = if (isActive) accentColor else Color.White

            // Draw hexagon border
            drawPath(
                path = hexagonPath,
                color = strokeColor,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // We draw the icon on top of the Canvas using a standard Icon component
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) accentColor else Color.White,
            modifier = Modifier.size(icon.defaultWidth * 0.8f) // Scale icon to fit
        )
    }
}


private fun createHexagonPath(size: Size): Path {
    return Path().apply {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val angle = 2.0 * PI / 6
        moveTo(
            x = center.x + radius * cos(0.0).toFloat(),
            y = center.y + radius * sin(0.0).toFloat()
        )
        for (i in 1..6) {
            lineTo(
                x = center.x + radius * cos(angle * i).toFloat(),
                y = center.y + radius * sin(angle * i).toFloat()
            )
        }
        close()
    }
}