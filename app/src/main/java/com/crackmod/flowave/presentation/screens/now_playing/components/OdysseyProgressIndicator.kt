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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OdysseyProgressIndicator(
    position: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    formatTime: (Long) -> String,
    modifier: Modifier = Modifier,
    segments: Int = 48,
    gapWidth: Dp = 2.dp,
    activeColor: Color,
    inactiveColor: Color,
    textColor: Color
) {
    val progress = (position.toFloat() / duration.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = LinearEasing),
        label = "progress_anim_odyssey"
    )

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val seekFraction = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek((duration * seekFraction).toLong())
                    }
                }
        ) {
            val gapPx = gapWidth.toPx()
            val totalGap = gapPx * (segments - 1)
            val segmentWidth = (size.width - totalGap) / segments
            val segmentHeight = size.height

            val activeSegments = (animatedProgress * segments)

            for (i in 0 until segments) {
                val color = if (i < activeSegments) activeColor else inactiveColor
                val start = i * (segmentWidth + gapPx)
                drawRoundRect(
                    color = color,
                    topLeft = Offset(start, 0f),
                    size = Size(segmentWidth, segmentHeight),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatTime(position), fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = textColor)
            Text(formatTime(duration), fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = textColor)
        }
    }
}