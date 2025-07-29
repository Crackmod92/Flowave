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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun AuroraProgressIndicator(
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
        label = "progress_animation_aurora"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .pointerInput(duration) {
                    detectTapGestures { offset ->
                        if (duration > 0) {
                            val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeek((newProgress * duration).toLong())
                        }
                    }
                }
        ) {
            val trackHeight = 4.dp.toPx()
            val trackY = center.y - trackHeight / 2
            val thumbRadius = 8.dp.toPx()
            val thumbX = size.width * animatedProgress

            // Inactive track
            drawRoundRect(
                color = Color.White.copy(alpha = 0.2f),
                topLeft = Offset(0f, trackY),
                size = Size(width = size.width, height = trackHeight),
                cornerRadius = CornerRadius(trackHeight)
            )

            // Active track
            if (animatedProgress > 0) {
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(0f, trackY),
                    size = Size(width = thumbX, height = trackHeight),
                    cornerRadius = CornerRadius(trackHeight)
                )
            }

            // Thumb glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accentColor.copy(alpha = 0.4f), Color.Transparent),
                    center = Offset(thumbX, center.y),
                    radius = thumbRadius * 2.5f
                ),
                radius = thumbRadius * 2.5f,
                center = Offset(thumbX, center.y)
            )

            // Thumb circle
            drawCircle(
                color = accentColor,
                radius = thumbRadius,
                center = Offset(thumbX, center.y)
            )
        }

        Spacer(Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                formatTime(position),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                formatTime(duration),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}