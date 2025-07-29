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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun GalacticCoreProgressIndicator(
    position: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    formatTime: (Long) -> String,
    modifier: Modifier = Modifier,
    accentColor: Color,
    baseColor: Color
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
        label = "progress_anim_galaxy"
    )

    val goldGradient = Brush.horizontalGradient(
        colors = listOf(accentColor.copy(alpha = 0.6f), accentColor, accentColor.copy(alpha = 0.6f))
    )

    Column(modifier = modifier.fillMaxWidth()) {
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
            val trackHeight = 6.dp.toPx()
            val trackY = center.y - trackHeight / 2
            val progressWidth = size.width * animatedProgress

            // Inactive track
            drawRect(
                color = baseColor.copy(alpha = 0.5f),
                topLeft = Offset(0f, trackY),
                size = Size(width = size.width, height = trackHeight),
            )
            drawRect(
                color = accentColor.copy(alpha = 0.3f),
                topLeft = Offset(0f, trackY),
                size = Size(width = size.width, height = trackHeight),
                style = Stroke(width = 1.dp.toPx())
            )

            // Active track
            if (animatedProgress > 0) {
                drawRect(
                    brush = goldGradient,
                    topLeft = Offset(0f, trackY),
                    size = Size(width = progressWidth, height = trackHeight),
                )
            }

            // Thumb
            val thumbSize = 14.dp.toPx()
            val thumbX = progressWidth - (thumbSize / 2)
            val thumbY = center.y - (thumbSize / 2)

            drawRect(
                brush = goldGradient,
                topLeft = Offset(thumbX, thumbY),
                size = Size(thumbSize, thumbSize),
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                topLeft = Offset(thumbX, thumbY),
                size = Size(thumbSize, thumbSize),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                formatTime(if (isDragging) (dragPosition * duration).toLong() else position),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                formatTime(duration),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}