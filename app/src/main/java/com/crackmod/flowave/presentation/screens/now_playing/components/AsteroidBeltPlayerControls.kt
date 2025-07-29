package com.crackmod.flowave.presentation.screens.now_playing.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player

@Composable
fun AsteroidBeltPlayerControls(
    isPlaying: Boolean,
    shuffleModeEnabled: Boolean,
    repeatMode: Int,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    accentColor: Color,
    baseColor: Color,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    Column(modifier = modifier.fillMaxWidth()) {
        // Main Play/Pause Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTogglePlayPause()
                },
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = baseColor.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, accentColor)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = if (isPlaying) "▌▌ СТОП" else "► ПУСК",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = accentColor,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Secondary Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TerminalButton(
                text = "ПРД.",
                onClick = onSkipPrevious,
                accentColor = accentColor
            )
            TerminalButton(
                text = "СЛЧ.",
                onClick = onToggleShuffle,
                isActive = shuffleModeEnabled,
                accentColor = accentColor
            )
            TerminalButton(
                text = when(repeatMode) {
                    Player.REPEAT_MODE_ALL -> "ПОВ:∞"
                    Player.REPEAT_MODE_ONE -> "ПОВ:1"
                    else -> "ПОВ."
                },
                onClick = onToggleRepeat,
                isActive = repeatMode != Player.REPEAT_MODE_OFF,
                accentColor = accentColor
            )
            TerminalButton(
                text = "СЛД.",
                onClick = onSkipNext,
                accentColor = accentColor
            )
        }
    }
}


@Composable
private fun TerminalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    accentColor: Color
) {
    val haptic = LocalHapticFeedback.current

    // --- НОВАЯ СИГНАТУРНАЯ ЧЕРТА: ЭФФЕКТ МЕРЦАНИЯ ---
    val infiniteTransition = rememberInfiniteTransition(label = "flicker_transition")
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flicker_alpha"
    )
    val currentAlpha = if (isActive) animatedAlpha else 0.8f

    val textColor = if (isActive) accentColor.copy(alpha = currentAlpha) else Color.White.copy(alpha = 0.8f)
    val borderColor = if (isActive) accentColor.copy(alpha = currentAlpha * 0.8f) else Color.White.copy(alpha = 0.3f)

    Card(
        modifier = modifier.clickable {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f)),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}