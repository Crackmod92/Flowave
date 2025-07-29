// ПУТЬ: com/crackmod/flowave/presentation/screens/now_playing/components/NebulaPlayerControls.kt
// КОД:

package com.crackmod.flowave.presentation.screens.now_playing.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.crackmod.flowave.R

@Composable
fun NebulaPlayerControls(
    isPlaying: Boolean,
    shuffleModeEnabled: Boolean,
    repeatMode: Int,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color,
    contentColor: Color
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Кнопка SHUFFLE ---
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleShuffle()
                    })
        ) {
            Icon(
                painter = painterResource(id = R.drawable.nebula_ic_shuffle),
                contentDescription = "Перемешать",
                tint = if (shuffleModeEnabled) accentColor else contentColor.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.Center).size(50.dp)
            )
        }

        // --- Кнопка PREVIOUS ---
        Box(
            modifier = Modifier
                .size(56.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSkipPrevious()
                    })
        ) {
            Icon(
                painter = painterResource(id = R.drawable.nebula_ic_previous),
                contentDescription = "Предыдущий",
                tint = contentColor,
                modifier = Modifier.align(Alignment.Center).size(30.dp)
            )
        }

        // --- Кнопка PLAY/PAUSE ---
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(12.dp, CircleShape, spotColor = accentColor)
                .clip(CircleShape)
                .background(accentColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onTogglePlayPause()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            val iconRes = if (isPlaying) R.drawable.nebula_ic_pause else R.drawable.nebula_ic_play
            val iconColor = if (accentColor.luminance() > 0.5f) Color.Black else Color.White
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = "Воспроизвести/Пауза",
                tint = iconColor,
                modifier = Modifier.size(100.dp)
            )
        }

        // --- Кнопка NEXT ---
        Box(
            modifier = Modifier
                .size(56.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSkipNext()
                    })
        ) {
            Icon(
                painter = painterResource(id = R.drawable.nebula_ic_next),
                contentDescription = "Следующий",
                tint = contentColor,
                modifier = Modifier.align(Alignment.Center).size(30.dp)
            )
        }

        // --- Кнопка REPEAT ---
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleRepeat()
                    })
        ) {
            val iconRes = when (repeatMode) {
                Player.REPEAT_MODE_ONE -> R.drawable.nebula_ic_repeat_one
                Player.REPEAT_MODE_ALL -> R.drawable.nebula_ic_repeat
                else -> R.drawable.nebula_ic_repeat // Используем одну и ту же иконку для ALL и OFF
            }
            val color = if (repeatMode != Player.REPEAT_MODE_OFF) accentColor else contentColor.copy(alpha = 0.5f)
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = "Повторить",
                tint = color,
                modifier = Modifier.align(Alignment.Center).size(50.dp)
            )
        }
    }
}