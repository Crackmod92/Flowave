package com.crackmod.flowave.presentation.screens.now_playing

import android.content.ContentUris
import android.provider.MediaStore
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.crackmod.flowave.R
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.presentation.player.PlayerUiState
import com.crackmod.flowave.presentation.screens.now_playing.components.AlbumArtWithGestures
import com.crackmod.flowave.presentation.screens.now_playing.components.AnimatedTrackProgress
import com.crackmod.flowave.presentation.screens.now_playing.components.PlayerControls
import kotlinx.coroutines.CoroutineScope

// СТИЛЬ "SOLAR FLARE" (бывший DEFAULT)
@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun SolarFlareNowPlayingScreen(
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
    val albumArtUri = remember(currentTrack.albumId) {
        currentTrack.albumId?.let {
            try {
                ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, it)
            } catch (e: Exception) { null }
        }
    }

    var isNextTrack by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(albumArtUri)
                .crossfade(true)
                .memoryCacheKey("bg_${currentTrack.id}")
                .build(),
            contentDescription = "Размытый фон",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 32.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.2f),
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.KeyboardArrowDown, "Свернуть", tint = Color.White)
                }
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
                        color = Color.White.copy(alpha = 0.8f)
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
                IconButton(onClick = onShowOptionsMenu) {
                    Icon(Icons.Default.MoreVert, "Опции", tint = Color.White)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.weight(0.5f))

                AnimatedContent(
                    targetState = currentTrack,
                    label = "track_transition",
                    transitionSpec = {
                        val enterOffset = { size: Int -> if (isNextTrack) size else -size }
                        val exitOffset = { size: Int -> if (isNextTrack) -size else size }

                        slideInHorizontally(
                            animationSpec = tween(600, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)),
                            initialOffsetX = enterOffset
                        ) + fadeIn(
                            animationSpec = tween(500, delayMillis = 100)
                        ) togetherWith
                                slideOutHorizontally(
                                    animationSpec = tween(500, easing = CubicBezierEasing(0.4f, 0.0f, 0.6f, 1.0f)),
                                    targetOffsetX = exitOffset
                                ) + fadeOut(
                            animationSpec = tween(400)
                        )
                    }
                ) { track ->
                    AlbumArtWithGestures(
                        onSwipeNext = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isNextTrack = true
                            onSkipNextClick()
                        },
                        onSwipePrevious = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isNextTrack = false
                            onSkipPreviousClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        key = track.id
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(track.albumId?.let {
                                    try {
                                        ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, it)
                                    } catch (e: Exception) { null }
                                })
                                .crossfade(true)
                                .error(R.drawable.ic_flowave_album)
                                .build(),
                            contentDescription = "Обложка альбома",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .shadow(
                                    elevation = 12.dp,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                currentTrack.displayTitle,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                modifier = Modifier
                                    .basicMarquee()
                                    .clickable {
                                        currentTrack.albumId?.let { onAlbumClick(it) }
                                        onDismissRequest()
                                    },
                                color = Color.White
                            )
                            Text(
                                currentTrack.displayArtist,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.clickable {
                                    currentTrack.artistId?.let { onArtistClick(it) }
                                    onDismissRequest()
                                }
                            )
                        }
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleFavorite()
                        }) {
                            Icon(
                                painter = painterResource(id = if (currentTrack.isFavorite) R.drawable.ic_flowave_favorite_filled else R.drawable.ic_flowave_favorite_border),
                                contentDescription = "В избранное",
                                modifier = Modifier.size(28.dp),
                                tint = if (currentTrack.isFavorite) MaterialTheme.colorScheme.primary else Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    AnimatedTrackProgress(
                        position = uiState.currentPosition,
                        duration = uiState.duration,
                        onSeek = onSeek,
                        formatTime = formatTime,
                        isPlaying = uiState.isPlaying,
                        forceDarkThemeColors = true
                    )

                    Spacer(Modifier.height(8.dp))

                    PlayerControls(
                        isPlaying = uiState.isPlaying,
                        shuffleModeEnabled = uiState.shuffleModeEnabled,
                        isTogglingShuffle = uiState.isTogglingShuffle,
                        repeatMode = uiState.repeatMode,
                        onTogglePlayPause = onPlayPauseClick,
                        onSkipPrevious = {
                            isNextTrack = false
                            onSkipPreviousClick()
                        },
                        onSkipNext = {
                            isNextTrack = true
                            onSkipNextClick()
                        },
                        onToggleShuffle = onToggleShuffleClick,
                        onToggleRepeat = onToggleRepeatClick,
                        useDarkThemeColors = true,
                        playPauseContainerColor = MaterialTheme.colorScheme.primary,
                        activeColor = Color.White
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onShowLyrics()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Article, "Текст", tint = Color.White)
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onShowQueue()
                    }) {
                        Icon(Icons.Default.QueueMusic, "К очереди", tint = Color.White)
                    }
                }
            }
        }
    }
}