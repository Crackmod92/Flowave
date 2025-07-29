package com.crackmod.flowave.presentation.screens.now_playing.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange

@Composable
fun AlbumArtWithGestures(
    modifier: Modifier = Modifier,
    onSwipeNext: () -> Unit,
    onSwipePrevious: () -> Unit,
    key: Any?,
    content: @Composable BoxScope.() -> Unit
) {
    var dragAmount by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .pointerInput(key) {
                detectHorizontalDragGestures(
                    onDragStart = { dragAmount = 0f },
                    onDragEnd = {
                        val dragThreshold = size.width / 4
                        when {
                            dragAmount < -dragThreshold -> onSwipeNext()
                            dragAmount > dragThreshold -> onSwipePrevious()
                        }
                    },
                    onHorizontalDrag = { change, drag ->
                        if (change.positionChange() != androidx.compose.ui.geometry.Offset.Zero) change.consume()
                        dragAmount += drag
                    }
                )
            },
        contentAlignment = Alignment.Center,
        content = content
    )
}