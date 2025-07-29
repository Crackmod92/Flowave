// ПУТЬ: com/crackmod/flowave/presentation/screens/now_playing/components/PulsarPlayerControls.kt
// КОД:

package com.crackmod.flowave.presentation.screens.now_playing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.crackmod.flowave.R

@Composable
fun PulsarPlayerControls(
    isPlaying: Boolean,
    shuffleModeEnabled: Boolean,
    repeatMode: Int,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Кнопка SHUFFLE ---
        IconButton(onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onToggleShuffle()
        }) {
            // ИЗМЕНЕНИЕ 1: Добавляем логику для цвета и иконки
            val shuffleIcon = if (shuffleModeEnabled) R.drawable.pulsar_ic_shuffle else R.drawable.pulsar_ic_shuffle
            val shuffleColor = if (shuffleModeEnabled) activeColor else inactiveColor.copy(alpha = 0.5f)

            Icon(
                painter = painterResource(id = shuffleIcon),
                contentDescription = "Shuffle",
                modifier = Modifier.size(35.dp),
                tint = shuffleColor
            )
        }

        // --- Кнопка PREVIOUS ---
        IconButton(onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onSkipPrevious()
        }) {
            Icon(
                painter = painterResource(id = R.drawable.pulsar_ic_previous),
                contentDescription = "Previous",
                modifier = Modifier.size(40.dp),
                tint = inactiveColor // Эта кнопка всегда неактивного цвета
            )
        }

        // --- Кнопка PLAY/PAUSE ---
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onTogglePlayPause()
            },
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            val iconRes = if (isPlaying) R.drawable.pulsar_ic_pause else R.drawable.pulsar_ic_play
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = "Play/Pause",
                modifier = Modifier.size(75.dp),
            )
        }

        // --- Кнопка NEXT ---
        IconButton(onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onSkipNext()
        }) {
            Icon(
                painter = painterResource(id = R.drawable.pulsar_ic_next),
                contentDescription = "Next",
                modifier = Modifier.size(40.dp),
                tint = inactiveColor // Эта кнопка всегда неактивного цвета
            )
        }

        // --- Кнопка REPEAT ---
        IconButton(onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onToggleRepeat()
        }) {
            // ИЗМЕНЕНИЕ 2: Добавляем логику для цвета и иконки
            val repeatIcon = when (repeatMode) {
                Player.REPEAT_MODE_ONE -> R.drawable.pulsar_ic_repeat_one
                else -> R.drawable.pulsar_ic_repeat
            }
            val repeatColor = if (repeatMode != Player.REPEAT_MODE_OFF) activeColor else inactiveColor.copy(alpha = 0.5f)

            Icon(
                painter = painterResource(id = repeatIcon),
                contentDescription = "Repeat",
                modifier = Modifier.size(35.dp),
                tint = repeatColor
            )
        }
    }
}