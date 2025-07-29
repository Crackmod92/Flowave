// ФАЙЛ: AsteroidBeltNowPlayingScreen.txt
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.crackmod.flowave.R
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.presentation.player.PlayerUiState
import com.crackmod.flowave.presentation.screens.now_playing.components.AlbumArtWithGestures
import com.crackmod.flowave.presentation.screens.now_playing.components.AsteroidBeltPlayerControls
import com.crackmod.flowave.presentation.screens.now_playing.components.AsteroidBeltProgressIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// СТИЛЬ "ASTEROID BELT"
@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun AsteroidBeltNowPlayingScreen(
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
    val accentColor = Color(0xFFFF9800) // Rugged Orange
    val cockpitColor = Color(0xFF1A1A1A) // Dark Panel Color

    val glitchOffset = remember { Animatable(0f) }
    var isGlitching by remember { mutableStateOf(false) }

    LaunchedEffect(currentTrack.id) {
        launch {
            while (true) {
                delay(Random.nextLong(8000, 15000))
                isGlitching = true
                glitchOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = keyframes {
                        durationMillis = 200
                        -10f at 50
                        10f at 100
                        -5f at 150
                    }
                )
                isGlitching = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF04030A))
    ) {
        AsteroidFieldAndMeteorsBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
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
                        fontFamily = FontFamily.Monospace,
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
                IconButton(onClick = onShowOptionsMenu) { Icon(Icons.Default.MoreVert, "Опции", tint = Color.White) }
            }

            // Album Art and Title
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .aspectRatio(1f)
                        .graphicsLayer { translationX = glitchOffset.value },
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = currentTrack,
                        label = "track_transition_asteroid",
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
                            modifier = Modifier.fillMaxSize(),
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
                                    .shadow(16.dp, RoundedCornerShape(4.dp))
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            )
                        }
                    }
                    AlbumArtReticle(accentColor)
                    if (isGlitching) {
                        GlitchOverlay()
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text(
                    text = currentTrack.displayTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 32.dp).basicMarquee()
                )
                Text(
                    text = currentTrack.displayArtist,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    modifier = Modifier.clickable {
                        currentTrack.artistId?.let { onArtistClick(it) }
                        onDismissRequest()
                    }
                )
            }


            // Cockpit / Controls Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(8.dp)
                    .background(color = cockpitColor.copy(alpha = 0.8f), shape = RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SystemStatusTicker()
                Spacer(Modifier.height(8.dp))
                AsteroidBeltProgressIndicator(
                    position = uiState.currentPosition,
                    duration = uiState.duration,
                    onSeek = onSeek,
                    formatTime = formatTime,
                    accentColor = accentColor
                )
                Spacer(Modifier.height(24.dp))
                AsteroidBeltPlayerControls(
                    isPlaying = uiState.isPlaying,
                    shuffleModeEnabled = uiState.shuffleModeEnabled,
                    repeatMode = uiState.repeatMode,
                    onTogglePlayPause = onPlayPauseClick,
                    onSkipPrevious = onSkipPreviousClick,
                    onSkipNext = onSkipNextClick,
                    onToggleShuffle = onToggleShuffleClick,
                    onToggleRepeat = onToggleRepeatClick,
                    accentColor = accentColor,
                    baseColor = cockpitColor,
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
                            tint = if (currentTrack.isFavorite) accentColor else Color.White.copy(alpha = 0.7f)
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

@Composable
fun AlbumArtReticle(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "reticle_transition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Restart),
        label = "reticle_rotation"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
        label = "reticle_pulse"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val size = this.size.minDimension * pulse
        val strokeWidth = 1.5.dp.toPx()

        rotate(rotation) {
            drawArc(
                color = color,
                startAngle = 0f,
                sweepAngle = 70f,
                useCenter = false,
                style = Stroke(width = strokeWidth),
                size = androidx.compose.ui.geometry.Size(size, size),
                topLeft = Offset((this.size.width - size) / 2, (this.size.height - size) / 2)
            )
            drawArc(
                color = color,
                startAngle = 180f,
                sweepAngle = 70f,
                useCenter = false,
                style = Stroke(width = strokeWidth),
                size = androidx.compose.ui.geometry.Size(size, size),
                topLeft = Offset((this.size.width - size) / 2, (this.size.height - size) / 2)
            )
        }

        val cornerSize = size / 8
        val rectSize = this.size.width
        drawPath(
            Path().apply {
                moveTo(0f, cornerSize)
                lineTo(0f, 0f)
                lineTo(cornerSize, 0f)

                moveTo(rectSize, cornerSize)
                lineTo(rectSize, 0f)
                lineTo(rectSize - cornerSize, 0f)

                moveTo(0f, rectSize - cornerSize)
                lineTo(0f, rectSize)
                lineTo(cornerSize, rectSize)

                moveTo(rectSize, rectSize - cornerSize)
                lineTo(rectSize, rectSize)
                lineTo(rectSize - cornerSize, rectSize)
            },
            color = color.copy(alpha = 0.7f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

// --- ИЗМЕНЕНИЕ: ТИКЕР ПРЕВРАЩЕН В БЕГУЩУЮ СТРОКУ ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SystemStatusTicker() {
    val messages = remember {
        listOf(
            "СИГНАЛ: СТАБИЛЕН",
            "СЕКТОР: 7G",
            "СОСТОЯНИЕ: НОМИНАЛ.",
            "ЩИТЫ: 98%",
            "ПРЕДУПРЕЖДЕНИЕ: БЛИЗОСТЬ АСТЕРОИДОВ",
            "ЭНЕРГИЯ: 87%",
            "ДВИГАТЕЛИ: ОПТИМАЛЬНЫ",
            "СКАНЕР: АКТИВЕН",
            "ТОПЛИВО: 65%",
            "ГИПЕРПРИВОД: ГОТОВ",
            "ТЕМПЕРАТУРА: 24°C",
            "НАВИГАЦИЯ: КОРРЕКТНА",
            "СВЯЗЬ: УСТАНОВЛЕНА",
            "ГРАВИТАЦИЯ: НОРМА",
            "ДАТЧИКИ: ИСПРАВНЫ",
            "СИСТЕМЫ: ПРОВЕРЕНЫ",
            "РЕАКТОР: 75%",
            "ВНИМАНИЕ: ЧАЙ ЗАКОНЧИЛСЯ",
            "КОМПЬЮТЕР: ЗАЩИЩЕН",
            "АВАРИЯ: ДАВЛЕНИЕ В НОРМЕ",
            "ВЕНТИЛЯЦИЯ: РАБОТАЕТ",
            "ТЕЛЕМЕТРИЯ: ПЕРЕДАЧА",
            "ОШИБОК: 0",
            "РЕЖИМ: АВТОПИЛОТ",
            "ЗАРЯД ОРУЖИЯ: 100%"
        )
    }

    val tickerText = remember(messages) {
        messages.joinToString("   /   ")
    }

    Text(
        text = tickerText,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        color = Color.White.copy(alpha = 0.6f),
        maxLines = 1, // Обязательно для работы marquee
        modifier = Modifier
            .fillMaxWidth()
            .basicMarquee(
                iterations = Int.MAX_VALUE, // Бесконечный цикл
                velocity = 30.dp, // Скорость прокрутки
                initialDelayMillis = 1000 // Задержка перед началом
            )
    )
}

@Composable
fun GlitchOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val random = Random
        val lines = List(15) {
            val y = random.nextFloat() * size.height
            val xStart = random.nextFloat() * size.width * 0.2f
            val xEnd = size.width - (random.nextFloat() * size.width * 0.2f)
            Pair(Offset(xStart, y), Offset(xEnd, y))
        }
        drawPoints(
            points = lines.flatMap { listOf(it.first, it.second) },
            pointMode = PointMode.Lines,
            color = Color.White.copy(alpha = 0.3f),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private data class Asteroid(
    val path: Path,
    val initialPos: Offset,
    val velocity: Offset,
    val angularVelocity: Float
)
private class Meteor(
    var x: Float, var y: Float,
    val vx: Float, val vy: Float,
    var life: Float = 1.0f
)

@Composable
private fun AsteroidFieldAndMeteorsBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "asteroid_field_transition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(50000, easing = LinearEasing), RepeatMode.Restart),
        label = "field_time"
    )

    // Используем remember для создания ОДИН РАЗ
    val asteroids = remember {
        List(15) {
            val size = Random.nextFloat() * 40f + 30f
            val path = Path().apply {
                moveTo(Random.nextFloat() * size - size / 2, Random.nextFloat() * size - size / 2)
                repeat(8) { lineTo(Random.nextFloat() * size - size / 2, Random.nextFloat() * size - size / 2) }
                close()
            }
            Asteroid(
                path = path,
                initialPos = Offset(Random.nextFloat(), Random.nextFloat()),
                velocity = Offset(Random.nextFloat() * 0.1f + 0.05f, Random.nextFloat() * 0.02f - 0.01f),
                angularVelocity = Random.nextFloat() * 20f - 10f
            )
        }
    }

    // ОПТИМИЗАЦИЯ: Используем mutableStateListOf для метеоров
    val meteors = remember { mutableStateListOf<Meteor>() }
    // ОПТИМИЗАЦИЯ: Добавляем метеоры в LaunchedEffect, который не зависит от рекомпозиции
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(500, 3000))
            if (meteors.size < 10) { // Ограничим количество метеоров
                meteors.add(
                    Meteor(
                        x = Random.nextFloat(), y = Random.nextFloat() * 1.5f - 0.25f,
                        vx = -1.0f, vy = 1.0f
                    )
                )
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val meteorSpeed = 15f
        val lifeDecrement = 0.01f

        // Draw Asteroids
        asteroids.forEach { asteroid ->
            val currentX = (asteroid.initialPos.x * width + time * width * asteroid.velocity.x).mod(width * 1.5f) - (width * 0.25f)
            val currentY = (asteroid.initialPos.y * height + time * height * asteroid.velocity.y).mod(height * 1.2f) - (height * 0.1f)
            val currentRotation = time * asteroid.angularVelocity

            translate(left = currentX, top = currentY) {
                rotate(currentRotation) {
                    drawPath(path = asteroid.path, color = Color.Gray.copy(alpha = 0.15f))
                    drawPath(path = asteroid.path, color = Color.Gray.copy(alpha = 0.4f), style = Stroke(width = 1.dp.toPx()))
                }
            }
        }

        // ОПТИМИЗАЦИЯ: Обновляем и рисуем метеоры в одном цикле, используя итератор
        val iterator = meteors.iterator()
        while(iterator.hasNext()){
            val meteor = iterator.next()
            meteor.x += meteor.vx * meteorSpeed / width
            meteor.y += meteor.vy * meteorSpeed / height
            meteor.life -= lifeDecrement

            if(meteor.life <= 0f) {
                iterator.remove()
            } else {
                val currentPos = Offset(meteor.x * width, meteor.y * height)
                val tailStart = Offset((meteor.x - meteor.vx * 0.1f) * width, (meteor.y - meteor.vy * 0.1f) * height)
                drawLine(
                    brush = Brush.linearGradient(colors = listOf(Color.Transparent, Color.White.copy(alpha = meteor.life)), start = tailStart, end = currentPos),
                    start = tailStart, end = currentPos,
                    strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round
                )
            }
        }
    }
}