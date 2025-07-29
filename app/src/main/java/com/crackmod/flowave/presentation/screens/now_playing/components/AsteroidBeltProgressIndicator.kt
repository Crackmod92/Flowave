package com.crackmod.flowave.presentation.screens.now_playing.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AsteroidBeltProgressIndicator(
    position: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    formatTime: (Long) -> String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val progress = (position.toFloat() / duration.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = LinearEasing),
        label = "progress_asteroid_scanner"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .pointerInput(duration) {
                    detectTapGestures { offset ->
                        if (duration > 0) {
                            val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeek((newProgress * duration).toLong())
                        }
                    }
                }
        ) {
            val trackHeight = 8.dp.toPx()
            val yCenter = center.y

            // Background track
            drawLine(
                color = Color.Black.copy(alpha = 0.3f),
                start = Offset(0f, yCenter),
                end = Offset(size.width, yCenter),
                strokeWidth = trackHeight,
                cap = StrokeCap.Round
            )

            // Scanning Line with Gradient
            val progressX = size.width * animatedProgress
            val scannerWidth = 80.dp.toPx()

            val scannerBrush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, accentColor.copy(alpha = 0.1f), accentColor, accentColor.copy(alpha = 0.1f), Color.Transparent),
                startX = progressX - scannerWidth,
                endX = progressX + scannerWidth
            )

            // Draw gradient behind the line
            drawLine(
                brush = scannerBrush,
                start = Offset(0f, yCenter),
                end = Offset(size.width, yCenter),
                strokeWidth = trackHeight * 2, // Wider glow
                cap = StrokeCap.Round
            )

            // Draw bright scanner line
            drawLine(
                color = Color.White,
                start = Offset(progressX, yCenter - trackHeight / 2),
                end = Offset(progressX, yCenter + trackHeight / 2),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                formatTime(position),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                formatTime(duration),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}