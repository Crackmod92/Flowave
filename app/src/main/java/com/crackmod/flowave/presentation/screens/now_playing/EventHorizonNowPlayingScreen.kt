// ФАЙЛ: EventHorizonNowPlayingScreen.txt
package com.crackmod.flowave.presentation.screens.now_playing

import android.annotation.SuppressLint
import android.content.ContentUris
import android.provider.MediaStore
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
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
import com.crackmod.flowave.presentation.screens.now_playing.components.CircularVisualizer
import com.crackmod.flowave.presentation.screens.now_playing.components.PlayerControls
import kotlinx.coroutines.CoroutineScope
import kotlin.random.Random

// СТИЛЬ "EVENT HORIZON" (Полностью переделанный)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventHorizonNowPlayingScreen(
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
    // --- ИЗМЕНЕНИЕ АНИМАЦИИ ---
    // Вместо полного вращения теперь плавное покачивание
    val infiniteTransition = rememberInfiniteTransition(label = "sway_transition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -1.5f, // Начальный угол
        targetValue = 1.5f,    // Конечный угол
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing), // Более плавная анимация
            repeatMode = RepeatMode.Reverse // Покачивание туда-обратно
        ),
        label = "album_sway"
    )
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        StarfieldBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismissRequest) { Icon(Icons.Default.KeyboardArrowDown, "Свернуть", tint = Color.White) }
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
                IconButton(onClick = onShowOptionsMenu) { Icon(Icons.Default.MoreVert, "Опции", tint = Color.White) }
            }

            // Central Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedContent(
                    targetState = currentTrack,
                    label = "track_transition_horizon",
                    transitionSpec = {
                        fadeIn(tween(800)) togetherWith fadeOut(tween(400))
                    }
                ) { track ->
                    CircularAlbumArtWithVisualizer(
                        track = track,
                        progress = if (uiState.duration > 0) uiState.currentPosition.toFloat() / uiState.duration else 0f,
                        isPlaying = uiState.isPlaying,
                        rotationDegrees = rotation, // Передаем новую анимацию
                        onSeek = { fraction -> onSeek((uiState.duration * fraction).toLong()) },
                        onSwipeNext = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSkipNextClick()
                        },
                        onSwipePrevious = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSkipPreviousClick()
                        }
                    )
                }
            }

            // Bottom Info & Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentTrack.displayTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
                Text(
                    text = currentTrack.displayArtist,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    modifier = Modifier.clickable {
                        currentTrack.artistId?.let { onArtistClick(it) }
                        onDismissRequest()
                    }
                )
                Spacer(Modifier.height(24.dp))

                // Time display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(uiState.currentPosition), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                    Text(formatTime(uiState.duration), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                }
                Spacer(Modifier.height(16.dp))

                PlayerControls(
                    isPlaying = uiState.isPlaying,
                    shuffleModeEnabled = uiState.shuffleModeEnabled,
                    isTogglingShuffle = uiState.isTogglingShuffle,
                    repeatMode = uiState.repeatMode,
                    onTogglePlayPause = onPlayPauseClick,
                    onSkipPrevious = onSkipPreviousClick,
                    onSkipNext = onSkipNextClick,
                    onToggleShuffle = onToggleShuffleClick,
                    onToggleRepeat = onToggleRepeatClick,
                    useDarkThemeColors = true,
                    playPauseContainerColor = MaterialTheme.colorScheme.primary,
                    activeColor = Color.White
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
                            tint = if (currentTrack.isFavorite) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f)
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

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun CircularAlbumArtWithVisualizer(
    track: Track,
    progress: Float,
    isPlaying: Boolean,
    rotationDegrees: Float,
    onSeek: (Float) -> Unit,
    onSwipeNext: () -> Unit,
    onSwipePrevious: () -> Unit
) {
    val context = LocalContext.current
    val albumArtUri = track.albumId?.let { ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, it) }
    var dragAmount by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .aspectRatio(1f)
            .pointerInput(track.id) {
                detectHorizontalDragGestures(
                    onDragStart = { dragAmount = 0f },
                    onDragEnd = {
                        val dragThreshold = size.width / 4
                        when {
                            dragAmount < -dragThreshold -> onSwipeNext()
                            dragAmount > dragThreshold -> onSwipePrevious()
                        }
                    },
                    onHorizontalDrag = { change, drag ->
                        if (change.positionChange() != Offset.Zero) change.consume()
                        dragAmount += drag
                    }
                )
            }
    ) {
        val visualizerPadding = 20.dp
        val albumArtSize = maxWidth - (visualizerPadding * 2)

        CircularVisualizer(
            progress = progress,
            isPlaying = isPlaying,
            modifier = Modifier.size(maxWidth),
            onSeek = onSeek,
            strokeWidth = 3.dp,
            visualizerWidth = 14.dp
        )

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(albumArtUri)
                .crossfade(true)
                .error(R.drawable.ic_flowave_album)
                .build(),
            contentDescription = "Обложка альбома",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(albumArtSize)
                .rotate(if (isPlaying) rotationDegrees else 0f)
                .clip(CircleShape)
        )
    }
}

@Composable
private fun StarfieldBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "starfield")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_alpha"
    )

    val stars = remember {
        List(100) {
            Offset(
                x = Random.nextFloat(),
                y = Random.nextFloat()
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val starPoints = stars.map {
            Offset(it.x * size.width, it.y * size.height)
        }
        drawPoints(
            points = starPoints,
            pointMode = PointMode.Points,
            color = Color.White.copy(alpha = alpha),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
    }
}