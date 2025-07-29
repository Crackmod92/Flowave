package com.crackmod.flowave.presentation.screens.now_playing

import android.content.ContentUris
import android.provider.MediaStore
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.crackmod.flowave.R
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.presentation.player.PlayerUiState
import com.crackmod.flowave.presentation.screens.now_playing.components.AlbumArtWithGestures
import com.crackmod.flowave.presentation.screens.now_playing.components.GlitchLineProgressIndicator
import com.crackmod.flowave.presentation.screens.now_playing.components.WarpDrivePlayerControls
import kotlinx.coroutines.CoroutineScope
import kotlin.random.Random

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun WarpDriveNowPlayingScreen(
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

    val infiniteTransition = rememberInfiniteTransition(label = "crt_animation")
    val distortionOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "distortion_offset"
    )
    val noiseSeed by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing)
        ),
        label = "noise_seed"
    )

    val accentColor1 = Color(0xFF00FFFF) // Cyan
    val accentColor2 = Color(0xFF39FF14) // Neon Green
    val textColor = Color.White.copy(alpha = 0.9f)
    val monospaceFont = FontFamily.Monospace

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {
        AlbumArtWithGestures(
            onSwipeNext = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSkipNextClick()
            },
            onSwipePrevious = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSkipPreviousClick()
            },
            modifier = Modifier.fillMaxSize(),
            key = currentTrack.id
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(albumArtUri)
                    .crossfade(250)
                    .error(R.drawable.ic_flowave_album)
                    .build(),
                contentDescription = "Album Art Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.6f)
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCrtDistortion(accentColor1, accentColor2, distortionOffsetY, noiseSeed)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.9f)
                        ),
                        startY = 0.0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.KeyboardArrowDown, "SYS_CLOSE", tint = accentColor1)
                }
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
                        "// ИЗ АЛЬБОМА",
                        fontFamily = monospaceFont,
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    Text(
                        currentTrack.displayAlbum,
                        fontFamily = monospaceFont,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                }
                IconButton(onClick = onShowOptionsMenu) {
                    Icon(Icons.Default.MoreVert, "SYS_OPT", tint = accentColor1)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            "// NOW_PLAYING:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = monospaceFont,
                                color = textColor.copy(alpha = 0.5f),
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AnimatedContent(
                            targetState = currentTrack.displayTitle,
                            transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
                            label = "title_anim"
                        ) { title ->
                            Text(
                                title,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = monospaceFont,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                ),
                                maxLines = 1,
                                modifier = Modifier
                                    .basicMarquee()
                            )
                        }
                        AnimatedContent(
                            targetState = currentTrack.displayArtist,
                            transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
                            label = "artist_anim"
                        ) { artist ->
                            Text(
                                artist,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = monospaceFont,
                                    color = textColor.copy(alpha = 0.7f),
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.clickable {
                                    currentTrack.artistId?.let { onArtistClick(it) }
                                    onDismissRequest()
                                },
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleFavorite()
                    }) {
                        Icon(
                            painter = painterResource(id = if (currentTrack.isFavorite) R.drawable.ic_flowave_favorite_filled else R.drawable.ic_flowave_favorite_border),
                            contentDescription = "ADD_FAV",
                            modifier = Modifier.size(28.dp),
                            tint = if (currentTrack.isFavorite) accentColor2 else textColor
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                GlitchLineProgressIndicator(
                    position = uiState.currentPosition,
                    duration = uiState.duration,
                    onSeek = onSeek,
                    formatTime = formatTime,
                    isPlaying = uiState.isPlaying,
                    accentColor1 = accentColor1,
                    accentColor2 = accentColor2,
                    textColor = textColor,
                )

                Spacer(Modifier.height(24.dp))

                WarpDrivePlayerControls(
                    isPlaying = uiState.isPlaying,
                    shuffleModeEnabled = uiState.shuffleModeEnabled,
                    repeatMode = uiState.repeatMode,
                    onTogglePlayPause = onPlayPauseClick,
                    onSkipPrevious = onSkipPreviousClick,
                    onSkipNext = onSkipNextClick,
                    onToggleShuffle = onToggleShuffleClick,
                    onToggleRepeat = onToggleRepeatClick,
                    accentColor1 = accentColor1,
                    textColor = textColor
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onShowLyrics()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Article, "GET_LYRICS", tint = accentColor1)
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onShowQueue()
                    }) {
                        Icon(Icons.Default.QueueMusic, "GOTO_QUEUE", tint = accentColor1)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawCrtDistortion(color1: Color, color2: Color, offsetY: Float, noiseSeed: Float) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val random = Random(noiseSeed.toLong())

    // 1. Scanlines
    for (y in 0 until size.height.toInt() step 3) {
        drawLine(
            color = Color.Black.copy(alpha = 0.15f),
            start = Offset(0f, y.toFloat()),
            end = Offset(canvasWidth, y.toFloat()),
            strokeWidth = 1.5f
        )
    }

    // 2. Rolling Hum Bar
    val barHeight = canvasHeight * 0.3f
    val barY = (offsetY * (canvasHeight + barHeight)) - barHeight
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.0f),
                Color.White.copy(alpha = 0.03f),
                Color.White.copy(alpha = 0.0f)
            )
        ),
        topLeft = Offset(0f, barY),
        size = androidx.compose.ui.geometry.Size(canvasWidth, barHeight)
    )

    // 3. Chromatic Aberration
    drawRect(
        color = color1.copy(alpha = 0.08f),
        topLeft = Offset(-3f, 0f),
        size = size
    )
    drawRect(
        color = color2.copy(alpha = 0.06f),
        topLeft = Offset(2f, 0f),
        size = size
    )

    // 4. Signal Noise / Snow
    repeat(100) {
        val x = random.nextFloat() * canvasWidth
        val y = random.nextFloat() * canvasHeight
        drawCircle(
            color = Color.White.copy(alpha = random.nextFloat() * 0.1f),
            radius = random.nextFloat() * 1.5f,
            center = Offset(x, y)
        )
    }
}