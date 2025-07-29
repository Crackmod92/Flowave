package com.crackmod.flowave.presentation.screens.now_playing.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun GlitchLineProgressIndicator(
    position: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    formatTime: (Long) -> String,
    isPlaying: Boolean,
    accentColor1: Color,
    accentColor2: Color,
    textColor: Color
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableStateOf(position) }

    val animatedPosition by animateFloatAsState(
        targetValue = if (isDragging) dragPosition.toFloat() else position.toFloat(),
        animationSpec = if (isPlaying && !isDragging) tween(1000, easing = LinearEasing) else snap(),
        label = "crt_progress_animation"
    )

    val progress = (animatedPosition / duration.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
    val monospaceFont = FontFamily.Monospace

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            isDragging = true
                            dragPosition = (offset.x / size.width * duration).toLong()
                            tryAwaitRelease()
                            onSeek(dragPosition)
                            isDragging = false
                        }
                    )
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val lineY = canvasHeight / 2f

            drawLine(
                color = textColor.copy(alpha = 0.2f),
                start = Offset(0f, lineY),
                end = Offset(canvasWidth, lineY),
                strokeWidth = 2f
            )

            val progressX = canvasWidth * progress

            // Main progress line with a slight "glow"
            drawLine(
                color = accentColor1.copy(alpha = 0.5f),
                start = Offset(0f, lineY),
                end = Offset(progressX, lineY),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = accentColor1,
                start = Offset(0f, lineY),
                end = Offset(progressX, lineY),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )

            // Constant subtle distortion on the progress line
            if (progress > 0) {
                val distortionAmount = progressX * 0.05f
                drawLine(
                    color = accentColor2.copy(alpha = 0.7f),
                    start = Offset(progressX - distortionAmount, lineY - 2f),
                    end = Offset(progressX, lineY - 2f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.8f),
                    start = Offset(progressX - distortionAmount * 0.5f, lineY + 2f),
                    end = Offset(progressX, lineY + 2f),
                    strokeWidth = 1f
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "STREAM_TIME: ${formatTime(if (isDragging) dragPosition else position)}",
                fontFamily = monospaceFont,
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.7f)
            )
            Text(
                text = "STREAM_DUR: ${formatTime(duration)}",
                fontFamily = monospaceFont,
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.7f)
            )
        }
    }
}