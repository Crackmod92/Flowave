// ПУТЬ: /app/src/main/java/com/crackmod/flowave/presentation/screens/playlist_detail/PlaylistDetailScreen.kt
package com.crackmod.flowave.presentation.screens.playlist_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.presentation.components.EmptyContent
import com.crackmod.flowave.presentation.player.PlayerViewModel
import com.crackmod.flowave.presentation.screens.home.components.ScanStatusBanner
import com.crackmod.flowave.presentation.screens.library.LibraryViewModel
import com.crackmod.flowave.presentation.screens.library.components.ConfirmationDialog
import com.crackmod.flowave.presentation.screens.library.components.TrackListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    onBackPress: () -> Unit,
    onShowTrackOptions: (Track) -> Unit,
    viewModel: PlaylistDetailViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel,
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val playerUiState by playerViewModel.uiState.collectAsState()
    val bannerState by libraryViewModel.bannerState.collectAsState()
    val showDeletePlaylistConfirmation by viewModel.showDeleteConfirmation.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }

    if (showDeletePlaylistConfirmation) {
        ConfirmationDialog(
            title = "Удалить плейлист",
            text = "Вы уверены, что хотите удалить плейлист \"${state.playlist?.name}\"? Это действие необратимо.",
            onConfirm = {
                viewModel.deletePlaylist()
                onBackPress()
            },
            onDismiss = { viewModel.onDismissDeleteConfirmation() }
        )
    }

    val lazyListState = rememberLazyListState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            val scrollOffset = lazyListState.firstVisibleItemScrollOffset.toFloat()
            val headerSize = 250f
            val alphaValue = (scrollOffset / headerSize).coerceIn(0f, 1f)
            val scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = alphaValue)

            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = state.playlist?.name ?: "",
                        maxLines = 1,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.graphicsLayer {
                            alpha = ((scrollOffset - (headerSize * 0.75f)) / (headerSize * 0.25f)).coerceIn(0f, 1f)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    if (state.playlist?.isSystem == false) {
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, "Меню плейлиста")
                            }
                            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("Удалить плейлист") },
                                    onClick = {
                                        viewModel.onShowDeleteConfirmation()
                                        menuExpanded = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = scrolledContainerColor
                ),
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = "Ошибка: ${state.error}") }
                else -> {
                    val bottomPadding = if (playerUiState.isPlayerVisible) 80.dp else 16.dp
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = lazyListState,
                            contentPadding = PaddingValues(
                                top = paddingValues.calculateTopPadding(),
                                bottom = bottomPadding
                            )
                        ) {
                            item {
                                state.playlist?.let { playlist ->
                                    PlaylistHeader(
                                        name = playlist.name,
                                        trackCount = state.tracks.size,
                                        scrollOffset = { lazyListState.firstVisibleItemScrollOffset.toFloat() },
                                        onPlayClick = { playerViewModel.playTrackList(state.tracks, 0) },
                                        onShuffleClick = {
                                            playerViewModel.playTrackList(state.tracks, 0, shuffle = true)
                                        }
                                    )
                                }
                            }

                            if (state.tracks.isEmpty()) {
                                item {
                                    EmptyContent(
                                        title = "Плейлист пуст",
                                        subtitle = "Добавьте треки в медиатеке, чтобы они появились здесь."
                                    )
                                }
                            } else {
                                itemsIndexed(state.tracks, key = { _, track -> track.id }) { index, track ->
                                    TrackListItem(
                                        track = track,
                                        isPlaying = playerUiState.currentTrack?.id == track.id,
                                        onTrackClick = { playerViewModel.playTrackList(state.tracks, index) },
                                        onFavoriteClick = { libraryViewModel.toggleFavorite(it) },
                                        onLongClick = { onShowTrackOptions(it) },
                                        trailingContent = {
                                            IconButton(onClick = { onShowTrackOptions(track) }) {
                                                Icon(Icons.Default.MoreVert, "Опции трека")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            ScanStatusBanner(
                isVisible = bannerState.isVisible,
                message = bannerState.message,
                isError = bannerState.isError,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun PlaylistHeader(
    name: String,
    trackCount: Int,
    scrollOffset: () -> Float,
    onPlayClick: () -> Unit,
    onShuffleClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = 1f - (scrollOffset() / 400f).coerceIn(0f, 1f)
                translationY = scrollOffset() * 0.5f
            }
            .padding(horizontal = 16.dp), // --- ИЗМЕНЕНИЕ: Убрали общий padding
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(200.dp)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = "Иконка плейлиста",
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            text = "Плейлист • $trackCount треков",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            Button(onClick = onPlayClick, modifier = Modifier
                .weight(1f)
                .height(56.dp), enabled = trackCount > 0) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Играть")
            }
            OutlinedButton(onClick = onShuffleClick, modifier = Modifier
                .weight(1f)
                .height(56.dp), enabled = trackCount > 0) {
                Icon(Icons.Default.Shuffle, contentDescription = null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Перемешать")
            }
        }
    }
}