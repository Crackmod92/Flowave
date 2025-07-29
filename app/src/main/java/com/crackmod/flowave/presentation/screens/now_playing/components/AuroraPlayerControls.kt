package com.crackmod.flowave.presentation.screens.now_playing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.crackmod.flowave.R

@Composable
fun AuroraPlayerControls(
    isPlaying: Boolean,
    shuffleModeEnabled: Boolean,
    repeatMode: Int,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color
) {
    val haptic = LocalHapticFeedback.current
    val contentColor = Color.White.copy(alpha = 0.8f)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Кнопка SHUFFLE ---
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleShuffle()
            },
            modifier = Modifier.size(48.dp)
        ) {
            val iconRes = if (shuffleModeEnabled) R.drawable.aurora_shuffle else R.drawable.aurora_shuffle
            val color = if (shuffleModeEnabled) accentColor else contentColor
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = "Перемешать",
                tint = color,
                modifier = Modifier
                    .size(30.dp)
            )
        }

        // --- Кнопка PREVIOUS ---
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSkipPrevious()
            },
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.aurora_previous),
                contentDescription = "Предыдущий",
                tint = contentColor,
                modifier = Modifier.size(35.dp)
            )
        }

        // --- Кнопка PLAY/PAUSE ---
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onTogglePlayPause()
            },
            modifier = Modifier.size(50.dp)
        ) {
            val iconRes = if (isPlaying) R.drawable.aurora_pause else R.drawable.aurora_play
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = "Воспроизвести/Пауза",
                tint = accentColor,
                modifier = Modifier
                    .size(75.dp)
            )
        }

        // --- Кнопка NEXT ---
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSkipNext()
            },
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.aurora_next),
                contentDescription = "Следующий",
                tint = contentColor,
                modifier = Modifier.size(35.dp)
            )
        }

        // --- Кнопка REPEAT ---
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleRepeat()
            },
            modifier = Modifier.size(48.dp)
        ) {
            val iconRes = when (repeatMode) {
                Player.REPEAT_MODE_ONE -> R.drawable.aurora_repeat_one
                Player.REPEAT_MODE_ALL -> R.drawable.aurora_repeat
                else -> R.drawable.aurora_repeat
            }
            val isActive = repeatMode != Player.REPEAT_MODE_OFF
            val color = if (isActive) accentColor else contentColor

            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = "Повторить",
                tint = color,
                modifier = Modifier
                    .size(30.dp)
            )
        }
    }
}