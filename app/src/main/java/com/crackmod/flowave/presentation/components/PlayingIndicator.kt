package com.crackmod.flowave.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun PlayingIndicator(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    color: Color = MaterialTheme.colorScheme.primary
) {
    var animationTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                animationTrigger = !animationTrigger
                delay(700) // Средняя длительность одного "такта" анимации
            }
        }
    }

    val createBarAnimation: @Composable (targetValueWhenPlaying: Float, duration: Int) -> State<Float> = { targetValueWhenPlaying, duration ->
        animateFloatAsState(
            targetValue = if (isPlaying) (if (animationTrigger) targetValueWhenPlaying else 0.2f) else 0.2f,
            animationSpec = tween(durationMillis = duration),
            label = "bar_height"
        )
    }

    val height1 by createBarAnimation(1f, 600)
    val height2 by createBarAnimation(0.8f, 800)
    val height3 by createBarAnimation(1f, 700)
    val height4 by createBarAnimation(0.7f, 900)
    val height5 by createBarAnimation(0.9f, 500)

    Row(
        modifier = modifier.height(22.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Bar(height = height1, color = color)
        Bar(height = height2, color = color)
        Bar(height = height3, color = color)
        Bar(height = height4, color = color)
        Bar(height = height5, color = color)
    }
}

@Composable
private fun RowScope.Bar(height: Float, color: Color) {
    Box(
        modifier = Modifier
            .width(3.dp)
            .fillMaxHeight(height)
            .clip(RoundedCornerShape(2.dp))
            .background(color)
    )
}