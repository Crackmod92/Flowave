// ПУТЬ: com/crackmod/flowave/presentation/screens/now_playing/GalacticCoreNowPlayingScreen.kt
// КОД:

package com.crackmod.flowave.presentation.screens.now_playing

import android.content.ContentUris
import android.provider.MediaStore
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.crackmod.flowave.R
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.presentation.player.PlayerUiState
import com.crackmod.flowave.presentation.screens.now_playing.components.AlbumArtWithGestures
import com.crackmod.flowave.presentation.screens.now_playing.components.GalacticCorePlayerControls
import com.crackmod.flowave.presentation.screens.now_playing.components.GalacticCoreProgressIndicator
import kotlinx.coroutines.CoroutineScope
import kotlin.random.Random

private data class Star(val x: Float, val y: Float, val size: Float, val alpha: Float)

@Composable
private fun GalacticCoreBackground(accentColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "galaxy_transition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "galaxy_rotation"
    )

    val stars = remember {
        List(200) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 2f + 0.5f,
                alpha = Random.nextFloat() * 0.7f + 0.2f
            )
        }
    }

    Canvas(modifier = Modifier
        .fillMaxSize()
        .rotate(rotation)) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = center,
                radius = size.width * 0.7f
            )
        )

        stars.forEach { star ->
            drawCircle(
                color = Color.White.copy(alpha = star.alpha),
                radius = star.size,
                center = Offset(star.x * size.width, star.y * size.height)
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun GalacticCoreNowPlayingScreen(
    currentTrack: Track,
    uiState: PlayerUiState,
    onDismissRequest: () -> Unit,
    onArtistClick: (Long) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onShowLyrics: () -> Unit,
    onShowOptionsMenu: () -> Unit,
    onShowQueue: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSkipNextClick: () -> Unit,
    onSkipPreviousClick: () -> Unit,
    onToggleShuffleClick: () -> Unit,
    onToggleRepeatClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleFavorite: () -> Unit,
    formatTime: (Long) -> String,
    haptic: HapticFeedback,
    scope: CoroutineScope
) {
    // ИЗМЕНЕНИЕ: Задаем цвета статично, чтобы они не менялись с темой
    val accentGold = Color(0xFFE6B400)
    val baseColor = Color(0xFF1A1A1D) // Темный фон
    val backgroundColor = Color(0xFF0C0C0C) // Абсолютно темный фон

    Box(
        modifier = Modifier.fillMaxSize().background(backgroundColor) // Устанавливаем базовый темный фон
    ) {
        GalacticCoreBackground(accentColor = accentGold)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            baseColor.copy(alpha = 0.5f),
                            Color.Transparent,
                            Color.Transparent,
                            baseColor
                        ),
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismissRequest) { Icon(Icons.Default.KeyboardArrowDown, "Свернуть", tint = Color.White.copy(alpha = 0.8f)) }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            currentTrack.albumId?.let { onAlbumClick(it) }
                            onDismissRequest()
                        },
                ) {
                    Text(
                        "ИЗ АЛЬБОМА",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        currentTrack.displayAlbum,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                }
                IconButton(onClick = onShowOptionsMenu) { Icon(Icons.Default.MoreVert, "Опции", tint = Color.White.copy(alpha = 0.8f)) }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            AnimatedContent(
                targetState = currentTrack,
                label = "track_transition_galaxy",
                transitionSpec = { fadeIn(tween(600)) togetherWith fadeOut(tween(600)) }
            ) { track ->
                AlbumArtWithGestures(
                    onSwipeNext = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSkipNextClick()
                    },
                    onSwipePrevious = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSkipPreviousClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(1f),
                    key = track.id
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = accentGold)
                            .border(
                                width = 1.5.dp,
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        accentGold,
                                        accentGold.copy(alpha = 0.4f),
                                        accentGold
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(8.dp)
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(track.albumId?.let { ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, it) })
                                .crossfade(true)
                                .error(R.drawable.ic_flowave_album)
                                .build(),
                            contentDescription = "Обложка альбома",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentTrack.displayTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White, // Явно белый
                    maxLines = 1,
                    modifier = Modifier
                        .basicMarquee()
                )
                Text(
                    text = currentTrack.displayArtist,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f), // Явно белый с альфой
                    maxLines = 1,
                    modifier = Modifier.clickable {
                        currentTrack.artistId?.let { onArtistClick(it) }
                        onDismissRequest()
                    }
                )
                Spacer(Modifier.height(24.dp))
                GalacticCoreProgressIndicator(
                    position = uiState.currentPosition,
                    duration = uiState.duration,
                    onSeek = onSeek,
                    formatTime = formatTime,
                    accentColor = accentGold,
                    baseColor = baseColor
                )
                Spacer(Modifier.height(16.dp))
                GalacticCorePlayerControls(
                    isPlaying = uiState.isPlaying,
                    shuffleModeEnabled = uiState.shuffleModeEnabled,
                    repeatMode = uiState.repeatMode,
                    onTogglePlayPause = onPlayPauseClick,
                    onSkipPrevious = onSkipPreviousClick,
                    onSkipNext = onSkipNextClick,
                    onToggleShuffle = onToggleShuffleClick,
                    onToggleRepeat = onToggleRepeatClick,
                    accentColor = accentGold,
                    baseColor = baseColor,
                    contentColor = Color.White // Явно передаем белый цвет
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleFavorite()
                    }) {
                        Icon(
                            painter = painterResource(id = if (currentTrack.isFavorite) R.drawable.ic_flowave_favorite_filled else R.drawable.ic_flowave_favorite_border),
                            contentDescription = "В избранное",
                            tint = if (currentTrack.isFavorite) accentGold else Color.White.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onShowLyrics()
                    }) { Icon(Icons.AutoMirrored.Filled.Article, "Текст", tint = Color.White.copy(alpha = 0.7f)) }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onShowQueue()
                    }) { Icon(Icons.Default.QueueMusic, "К очереди", tint = Color.White.copy(alpha = 0.7f)) }
                }
            }
        }
    }
}