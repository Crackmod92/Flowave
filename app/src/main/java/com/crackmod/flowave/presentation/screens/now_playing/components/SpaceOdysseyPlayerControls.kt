// ПУТЬ: com/crackmod/flowave/presentation/screens/now_playing/components/SpaceOdysseyPlayerControls.kt
// КОД:

package com.crackmod.flowave.presentation.screens.now_playing.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.crackmod.flowave.R

@Composable
fun SpaceOdysseyPlayerControls(
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
    textColor: Color
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Кнопка SHUFFLE (с вашей новой иконкой) ---
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleShuffle()
            },
            modifier = Modifier.size(48.dp)
        ) {
            val color = if (shuffleModeEnabled) accentColor else textColor.copy(alpha = 0.3f)
            Icon(
                painter = painterResource(id = R.drawable.so_ic_shuffle), // ИСПОЛЬЗУЕМ НОВУЮ ИКОНКУ
                contentDescription = "Перемешать",
                tint = color,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(Modifier.width(16.dp))

        // --- Кнопка PREVIOUS (с вашей новой иконкой) ---
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSkipPrevious()
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.so_ic_previous), // ИСПОЛЬЗУЕМ НОВУЮ ИКОНКУ
                contentDescription = "Предыдущий",
                tint = textColor,
                modifier = Modifier.size(100.dp)
            )
        }


        Spacer(Modifier.width(24.dp))

        // --- Кнопка PLAY/PAUSE (осталась без изменений, с Canvas) ---
        Box(
            modifier = Modifier
                .size(72.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTogglePlayPause()
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = accentColor,
                    style = Stroke(width = 2.dp.toPx())
                )
                // Рисуем внутренний круг только когда трек играет
                if (isPlaying) {
                    drawCircle(
                        color = accentColor,
                        radius = size.minDimension / 4f
                    )
                }
            }
        }

        Spacer(Modifier.width(24.dp))

        // --- Кнопка NEXT (с вашей новой иконкой) ---
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSkipNext()
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.so_ic_next), // ИСПОЛЬЗУЕМ НОВУЮ ИКОНКУ
                contentDescription = "Следующий",
                tint = textColor,
                modifier = Modifier.size(100.dp)
            )
        }

        Spacer(Modifier.width(16.dp))

        // --- Кнопка REPEAT (с вашими новыми иконками) ---
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleRepeat()
            },
            modifier = Modifier.size(48.dp)
        ) {
            val iconRes = when (repeatMode) {
                Player.REPEAT_MODE_ONE -> R.drawable.so_ic_repeat_one // ИСПОЛЬЗУЕМ НОВУЮ ИКОНКУ
                Player.REPEAT_MODE_ALL -> R.drawable.so_ic_repeat // ИСПОЛЬЗУЕМ НОВУЮ ИКОНКУ
                else -> R.drawable.so_ic_repeat // Используем ту же иконку для состояния "off"
            }
            val color = if (repeatMode != Player.REPEAT_MODE_OFF) accentColor else textColor.copy(alpha = 0.3f)
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = "Повторить",
                tint = color,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}