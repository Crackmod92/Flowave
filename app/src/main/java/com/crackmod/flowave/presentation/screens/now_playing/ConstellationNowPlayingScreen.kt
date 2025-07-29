// ФАЙЛ: ConstellationNowPlayingScreen.txt
package com.crackmod.flowave.presentation.screens.now_playing

import android.content.ContentUris
import android.provider.MediaStore
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.withTransform
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
import com.crackmod.flowave.presentation.screens.now_playing.components.ConstellationPlayerControls
import com.crackmod.flowave.presentation.screens.now_playing.components.ConstellationProgressIndicator
import kotlinx.coroutines.CoroutineScope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// --- ОБНОВЛЕННАЯ СТРУКТУРА ДАННЫХ ДЛЯ СОЗВЕЗДИЙ ---
private data class ConstellationData(
    val name: String,
    val stars: List<Offset>,
    val connections: List<Pair<Int, Int>>,
    val isCluster: Boolean = false // Флаг для звездных скоплений типа Плеяд
)

// --- РАСШИРЕННЫЙ СПИСОК СОЗВЕЗДИЙ ---
private val predefinedConstellations = listOf(
    // Старые
    ConstellationData("Ursa Major",
        stars = listOf(Offset(0.1f, 0.3f), Offset(0.3f, 0.35f), Offset(0.5f, 0.4f), Offset(0.7f, 0.42f), Offset(0.8f, 0.6f), Offset(0.95f, 0.55f), Offset(0.85f, 0.35f)),
        connections = listOf(0 to 1, 1 to 2, 2 to 3, 3 to 4, 3 to 6, 4 to 5, 6 to 5)
    ),
    ConstellationData("Orion",
        stars = listOf(Offset(0.2f, 0.1f), Offset(0.8f, 0.2f), Offset(0.4f, 0.4f), Offset(0.5f, 0.5f), Offset(0.6f, 0.6f), Offset(0.3f, 0.85f), Offset(0.8f, 0.8f)),
        connections = listOf(0 to 2, 1 to 4, 2 to 3, 3 to 4, 2 to 5, 4 to 6, 0 to 1, 5 to 6)
    ),
    // Новые и красивые
    ConstellationData("Cassiopeia",
        stars = listOf(Offset(0.1f, 0.4f), Offset(0.3f, 0.2f), Offset(0.5f, 0.5f), Offset(0.7f, 0.2f), Offset(0.9f, 0.4f)),
        connections = listOf(0 to 1, 1 to 2, 2 to 3, 3 to 4)
    ),
    ConstellationData("Cygnus", // Лебедь
        stars = listOf(Offset(0.5f, 0.1f), Offset(0.2f, 0.5f), Offset(0.8f, 0.5f), Offset(0.5f, 0.6f), Offset(0.5f, 0.9f)),
        connections = listOf(0 to 3, 1 to 3, 2 to 3, 3 to 4)
    ),
    ConstellationData("Scorpius", // Скорпион
        stars = listOf(Offset(0.2f, 0.2f), Offset(0.4f, 0.1f), Offset(0.6f, 0.25f), Offset(0.7f, 0.45f), Offset(0.65f, 0.65f), Offset(0.5f, 0.8f), Offset(0.4f, 0.9f)),
        connections = listOf(0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 6)
    ),
    ConstellationData("Pleiades", // Плеяды (звездное скопление)
        stars = listOf(Offset(0.4f, 0.3f), Offset(0.5f, 0.35f), Offset(0.45f, 0.45f), Offset(0.55f, 0.5f), Offset(0.65f, 0.4f), Offset(0.6f, 0.25f), Offset(0.7f, 0.55f)),
        connections = emptyList(),
        isCluster = true
    )
)


// СТИЛЬ "CONSTELLATION"
@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun ConstellationNowPlayingScreen(
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
    val accentColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0F18)) // Deep space blue
    ) {
        ConstellationBackground(
            trackId = currentTrack.id,
            accentColor = accentColor
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with album link
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

            Spacer(modifier = Modifier.weight(0.5f))

            // Album Art
            AnimatedContent(
                targetState = currentTrack,
                label = "track_transition_constellation",
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
                        .fillMaxWidth(0.7f)
                        .aspectRatio(1f),
                    key = track.id
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(track.albumId?.let { ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, it) })
                            .crossfade(true)
                            .error(R.drawable.ic_flowave_album)
                            .build(),
                        contentDescription = "Обложка альбома",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(HexagonShape)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Track Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp)
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
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ConstellationProgressIndicator(
                    position = uiState.currentPosition,
                    duration = uiState.duration,
                    onSeek = onSeek,
                    formatTime = formatTime,
                    accentColor = accentColor
                )
                Spacer(Modifier.height(24.dp))
                ConstellationPlayerControls(
                    isPlaying = uiState.isPlaying,
                    shuffleModeEnabled = uiState.shuffleModeEnabled,
                    repeatMode = uiState.repeatMode,
                    onTogglePlayPause = onPlayPauseClick,
                    onSkipPrevious = onSkipPreviousClick,
                    onSkipNext = onSkipNextClick,
                    onToggleShuffle = onToggleShuffleClick,
                    onToggleRepeat = onToggleRepeatClick,
                    accentColor = accentColor
                )
                Spacer(Modifier.height(24.dp))
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
                            tint = if (currentTrack.isFavorite) accentColor else Color.White.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onShowLyrics()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Article, "Текст", tint = Color.White.copy(alpha = 0.7f))
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onShowQueue()
                    }) {
                        Icon(Icons.Default.QueueMusic, "К очереди", tint = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

val HexagonShape = object : Shape {
    override fun createOutline(size: Size, layoutDirection: androidx.compose.ui.unit.LayoutDirection, density: androidx.compose.ui.unit.Density): Outline {
        val path = Path().apply {
            val radius = size.minDimension / 2f
            val angle = 2.0 * PI / 6
            moveTo(
                x = size.width / 2 + radius * cos(0.0).toFloat(),
                y = size.height / 2 + radius * sin(0.0).toFloat()
            )
            for (i in 1..6) {
                lineTo(
                    x = size.width / 2 + radius * cos(angle * i).toFloat(),
                    y = size.height / 2 + radius * sin(angle * i).toFloat()
                )
            }
            close()
        }
        return Outline.Generic(path)
    }
}


@Composable
private fun ConstellationBackground(trackId: Long, accentColor: Color) {
    val backgroundStars = remember {
        List(200) { // Увеличим количество фоновых звезд
            Offset(
                x = Random.nextFloat(),
                y = Random.nextFloat()
            ) to Random.nextFloat() * 2.0f // Сделаем их чуть меньше, чтобы созвездия выделялись
        }
    }

    // --- НОВАЯ ЛОГИКА: Выбор и размещение нескольких созвездий ---
    val constellationsToDraw = remember(trackId) {
        val random = Random(trackId)
        val shuffledConstellations = predefinedConstellations.shuffled(random)
        // Определяем "квадранты" для размещения созвездий
        val quadrants = listOf(
            Rect(0.05f, 0.05f, 0.45f, 0.45f), // Top-left
            Rect(0.55f, 0.05f, 0.95f, 0.45f), // Top-right
            Rect(0.3f, 0.55f, 0.7f, 0.95f)    // Bottom-center
        ).shuffled(random)

        // Выбираем 2 или 3 созвездия и назначаем им квадрант
        shuffledConstellations.take(random.nextInt(2, 4)).mapIndexed { index, data ->
            data to quadrants[index]
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Рисуем общий фон из звезд
        backgroundStars.forEach { (pos, radius) ->
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = radius,
                center = Offset(pos.x * width, pos.y * height)
            )
        }

        // 2. Рисуем выбранные созвездия в их квадрантах
        constellationsToDraw.forEach { (constellation, quadrant) ->
            withTransform({
                // Переносим начало координат в левый верхний угол квадранта
                translate(left = width * quadrant.left, top = height * quadrant.top)
            }) {
                val quadrantWidth = width * quadrant.width
                val quadrantHeight = height * quadrant.height

                // Рисуем звезды созвездия
                val starPositions = constellation.stars.map {
                    Offset(it.x * quadrantWidth, it.y * quadrantHeight)
                }

                starPositions.forEach { pos ->
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accentColor.copy(alpha = 0.4f), Color.Transparent),
                            center = pos,
                            radius = 10.dp.toPx()
                        ),
                        radius = 10.dp.toPx(),
                        center = pos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = pos
                    )
                }

                // Рисуем линии, если это не скопление
                if (!constellation.isCluster) {
                    constellation.connections.forEach { (startIdx, endIdx) ->
                        val start = starPositions[startIdx]
                        val end = starPositions[endIdx]
                        drawLine(
                            color = accentColor.copy(alpha = 0.7f),
                            start = start,
                            end = end,
                            strokeWidth = 1.dp.toPx(),
                        )
                    }
                }
            }
        }
    }
}