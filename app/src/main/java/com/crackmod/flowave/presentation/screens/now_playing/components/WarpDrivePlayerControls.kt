package com.crackmod.flowave.presentation.screens.now_playing.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player

@Composable
private fun TerminalButton(
    text: String,
    onClick: () -> Unit,
    isActive: Boolean,
    activeColor: Color,
    textColor: Color
) {
    val haptic = LocalHapticFeedback.current
    val finalTextColor = if (isActive) activeColor else textColor
    val borderColor = if (isActive) activeColor.copy(alpha = 0.9f) else textColor.copy(alpha = 0.5f)

    Card(
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.clickable {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        }
    ) {
        Text(
            text = text,
            color = finalTextColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}


@Composable
fun WarpDrivePlayerControls(
    shuffleModeEnabled: Boolean,
    repeatMode: Int,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    accentColor1: Color,
    textColor: Color
) {
    val haptic = LocalHapticFeedback.current
    val monospaceFont = FontFamily.Monospace

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RectangleShape,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, accentColor1.copy(alpha = 0.7f)),
            modifier = Modifier.clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onTogglePlayPause()
            }
        ) {
            Text(
                text = if (isPlaying) "[ ■ СИСТЕМА: СТОП ]" else "[ ► СИСТЕМА: ПУСК ]",
                fontFamily = monospaceFont,
                fontWeight = FontWeight.Bold,
                color = accentColor1,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TerminalButton(
                text = "ПЕРЕМЕШ.",
                onClick = onToggleShuffle,
                isActive = shuffleModeEnabled,
                activeColor = accentColor1,
                textColor = textColor
            )

            TerminalButton(
                text = "<< НАЗАД",
                onClick = onSkipPrevious,
                isActive = false,
                activeColor = accentColor1,
                textColor = textColor
            )

            TerminalButton(
                text = "ВПЕРЕД >>",
                onClick = onSkipNext,
                isActive = false,
                activeColor = accentColor1,
                textColor = textColor
            )

            TerminalButton(
                text = when (repeatMode) {
                    Player.REPEAT_MODE_ONE -> "ПОВТОР: 1"
                    Player.REPEAT_MODE_ALL -> "ПОВТОР: ∞"
                    else -> "ПОВТОР"
                },
                onClick = onToggleRepeat,
                isActive = repeatMode != Player.REPEAT_MODE_OFF,
                activeColor = accentColor1,
                textColor = textColor
            )
        }
    }
}