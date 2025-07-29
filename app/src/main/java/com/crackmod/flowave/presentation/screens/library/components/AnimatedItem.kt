package com.crackmod.flowave.presentation.screens.library.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.animateItemAppearance(index: Int): Modifier = composed {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "item_alpha_animation"
    )

    val translationY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 25f, // Начинаем на 25 пикселей ниже
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "item_translationY_animation"
    )

    this.graphicsLayer {
        this.alpha = alpha
        this.translationY = translationY
    }
}