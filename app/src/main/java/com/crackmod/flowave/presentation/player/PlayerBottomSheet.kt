package com.crackmod.flowave.presentation.player

import android.content.ContentUris
import android.provider.MediaStore
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.crackmod.flowave.R
import com.crackmod.flowave.ui.theme.AmoledBackground

@Composable
fun PlayerBottomSheet(
    uiState: PlayerUiState,
    onContainerClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSkipNextClick: () -> Unit,
    onSkipPreviousClick: () -> Unit,
    onStopClick: () -> Unit,
) {
    // ### ИЗМЕНЕНИЕ: Вся логика теперь здесь ###
    val isAmoled = MaterialTheme.colorScheme.background == AmoledBackground

    // Определяем цвета по умолчанию
    val defaultBackgroundColor = if (isAmoled) Color.Black else MaterialTheme.colorScheme.surfaceVariant
    val defaultContentColor = MaterialTheme.colorScheme.onSurface

    // Выбираем целевые цвета: динамические или по умолчанию
    val targetBackgroundColor = if (isAmoled) defaultBackgroundColor else uiState.miniPlayerBackgroundColor ?: defaultBackgroundColor
    val targetContentColor = if (isAmoled) defaultContentColor else uiState.miniPlayerTextColor ?: defaultContentColor

    // Анимируем переход между цветами
    val animatedBackgroundColor by animateColorAsState(targetValue = targetBackgroundColor, tween(1200), label = "bg_color")
    val animatedContentColor by animateColorAsState(targetValue = targetContentColor, tween(1200), label = "content_color")

    // Создаем модификатор для рамки, который применяется только в AMOLED
    val borderModifier = if (isAmoled) {
        Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
    } else {
        Modifier
    }

    // Внешний Box для отступов, чтобы плеер "плавал"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        AnimatedContent(
            targetState = uiState.isRestoring || uiState.currentTrack == null,
            label = "mini_player_transition"
        ) { isPlaceholderVisible ->
            if (isPlaceholderVisible) {
                // Плейсхолдер загрузки
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(defaultBackgroundColor)
                        .then(borderModifier)
                ) {
                    if (uiState.isRestoring) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(2.dp).align(Alignment.BottomCenter)
                        )
                    }
                }
            } else {
                uiState.currentTrack?.let { track ->
                    val progress = if (uiState.duration > 0) uiState.currentPosition.toFloat() / uiState.duration.toFloat() else 0f
                    val albumArtUri = track.albumId?.let {
                        try { ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, it) } catch (e: Exception) { null }
                    }
                    val haptic = LocalHapticFeedback.current
                    var dragAmount by remember { mutableFloatStateOf(0f) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)) // Скругляем углы
                            .then(borderModifier) // Добавляем рамку для AMOLED
                            .background(animatedBackgroundColor) // Применяем анимированный фон
                            .clickable(onClick = onContainerClick)
                            .pointerInput(track.id) {
                                detectHorizontalDragGestures(
                                    onDragStart = { dragAmount = 0f },
                                    onDragEnd = {
                                        val dragThreshold = size.width / 4
                                        if (dragAmount < -dragThreshold) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onSkipNextClick()
                                        } else if (dragAmount > dragThreshold) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onSkipPreviousClick()
                                        }
                                    },
                                    onHorizontalDrag = { change, drag ->
                                        if (change.positionChange() != androidx.compose.ui.geometry.Offset.Zero) change.consume()
                                        dragAmount += drag
                                    }
                                )
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(albumArtUri).crossfade(true).error(R.drawable.ic_flowave_album)
                                        .placeholder(R.drawable.ic_flowave_album).build(),
                                    contentDescription = "Обложка альбома",
                                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                                    Text(
                                        text = track.displayTitle, style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                        color = animatedContentColor // Динамический цвет текста
                                    )
                                    Text(
                                        text = track.displayArtist, style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                                        color = animatedContentColor.copy(alpha = 0.8f) // Чуть приглушенный
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = onPlayPauseClick) {
                                    Icon(
                                        painter = painterResource(id = if (uiState.isPlaying) R.drawable.ic_bottomplayer_pause else R.drawable.ic_bottomplayer_play),
                                        contentDescription = "Play/Pause", modifier = Modifier.size(32.dp),
                                        tint = animatedContentColor // Динамический цвет иконки
                                    )
                                }
                                IconButton(onClick = onStopClick) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_bottomplayer_close),
                                        contentDescription = "Close Player", modifier = Modifier.size(32.dp),
                                        tint = animatedContentColor // Динамический цвет иконки
                                    )
                                }
                            }
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(3.dp).align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
    }
}