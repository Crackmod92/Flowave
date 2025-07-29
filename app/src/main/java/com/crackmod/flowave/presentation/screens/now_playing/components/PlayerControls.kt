package com.crackmod.flowave.presentation.screens.now_playing.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.crackmod.flowave.ui.theme.AmoledBackground
import com.crackmod.flowave.ui.theme.NavigationUnselectedAmoled

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    shuffleModeEnabled: Boolean,
    repeatMode: Int,
    isTogglingShuffle: Boolean,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier,
    useDarkThemeColors: Boolean = false,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurface,
    playPauseContainerColor: Color = activeColor,
) {
    val haptic = LocalHapticFeedback.current

    val finalInactiveColor = if (useDarkThemeColors) {
        if (MaterialTheme.colorScheme.background == AmoledBackground) NavigationUnselectedAmoled else Color.White.copy(alpha = 0.8f)
    } else {
        inactiveColor
    }

    val finalActiveColor = if (useDarkThemeColors) Color.White else activeColor
    val finalPlayPauseContentColor = if (playPauseContainerColor.luminance() > 0.5f) {
        Color.Black
    } else {
        Color.White
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleShuffle()
            },
            enabled = !isTogglingShuffle
        ) {
            AnimatedContent(
                targetState = isTogglingShuffle,
                label = "shuffle_icon_anim",
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { isToggling ->
                if (isToggling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.dp,
                        color = finalActiveColor
                    )
                } else {
                    Icon(
                        painter = painterResource(id = if (shuffleModeEnabled) R.drawable.ic_flowave_shuffle_on else R.drawable.ic_flowave_shuffle_off),
                        contentDescription = "Перемешать",
                        modifier = Modifier.size(28.dp),
                        tint = if (shuffleModeEnabled) finalActiveColor else finalInactiveColor
                    )
                }
            }
        }

        IconButton(onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onSkipPrevious()
        }, modifier = Modifier.size(56.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.ic_flowave_skip_previous),
                contentDescription = "Предыдущий",
                modifier = Modifier.fillMaxSize(),
                tint = finalInactiveColor
            )
        }

        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onTogglePlayPause()
            },
            modifier = Modifier
                .size(72.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(playPauseContainerColor)
        ) {
            Icon(
                painter = painterResource(id = if (isPlaying) R.drawable.ic_flowave_pause else R.drawable.ic_flowave_play),
                contentDescription = if (isPlaying) "Пауза" else "Воспроизвести",
                modifier = Modifier.size(40.dp),
                tint = finalPlayPauseContentColor
            )
        }

        IconButton(onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onSkipNext()
        }, modifier = Modifier.size(56.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.ic_flowave_skip_next),
                contentDescription = "Следующий",
                modifier = Modifier.fillMaxSize(),
                tint = finalInactiveColor
            )
        }

        IconButton(onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onToggleRepeat()
        }) {
            val repeatIconRes = when (repeatMode) {
                Player.REPEAT_MODE_ONE -> R.drawable.ic_flowave_repeat_one
                Player.REPEAT_MODE_ALL -> R.drawable.ic_flowave_repeat_all
                else -> R.drawable.ic_flowave_repeat_off
            }
            val targetTint = if (repeatMode == Player.REPEAT_MODE_OFF) {
                finalInactiveColor
            } else {
                finalActiveColor
            }

            Icon(
                painter = painterResource(id = repeatIconRes),
                contentDescription = "Повторить",
                modifier = Modifier.size(28.dp),
                tint = targetTint
            )
        }
    }
}