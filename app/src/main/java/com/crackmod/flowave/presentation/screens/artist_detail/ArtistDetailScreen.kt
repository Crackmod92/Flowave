// ПУТЬ: /app/src/main/java/com/crackmod/flowave/presentation/screens/artist_detail/ArtistDetailScreen.kt
package com.crackmod.flowave.presentation.screens.artist_detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crackmod.flowave.domain.model.Artist
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.presentation.player.PlayerViewModel
import com.crackmod.flowave.presentation.screens.library.LibraryViewModel
import com.crackmod.flowave.presentation.screens.library.components.AlbumGridItem
import com.crackmod.flowave.presentation.screens.library.components.TrackListItem
import com.crackmod.flowave.presentation.screens.search.components.SectionHeader
import com.crackmod.flowave.R

private const val TRACKS_PER_PAGE = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    onBackPress: () -> Unit,
    onAlbumClick: (Long) -> Unit,
    onShowTrackOptions: (Track) -> Unit,
    viewModel: ArtistDetailViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel,
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val playerUiState by playerViewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()

    val surfaceColor = MaterialTheme.colorScheme.surface
    val gradientBrush = remember {
        Brush.verticalGradient(
            colors = listOf(surfaceColor.copy(alpha = 0.5f), Color.Transparent),
            endY = 300f
        )
    }

    val scrolledContainerColor by remember {
        derivedStateOf {
            if (lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 200f) {
                surfaceColor
            } else {
                surfaceColor.copy(alpha = (lazyListState.firstVisibleItemScrollOffset / 200f).coerceIn(0f, 1f))
            }
        }
    }

    val titleAlpha by remember {
        derivedStateOf {
            if (lazyListState.firstVisibleItemIndex == 0) {
                val scrollOffset = lazyListState.firstVisibleItemScrollOffset.toFloat()
                val headerHeightPx = 200f
                ((scrollOffset - (headerHeightPx * 0.7f)) / (headerHeightPx * 0.3f)).coerceIn(0f, 1f)
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
                .height(300.dp)
                .background(gradientBrush)
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = state.artist?.displayName ?: "",
                            modifier = Modifier.graphicsLayer { alpha = titleAlpha },
                            maxLines = 1,
                            fontWeight = FontWeight.Bold,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackPress) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
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
            when {
                state.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = "Ошибка: ${state.error}") }
                else -> {
                    val bottomPadding = if (playerUiState.isPlayerVisible) 80.dp else 16.dp
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        state = lazyListState,
                        contentPadding = PaddingValues(bottom = bottomPadding)
                    ) {
                        item {
                            state.artist?.let { artist ->
                                ArtistDetailHeader(
                                    artist = artist,
                                    onPlayClick = { playerViewModel.playTrackList(state.tracks, 0) },
                                    onShuffleClick = { playerViewModel.playTrackList(state.tracks, 0, shuffle = true) }
                                )
                            }
                        }

                        if (state.albums.isNotEmpty()) {
                            item { SectionHeader("Альбомы") }
                            item {
                                AlbumsRow(
                                    albums = state.albums,
                                    onAlbumClick = onAlbumClick
                                )
                            }
                        }

                        if (state.tracks.isNotEmpty()) {
                            item { SectionHeader("Популярные треки") }
                            item {
                                PopularTracksPager(
                                    tracks = state.tracks,
                                    playerUiState = playerUiState,
                                    onTrackClick = { track ->
                                        val index = state.tracks.indexOf(track)
                                        playerViewModel.playTrackList(state.tracks, index)
                                    },
                                    onFavoriteClick = { track -> libraryViewModel.toggleFavorite(track) },
                                    onShowTrackOptions = onShowTrackOptions
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumsRow(
    albums: List<com.crackmod.flowave.domain.model.Album>,
    onAlbumClick: (Long) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(albums, key = { it.id }) { album ->
            Box(modifier = Modifier.width(140.dp)) {
                AlbumGridItem(album = album, onAlbumClick = { onAlbumClick(album.id) })
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PopularTracksPager(
    tracks: List<Track>,
    playerUiState: com.crackmod.flowave.presentation.player.PlayerUiState,
    onTrackClick: (Track) -> Unit,
    onFavoriteClick: (Track) -> Unit,
    onShowTrackOptions: (Track) -> Unit
) {
    val trackPages = remember(tracks) { tracks.chunked(TRACKS_PER_PAGE) }
    if (trackPages.isEmpty()) return

    val pagerState = rememberPagerState { trackPages.size }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) { pageIndex ->
            val page = trackPages[pageIndex]
            Column {
                page.forEach { track ->
                    val onTrackClickStable = remember<(Track) -> Unit> { { onTrackClick(it) } }
                    val onFavoriteClickStable = remember<(Track) -> Unit> { { onFavoriteClick(it) } }
                    val onShowTrackOptionsStable = remember<(Track) -> Unit> { { onShowTrackOptions(it) } }

                    TrackListItem(
                        track = track,
                        isPlaying = playerUiState.currentTrack?.id == track.id,
                        onTrackClick = { onTrackClickStable(track) },
                        onFavoriteClick = { onFavoriteClickStable(track) },
                        onLongClick = { onShowTrackOptionsStable(track) },
                        trailingContent = {
                            IconButton(onClick = { onShowTrackOptionsStable(track) }) {
                                Icon(Icons.Default.MoreVert, "Опции трека")
                            }
                        }
                    )
                }
            }
        }

        if (trackPages.size > 1) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                Modifier.height(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun ArtistDetailHeader(
    artist: Artist,
    onPlayClick: () -> Unit,
    onShuffleClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_flowave_artist_placeholder),
                contentDescription = "Аватар исполнителя",
                modifier = Modifier
                    .size(100.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = artist.displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatCountable(artist.albumCount, "альбом", "альбома", "альбомов"),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCountable(artist.trackCount, "трек", "трека", "треков"),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onPlayClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                enabled = artist.trackCount > 0
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Играть")
            }
            OutlinedButton(
                onClick = onShuffleClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                enabled = artist.trackCount > 0
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Перемешать")
            }
        }
    }
}

private fun formatCountable(count: Int, one: String, few: String, many: String): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "$count $one"
        count % 10 in 2..4 && count % 100 !in 12..14 -> "$count $few"
        else -> "$count $many"
    }
}