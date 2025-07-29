package com.crackmod.flowave.presentation.screens.now_playing

import android.content.ContentUris
import android.provider.MediaStore
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.crackmod.flowave.R
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.presentation.player.PlayerUiState
import com.crackmod.flowave.presentation.screens.now_playing.components.AlbumArtWithGestures
import com.crackmod.flowave.presentation.screens.now_playing.components.OdysseyProgressIndicator
import com.crackmod.flowave.presentation.screens.now_playing.components.SpaceOdysseyPlayerControls
import kotlinx.coroutines.CoroutineScope
import kotlin.random.Random

@Composable
private fun OdysseyStarfield() {
    val stars = remember {
        List(50) {
            Triple(
                Offset(
                    x = Random.nextFloat(),
                    y = Random.nextFloat()
                ),
                Random.nextFloat() * 1.5f + 0.5f,
                Random.nextFloat() * 0.5f + 0.1f,
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        stars.forEach { (offset, radius, alpha) ->
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = radius,
                center = Offset(offset.x * size.width, offset.y * size.height)
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun SpaceOdysseyNowPlayingScreen(
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
    val accentRed = Color(0xFFE53935)
    val textColor = Color.White.copy(alpha = 0.8f)
    val monospace = FontFamily.Monospace

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C0C))
            .windowInsetsPadding(WindowInsets.safeDrawing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .weight(1.5f)) {

            OdysseyStarfield()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismissRequest) { Icon(Icons.Default.KeyboardArrowDown, "CMD_CLOSE", tint = textColor) }
                    // --- ДОБАВЛЕНА ССЫЛКА НА АЛЬБОМ ---
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
                            fontFamily = monospace,
                            color = textColor.copy(alpha = 0.7f)
                        )
                        Text(
                            currentTrack.displayAlbum,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            fontFamily = monospace,
                            color = textColor,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                    IconButton(onClick = onShowOptionsMenu) { Icon(Icons.Default.MoreVert, "CMD_OPTIONS", tint = textColor) }
                }

                AnimatedContent(
                    targetState = currentTrack,
                    label = "track_transition_odyssey",
                    transitionSpec = { fadeIn(tween(800)) togetherWith fadeOut(tween(800)) }
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
                            .fillMaxWidth(0.7f)
                            .aspectRatio(1f),
                        key = track.id
                    ) {
                        Card(
                            border = BorderStroke(1.dp, accentRed.copy(alpha = 0.5f)),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(track.albumId?.let { ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, it) })
                                    .crossfade(true)
                                    .error(R.drawable.ic_flowave_album)
                                    .build(),
                                contentDescription = "Album Art",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentTrack.displayTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Normal,
                    fontFamily = monospace,
                    color = textColor,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
                Text(
                    text = currentTrack.displayArtist,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = monospace,
                    color = textColor.copy(alpha = 0.6f),
                    maxLines = 1,
                    modifier = Modifier.clickable {
                        currentTrack.artistId?.let { onArtistClick(it) }
                        onDismissRequest()
                    }
                )
            }

            OdysseyProgressIndicator(
                position = uiState.currentPosition,
                duration = uiState.duration,
                onSeek = onSeek,
                formatTime = formatTime,
                activeColor = accentRed,
                inactiveColor = Color.White.copy(alpha = 0.2f),
                textColor = textColor.copy(alpha = 0.6f)
            )

            SpaceOdysseyPlayerControls(
                isPlaying = uiState.isPlaying,
                shuffleModeEnabled = uiState.shuffleModeEnabled,
                repeatMode = uiState.repeatMode,
                onTogglePlayPause = onPlayPauseClick,
                onSkipPrevious = onSkipPreviousClick,
                onSkipNext = onSkipNextClick,
                onToggleShuffle = onToggleShuffleClick,
                onToggleRepeat = onToggleRepeatClick,
                accentColor = accentRed,
                textColor = textColor
            )

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
                        contentDescription = "Favorite",
                        tint = if (currentTrack.isFavorite) accentRed else textColor.copy(alpha = 0.6f)
                    )
                }
                IconButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onShowLyrics()
                }) { Icon(Icons.AutoMirrored.Filled.Article, "Lyrics", tint = textColor.copy(alpha = 0.6f)) }
                IconButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onShowQueue()
                }) { Icon(Icons.Default.QueueMusic, "Queue", tint = textColor.copy(alpha = 0.6f)) }
            }
        }
    }
}