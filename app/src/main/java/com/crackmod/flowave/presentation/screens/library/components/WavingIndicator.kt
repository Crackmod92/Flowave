package com.crackmod.flowave.presentation.screens.home.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun WavingIndicator(
    isRefreshing: Boolean,
    pullProgress: Float,
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.primary

    // Анимация для "течения" волны во время обновления
    val infiniteTransition = rememberInfiniteTransition(label = "wave_phase_transition")
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        val width = size.width
        val height = size.height

        // Амплитуда волны зависит от прогресса вытягивания
        val amplitude = (height / 2) * pullProgress

        val path = Path()
        path.moveTo(0f, height / 2)

        val steps = 100
        for (i in 0..steps) {
            val x = (width / steps) * i
            val angle = (x / width) * 4 * Math.PI.toFloat() // 2 полные волны по ширине

            // Если идет обновление, сдвигаем фазу для анимации "течения"
            val phase = if (isRefreshing) phaseShift else 0f

            val y = height / 2 + amplitude * sin(angle + phase)
            path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 4.dp.toPx())
        )
    }
}