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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ConstellationProgressIndicator(
    position: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    formatTime: (Long) -> String,
    accentColor: Color
) {
    val progress = (position.toFloat() / duration.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = LinearEasing),
        label = "progress_constellation_comet"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .pointerInput(duration) {
                    detectTapGestures { offset ->
                        val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek((newProgress * duration).toLong())
                    }
                }
        ) {
            val trackHeight = 2.dp.toPx()
            val yCenter = center.y

            // Inactive track
            drawLine(
                color = Color.White.copy(alpha = 0.2f),
                start = Offset(0f, yCenter),
                end = Offset(size.width, yCenter),
                strokeWidth = trackHeight,
                cap = StrokeCap.Round
            )

            // Active track
            val progressX = size.width * animatedProgress
            if (animatedProgress > 0) {
                drawLine(
                    color = accentColor,
                    start = Offset(0f, yCenter),
                    end = Offset(progressX, yCenter),
                    strokeWidth = trackHeight,
                    cap = StrokeCap.Round
                )
            }

            // Comet (Thumb)
            val cometPosition = Offset(progressX, yCenter)

            // Comet Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accentColor.copy(alpha = 0.5f), Color.Transparent),
                    center = cometPosition,
                    radius = 16.dp.toPx()
                ),
                radius = 16.dp.toPx(),
                center = cometPosition
            )
            // Comet Core
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = cometPosition
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
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            Text(
                formatTime(duration),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}