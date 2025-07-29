// ФАЙЛ: AuroraNowPlayingScreen.txt
package com.crackmod.flowave.presentation.screens.now_playing

import android.content.ContentUris
import android.provider.MediaStore
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
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
import com.crackmod.flowave.presentation.screens.now_playing.components.AuroraPlayerControls
import com.crackmod.flowave.presentation.screens.now_playing.components.AuroraProgressIndicator
import kotlinx.coroutines.CoroutineScope

// СТИЛЬ "AURORA" (бывший COMPACT BOTTOM BAR / AURORA GLASS)
@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun AuroraNowPlayingScreen(
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
    val dominantColor = MaterialTheme.colorScheme.primary
    val animatedDominantColor by animateColorAsState(dominantColor, tween(1000), label = "dominant_color_anim")

    Box(modifier = Modifier.fillMaxSize()) {
        AuroraBackground(color = animatedDominantColor)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TopBar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.KeyboardArrowDown, "Свернуть", tint = Color.White)
                }
                // --- ИЗМЕНЕНИЕ: Добавлен переход на альбом ---
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            currentTrack.albumId?.let { onAlbumClick(it) }
                            onDismissRequest()
                        }
                        .padding(vertical = 4.dp),
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
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedContent(
                    targetState = currentTrack,
                    label = "track_transition_aurora",
                    transitionSpec = { fadeIn(tween(600)) togetherWith fadeOut(tween(600)) }
                ) { track ->
                    AlbumArtWithGestures(
                        onSwipeNext = onSkipNextClick,
                        onSwipePrevious = onSkipPreviousClick,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .aspectRatio(1f),
                        key = track.id
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(track.albumId?.let { ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, it) })
                                .crossfade(true)
                                .error(R.drawable.ic_flowave_album)
                                .build(),
                            contentDescription = "Обложка",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .shadow(elevation = 20.dp, shape = RoundedCornerShape(32.dp), spotColor = animatedDominantColor)
                                .clip(RoundedCornerShape(32.dp))
                        )
                    }
                }
            }

            // --- ОБНОВЛЕННАЯ СТЕКЛЯННАЯ ПАНЕЛЬ ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        ),
                        RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            currentTrack.displayTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee(),
                            color = Color.White
                        )
                        // --- ИЗМЕНЕНИЕ: Добавлен переход на исполнителя ---
                        Text(
                            currentTrack.displayArtist,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            modifier = Modifier.clickable {
                                currentTrack.artistId?.let { onArtistClick(it) }
                                onDismissRequest()
                            },
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    // --- ИЗМЕНЕНИЕ: Добавлен Haptic Feedback ---
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleFavorite()
                    }) {
                        Icon(
                            painter = painterResource(id = if (currentTrack.isFavorite) R.drawable.ic_flowave_favorite_filled else R.drawable.ic_flowave_favorite_border),
                            contentDescription = "В избранное",
                            modifier = Modifier.size(28.dp),
                            tint = if (currentTrack.isFavorite) animatedDominantColor else Color.White
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                // --- ЗАМЕНА НА КАСТОМНЫЙ ПРОГРЕСС-БАР ---
                AuroraProgressIndicator(
                    position = uiState.currentPosition,
                    duration = uiState.duration,
                    onSeek = onSeek,
                    formatTime = formatTime,
                    accentColor = animatedDominantColor
                )
                Spacer(Modifier.height(16.dp))

                // --- ЗАМЕНА НА КАСТОМНЫЕ КНОПКИ ---
                AuroraPlayerControls(
                    isPlaying = uiState.isPlaying,
                    shuffleModeEnabled = uiState.shuffleModeEnabled,
                    repeatMode = uiState.repeatMode,
                    onTogglePlayPause = onPlayPauseClick,
                    onSkipPrevious = onSkipPreviousClick,
                    onSkipNext = onSkipNextClick,
                    onToggleShuffle = onToggleShuffleClick,
                    onToggleRepeat = onToggleRepeatClick,
                    accentColor = animatedDominantColor
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // --- ИЗМЕНЕНИЕ: Добавлен Haptic Feedback ---
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onShowLyrics()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Article, "Текст", tint = Color.White.copy(alpha = 0.8f))
                    }
                    // --- ИЗМЕНЕНИЕ: Добавлен Haptic Feedback ---
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onShowQueue()
                    }) {
                        Icon(Icons.Default.QueueMusic, "К очереди", tint = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}

@Composable
private fun AuroraBackground(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora_transition")

    val offsetOne by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(22000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "offset1"
    )
    val offsetTwo by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(19000, easing = LinearEasing), RepeatMode.Reverse),
        label = "offset2"
    )
    val offsetThree by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(25000, easing = FastOutLinearInEasing), RepeatMode.Reverse),
        label = "offset3"
    )
    val offsetFour by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(17000, easing = LinearEasing), RepeatMode.Reverse),
        label = "offset4"
    )

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF0A020F))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val brush1 = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.4f), Color.Transparent),
                center = Offset(x = size.width * offsetOne, y = size.height * offsetTwo),
                radius = size.width * 0.8f
            )
            val brush2 = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.3f), Color.Transparent),
                center = Offset(x = size.width * offsetThree, y = size.height * offsetFour),
                radius = size.width * 0.9f
            )
            val brush3 = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.2f), Color.Transparent),
                center = Offset(x = size.width * offsetFour, y = size.height * (1-offsetOne)),
                radius = size.width * 0.7f
            )
            drawRect(brush1)
            drawRect(brush2)
            drawRect(brush3)
        }
    }
}