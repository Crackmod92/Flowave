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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun NebulaProgressIndicator(
    position: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    formatTime: (Long) -> String,
    accentColor: Color
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableStateOf(0f) }

    val progress = if (isDragging) {
        dragPosition
    } else {
        (position.toFloat() / duration.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = if (isDragging) tween(0) else tween(1000, easing = LinearEasing),
        label = "progress_animation_nebula_canvas"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .pointerInput(duration) {
                    detectTapGestures { offset ->
                        val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek((newProgress * duration).toLong())
                    }
                }
                .pointerInput(duration) {
                    detectHorizontalDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = {
                            isDragging = false
                            onSeek((dragPosition * duration).toLong())
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            dragPosition = (change.position.x / size.width).coerceIn(0f, 1f)
                        }
                    )
                }
        ) {
            val trackHeight = 8.dp.toPx()
            val trackY = center.y - trackHeight / 2
            val thumbRadius = 10.dp.toPx()
            val thumbX = size.width * animatedProgress
            val thumbY = center.y

            val inactiveColor = if (accentColor.luminance() > 0.5f) {
                Color.Black.copy(alpha = 0.3f)
            } else {
                Color.White.copy(alpha = 0.3f)
            }

            // Inactive track
            drawRoundRect(
                color = inactiveColor,
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
                    center = Offset(thumbX, thumbY),
                    radius = thumbRadius * 2.5f
                ),
                radius = thumbRadius * 2.5f,
                center = Offset(thumbX, thumbY)
            )

            // Thumb circle
            drawCircle(
                color = accentColor,
                radius = thumbRadius,
                center = Offset(thumbX, thumbY)
            )

            // Thumb inner dot
            drawCircle(
                color = if (accentColor.luminance() > 0.5f) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.7f),
                radius = thumbRadius / 3,
                center = Offset(thumbX, thumbY)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val displayPosition = if (isDragging) (dragPosition * duration).toLong() else position
            Text(
                formatTime(displayPosition),
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