package com.crackmod.flowave.presentation.screens.now_playing.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

private data class NebulaAnimationParams(
    val duration: Int,
    val initialX: Float,
    val targetX: Float,
    val initialY: Float,
    val targetY: Float,
    val initialRadius: Float,
    val targetRadius: Float
)

@Composable
fun NebulaBackground(colors: List<Color>, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "nebula_transition")

    var animationParams by remember { mutableStateOf<List<NebulaAnimationParams>>(emptyList()) }

    LaunchedEffect(colors) {
        animationParams = colors.map {
            NebulaAnimationParams(
                duration = Random.nextInt(20000, 30000),
                initialX = Random.nextFloat(),
                targetX = Random.nextFloat(),
                initialY = Random.nextFloat(),
                targetY = Random.nextFloat(),
                initialRadius = Random.nextFloat() * 0.4f + 0.8f, // from 0.8 to 1.2
                targetRadius = Random.nextFloat() * 0.4f + 0.8f
            )
        }
    }

    val animatedValues = animationParams.mapIndexed { index, params ->
        val x by infiniteTransition.animateFloat(
            initialValue = params.initialX,
            targetValue = params.targetX,
            animationSpec = infiniteRepeatable(
                animation = tween(params.duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "nebula_x_$index"
        )
        val y by infiniteTransition.animateFloat(
            initialValue = params.initialY,
            targetValue = params.targetY,
            animationSpec = infiniteRepeatable(
                animation = tween(params.duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "nebula_y_$index"
        )
        val radiusFactor by infiniteTransition.animateFloat(
            initialValue = params.initialRadius,
            targetValue = params.targetRadius,
            animationSpec = infiniteRepeatable(
                animation = tween(params.duration / 2, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "nebula_radius_$index"
        )
        Triple(x, y, radiusFactor)
    }

    val stars = remember {
        List(70) {
            Triple(
                Offset(Random.nextFloat(), Random.nextFloat()),
                Random.nextFloat() * 2f + 1f,
                Random.nextFloat() * 0.6f + 0.2f
            )
        }
    }

    Box(modifier = modifier.background(Color.Black)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            stars.forEach { (offset, radius, alpha) ->
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(offset.x * canvasWidth, offset.y * canvasHeight)
                )
            }

            animatedValues.forEachIndexed { index, (x, y, radiusFactor) ->
                val color = colors.getOrElse(index) { Color.White }
                val brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(x = canvasWidth * x, y = canvasHeight * y),
                    radius = (canvasWidth.coerceAtLeast(canvasHeight) * 0.7f) * radiusFactor
                )
                drawRect(brush)
            }
        }
    }
}