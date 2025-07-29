// ПУТЬ: com/crackmod/flowave/presentation/screens/now_playing/components/GalacticCorePlayerControls.kt
// КОД:

package com.crackmod.flowave.presentation.screens.now_playing.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.crackmod.flowave.R

@Composable
fun GalacticCorePlayerControls(
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
    baseColor: Color,
    contentColor: Color
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val goldGradient = Brush.horizontalGradient(listOf(accentColor.copy(alpha = 0.7f), accentColor))

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Кнопка SHUFFLE ---
        Card(
            modifier = Modifier.size(48.dp).clickable(interactionSource, null, onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onToggleShuffle() }),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = baseColor.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, if (shuffleModeEnabled) accentColor else contentColor.copy(alpha = 0.3f))
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_flowave_shuffle_off),
                    contentDescription = "Перемешать",
                    tint = if (shuffleModeEnabled) accentColor else contentColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // --- Кнопка PREVIOUS ---
        Card(
            modifier = Modifier.size(56.dp).clickable(interactionSource, null, onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onSkipPrevious() }),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = baseColor.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, contentColor.copy(alpha = 0.3f))
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_flowave_skip_previous),
                    contentDescription = "Предыдущий",
                    tint = contentColor,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // --- Кнопка PLAY/PAUSE ---
        Card(
            modifier = Modifier.size(72.dp).clickable(interactionSource, null, onClick = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onTogglePlayPause() }),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = baseColor),
            border = BorderStroke(1.5.dp, goldGradient)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                val iconRes = if (isPlaying) R.drawable.ic_flowave_pause else R.drawable.ic_flowave_play
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = "Воспроизвести/Пауза",
                    tint = accentColor,
                    modifier = Modifier.size(55.dp)
                )
            }
        }

        // --- Кнопка NEXT ---
        Card(
            modifier = Modifier.size(56.dp).clickable(interactionSource, null, onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onSkipNext() }),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = baseColor.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, contentColor.copy(alpha = 0.3f))
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_flowave_skip_next),
                    contentDescription = "Следующий",
                    tint = contentColor,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // --- Кнопка REPEAT ---
        Card(
            modifier = Modifier.size(48.dp).clickable(interactionSource, null, onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onToggleRepeat() }),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = baseColor.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, if (repeatMode != Player.REPEAT_MODE_OFF) accentColor else contentColor.copy(alpha = 0.3f))
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                val iconRes = when (repeatMode) {
                    Player.REPEAT_MODE_ONE -> R.drawable.ic_flowave_repeat_one
                    else -> R.drawable.ic_flowave_repeat_all
                }
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = "Повторить",
                    tint = if (repeatMode != Player.REPEAT_MODE_OFF) accentColor else contentColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}