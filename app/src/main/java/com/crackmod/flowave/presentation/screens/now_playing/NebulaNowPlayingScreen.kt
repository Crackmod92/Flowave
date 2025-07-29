package com.crackmod.flowave.presentation.screens.now_playing

import android.content.ContentUris
import android.graphics.drawable.BitmapDrawable
import android.provider.MediaStore
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.crackmod.flowave.R
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.presentation.player.PlayerUiState
import com.crackmod.flowave.presentation.screens.now_playing.components.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun NebulaNowPlayingScreen(
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
    val context = LocalContext.current
    val fallbackColor = MaterialTheme.colorScheme.primary
    var paletteColors by remember { mutableStateOf<List<Color>>(listOf(fallbackColor)) }

    val albumArtUri = remember(currentTrack.albumId) {
        currentTrack.albumId?.let {
            ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, it)
        }
    }

    LaunchedEffect(albumArtUri) {
        if (albumArtUri == null) {
            paletteColors = listOf(fallbackColor)
            return@LaunchedEffect
        }
        scope.launch(Dispatchers.IO) {
            try {
                val request = ImageRequest.Builder(context)
                    .data(albumArtUri)
                    .allowHardware(false)
                    .build()
                val result = (context.imageLoader.execute(request).drawable as? BitmapDrawable)?.bitmap
                if (result != null) {
                    val newPalette = Palette.from(result).generate()
                    val colors = listOfNotNull(
                        newPalette.vibrantSwatch,
                        newPalette.mutedSwatch,
                        newPalette.dominantSwatch
                    ).map { Color(it.rgb) }

                    withContext(Dispatchers.Main) {
                        paletteColors = colors.ifEmpty { listOf(fallbackColor) }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    paletteColors = listOf(fallbackColor)
                }
            }
        }
    }

    val accentColor = paletteColors.first()

    Box(modifier = Modifier.fillMaxSize()) {
        NebulaBackground(colors = paletteColors, modifier = Modifier.fillMaxSize())

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.KeyboardArrowDown, "Свернуть", tint = Color.White)
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

            AnimatedContent(
                targetState = currentTrack,
                label = "track_transition_nebula",
                transitionSpec = { fadeIn(tween(800)) togetherWith fadeOut(tween(800)) },
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp)
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
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1f),
                    key = track.id
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(track.albumId?.let { ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, it) })
                            .crossfade(true)
                            .error(R.drawable.ic_flowave_album)
                            .build(),
                        contentDescription = "Обложка альбома",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .shadow(elevation = 20.dp, shape = RoundedCornerShape(20.dp), spotColor = accentColor)
                            .clip(RoundedCornerShape(20.dp))
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            currentTrack.displayTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee(),
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
                            tint = if (currentTrack.isFavorite) accentColor else Color.White
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                NebulaProgressIndicator(
                    position = uiState.currentPosition,
                    duration = uiState.duration,
                    onSeek = onSeek,
                    formatTime = formatTime,
                    accentColor = accentColor
                )
                Spacer(Modifier.height(8.dp))

                NebulaPlayerControls(
                    isPlaying = uiState.isPlaying,
                    shuffleModeEnabled = uiState.shuffleModeEnabled,
                    repeatMode = uiState.repeatMode,
                    onTogglePlayPause = onPlayPauseClick,
                    onSkipPrevious = onSkipPreviousClick,
                    onSkipNext = onSkipNextClick,
                    onToggleShuffle = onToggleShuffleClick,
                    onToggleRepeat = onToggleRepeatClick,
                    accentColor = accentColor,
                    contentColor = Color.White
                )

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onShowLyrics()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Article, "Текст", tint = Color.White.copy(alpha = 0.8f))
                    }
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