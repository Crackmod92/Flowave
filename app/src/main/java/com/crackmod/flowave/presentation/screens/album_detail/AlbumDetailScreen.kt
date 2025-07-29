// ПУТЬ: /app/src/main/java/com/crackmod/flowave/presentation/screens/album_detail/AlbumDetailScreen.kt
package com.crackmod.flowave.presentation.screens.album_detail

import android.content.ContentUris
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.crackmod.flowave.R
import com.crackmod.flowave.domain.model.Album
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.presentation.components.EmptyContent
import com.crackmod.flowave.presentation.player.PlayerViewModel
import com.crackmod.flowave.presentation.screens.library.LibraryViewModel
import com.crackmod.flowave.presentation.screens.library.components.AlbumGridItem
import com.crackmod.flowave.presentation.screens.library.components.TrackListItem
import com.crackmod.flowave.presentation.screens.search.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    onBackPress: () -> Unit,
    onArtistClick: (Long) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onShowTrackOptions: (Track) -> Unit,
    viewModel: AlbumDetailViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel,
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val playerUiState by playerViewModel.uiState.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()

    val dominantColor = state.dominantColor ?: MaterialTheme.colorScheme.surfaceVariant
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(dominantColor.copy(alpha = 0.4f), Color.Transparent),
        endY = 500f
    )

    val surfaceColor = MaterialTheme.colorScheme.surface

    val scrolledContainerColor by remember {
        derivedStateOf {
            val firstVisibleItemIndex = lazyListState.firstVisibleItemIndex
            if (firstVisibleItemIndex == 0) {
                val scrollOffset = lazyListState.firstVisibleItemScrollOffset.toFloat()
                val headerHeightPx = 250f
                val alpha = (scrollOffset / headerHeightPx).coerceIn(0f, 1f)
                surfaceColor.copy(alpha = alpha)
            } else {
                surfaceColor
            }
        }
    }

    val titleAlpha by remember {
        derivedStateOf {
            val firstVisibleItemIndex = lazyListState.firstVisibleItemIndex
            if (firstVisibleItemIndex == 0) {
                val scrollOffset = lazyListState.firstVisibleItemScrollOffset.toFloat()
                val headerHeightPx = 250f
                ((scrollOffset - (headerHeightPx * 0.8f)) / (headerHeightPx * 0.2f)).coerceIn(0f, 1f)
            } else {
                1f
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = state.album?.displayTitle ?: "",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.graphicsLayer { alpha = titleAlpha }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackPress) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { menuExpanded = true }) { Icon(Icons.Default.MoreVert, "Опции") }
                            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("Удалить альбом") },
                                    onClick = {
                                        viewModel.onDeleteRequest()
                                        menuExpanded = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = scrolledContainerColor
                    ),
                    // Важно, чтобы TopAppBar реагировал на прокрутку
                    scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
                )
            }
        ) { paddingValues ->
            when {
                state.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = "Ошибка: ${state.error}") }
                else -> {
                    val bottomPadding = if (playerUiState.isPlayerVisible) 80.dp else 16.dp
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(bottom = bottomPadding)
                    ) {
                        item {
                            state.album?.let { album ->
                                CompactAlbumHeader(
                                    album = album,
                                    totalDuration = state.totalDurationFormatted,
                                    trackCount = state.tracks.size,
                                    onPlayClick = { playerViewModel.playTrackList(state.tracks, 0) },
                                    onShuffleClick = {
                                        playerViewModel.playTrackList(state.tracks, 0, shuffle = true)
                                    },
                                    onArtistClick = { state.tracks.firstOrNull()?.artistId?.let { onArtistClick(it) } }
                                )
                            }
                        }

                        if (state.tracks.isEmpty()) {
                            item { EmptyContent(title = "В этом альбоме нет треков.", subtitle = "Возможно, файлы были перемещены или удалены.") }
                        } else {
                            itemsIndexed(state.tracks, key = { _, track -> track.id }) { index, track ->
                                TrackListItem(
                                    track = track,
                                    trackNumber = index + 1,
                                    showAlbumArt = false,
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

                        if (state.otherAlbumsByArtist.isNotEmpty()) {
                            item { SectionHeader("Другие альбомы исполнителя") }
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(state.otherAlbumsByArtist, key = { it.id }) { album ->
                                        Box(modifier = Modifier.width(160.dp)) {
                                            AlbumGridItem(album = album, onAlbumClick = { onAlbumClick(it.id) })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactAlbumHeader(
    album: Album,
    totalDuration: String,
    trackCount: Int,
    onPlayClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onArtistClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            val albumArtUri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, album.id)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(albumArtUri).crossfade(true).error(R.drawable.ic_flowave_album).placeholder(R.drawable.ic_flowave_album).build(),
                contentDescription = "Обложка альбома",
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = album.displayTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = album.displayArtist,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.clickable { onArtistClick() },
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))

                val metaInfo = mutableListOf<String>()
                album.year?.let { metaInfo.add(it.toString()) }
                if (trackCount > 0) metaInfo.add("$trackCount треков")
                if (totalDuration.isNotBlank()) metaInfo.add(totalDuration)

                if (metaInfo.isNotEmpty()) {
                    Text(
                        text = "Альбом • ${metaInfo.joinToString(" • ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start)
        ) {
            Button(onClick = onPlayClick, modifier = Modifier
                .weight(1f)
                .height(48.dp)) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Играть")
            }
            OutlinedButton(onClick = onShuffleClick, modifier = Modifier
                .weight(1f)
                .height(48.dp)) {
                Icon(Icons.Default.Shuffle, contentDescription = null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Перемешать")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}