package com.crackmod.flowave.presentation.screens.lyrics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.MusicNote // ИМПОРТИРУЕМ ИКОНКУ
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crackmod.flowave.domain.model.LyricsLine
import com.crackmod.flowave.presentation.player.PlayerUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsScreen(
    trackId: Long,
    playerUiState: PlayerUiState,
    onBackPress: () -> Unit,
    onLineClick: (Long) -> Unit,
    viewModel: LyricsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(trackId) {
        viewModel.fetchLyrics(trackId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Текст песни") },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад к плееру")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is LyricsUiState.Loading -> CircularProgressIndicator()
                is LyricsUiState.Error -> Text(state.message, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                is LyricsUiState.NotFound -> Text("Текст для этого трека не найден.", textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                is LyricsUiState.Success -> {
                    if (state.lyrics.isInstrumental || state.lyrics.lines.isEmpty()) {
                        Text(
                            text = if (state.lyrics.isInstrumental) "Инструментальная композиция" else "Текст для этого трека отсутствует.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        SyncedLyricsContent(
                            lyricsLines = state.lyrics.lines,
                            currentPosition = playerUiState.currentPosition,
                            onLineClick = onLineClick
                        )
                    }
                }
            }
        }
    }
}

/**
 * Изолированный, высокопроизводительный компонент для одной строки текста.
 * Он будет перерисовываться только когда его собственный статус `isActive` меняется.
 */
@Composable
private fun LyricLineItem(
    lineContent: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val color by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        animationSpec = tween(durationMillis = 400),
        label = "lyric_line_color"
    )

    val style = if (isActive) {
        MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
    } else {
        MaterialTheme.typography.titleMedium
    }

    // ИЗМЕНЕНИЕ: Мы используем Box, чтобы центрировать контент (текст или иконку)
    // и применяем общие модификаторы к нему.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (lineContent.isBlank()) {
            // Если строка пустая, показываем иконку
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Инструментальный проигрыш",
                tint = color,
                // Размер иконки будет соответствовать размеру текста
                modifier = Modifier.size(style.fontSize.value.dp)
            )
        } else {
            // Если в строке есть текст, показываем его
            Text(
                text = lineContent,
                color = color,
                style = style,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SyncedLyricsContent(
    lyricsLines: List<LyricsLine>,
    currentPosition: Long,
    onLineClick: (Long) -> Unit
) {
    val lazyListState = rememberLazyListState()
    var isAutoScrollEnabled by remember { mutableStateOf(true) }

    val isDragged by lazyListState.interactionSource.collectIsDraggedAsState()
    if (isDragged) {
        isAutoScrollEnabled = false
    }

    var activeLineIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(currentPosition, lyricsLines) {
        val newIndex = lyricsLines.indexOfLast { it.startTimeMs <= currentPosition }.coerceAtLeast(0)
        if (newIndex != activeLineIndex) {
            activeLineIndex = newIndex
        }
    }

    LaunchedEffect(activeLineIndex) {
        if (isAutoScrollEnabled && lazyListState.layoutInfo.visibleItemsInfo.isNotEmpty()) {
            lazyListState.animateScrollToItem(activeLineIndex)
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.background

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val containerHeight = this.maxHeight

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    val brush = Brush.verticalGradient(
                        0.0f to backgroundColor,
                        0.2f to Color.Transparent,
                        0.8f to Color.Transparent,
                        1.0f to backgroundColor
                    )
                    drawRect(brush = brush)
                },
            contentPadding = PaddingValues(vertical = containerHeight / 2 - 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(
                items = lyricsLines,
                key = { index, item -> item.startTimeMs.toString() + item.content + index }
            ) { index, line ->
                LyricLineItem(
                    lineContent = line.content,
                    isActive = index == activeLineIndex,
                    onClick = {
                        onLineClick(line.startTimeMs)
                        isAutoScrollEnabled = true
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = !isAutoScrollEnabled,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            FilledTonalButton(
                onClick = { isAutoScrollEnabled = true },
            ) {
                Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Следить за текстом")
            }
        }
    }
}