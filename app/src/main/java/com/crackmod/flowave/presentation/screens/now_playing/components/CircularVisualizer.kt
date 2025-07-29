package com.crackmod.flowave.presentation.screens.now_playing.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun CircularVisualizer(
    progress: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onSeek: (Float) -> Unit,
    strokeWidth: Dp = 2.dp,
    visualizerWidth: Dp = 12.dp,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    var isDragging by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = if (isDragging) snap() else tween(1000, easing = LinearEasing),
        label = "progress_anim_visualizer"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "visualizer_transition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "visualizer_time"
    )

    // ИСПРАВЛЕНИЕ 1: Перемещаем `remember` в Composable-контекст
    val randomSeed = remember { Random.nextInt() }
    val amplitudes = remember(randomSeed) { List(80) { Random(randomSeed + it).nextFloat() } }


    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false },
                    onDrag = { change, _ ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val touch = change.position
                        val angleRad = atan2(touch.y - center.y, touch.x - center.x)
                        val angleDeg = (angleRad * (180f / PI.toFloat()) + 450f) % 360f
                        onSeek(angleDeg / 360f)
                        change.consume()
                    }
                )
            }
    ) {
        val strokePx = strokeWidth.toPx()
        val visualizerWidthPx = visualizerWidth.toPx()

        // Внешнее кольцо прогресса
        drawProgressRing(animatedProgress, strokePx, color)

        // Визуализатор (симуляция анализатора)
        drawSpectrumVisualizer(
            isPlaying = isPlaying,
            time = time,
            // Передаем готовый список вместо seed
            amplitudes = amplitudes,
            baseRadius = (size.minDimension - visualizerWidthPx * 2 - strokePx * 2) / 2,
            visualizerWidth = visualizerWidthPx,
            color = color
        )
    }
}

private fun DrawScope.drawProgressRing(
    progress: Float,
    strokeWidth: Float,
    color: Color
) {
    val radius = (size.minDimension - strokeWidth) / 2
    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
    val arcSize = Size(radius * 2, radius * 2)

    // Background track
    drawArc(
        color = Color.White.copy(alpha = 0.15f),
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        style = Stroke(width = strokeWidth),
        topLeft = topLeft,
        size = arcSize
    )

    // Progress track
    drawArc(
        color = color,
        startAngle = -90f,
        sweepAngle = 360 * progress,
        useCenter = false,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        topLeft = topLeft,
        size = arcSize
    )
}

// ИСПРАВЛЕНИЕ 2: Убираем `remember` и принимаем готовый список `amplitudes`
private fun DrawScope.drawSpectrumVisualizer(
    isPlaying: Boolean,
    time: Float,
    amplitudes: List<Float>,
    baseRadius: Float,
    visualizerWidth: Float,
    color: Color,
    barCount: Int = 80
) {
    val center = this.center

    for (i in 0 until barCount) {
        val angle = (i.toFloat() / barCount) * 2 * PI.toFloat()
        val cosA = cos(angle)
        val sinA = sin(angle)

        val amplitude = amplitudes.getOrElse(i) { 0f }
        val modulation = sin(time + angle * 4) * 0.1f + 0.9f
        val animatedAmplitude = if (isPlaying) (amplitude * modulation) else 0.0f
        val finalWidth = visualizerWidth * animatedAmplitude

        val startRadius = baseRadius + (visualizerWidth - finalWidth) / 2
        val endRadius = startRadius + finalWidth

        val start = Offset(center.x + startRadius * cosA, center.y + startRadius * sinA)
        val end = Offset(center.x + endRadius * cosA, center.y + endRadius * sinA)

        // ИСПРАВЛЕНИЕ 3: Добавляем суффикс 'f' к литералам
        val alpha = (animatedAmplitude * 0.8f + 0.2f).coerceIn(0.1f, 1.0f)

        drawLine(
            color = color.copy(alpha = alpha),
            start = start,
            end = end,
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}