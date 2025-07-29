// ПУТЬ: /app/src/main/java/com/crackmod/flowave/presentation/screens/search/SearchScreen.kt
package com.crackmod.flowave.presentation.screens.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crackmod.flowave.R
import com.crackmod.flowave.domain.model.Album
import com.crackmod.flowave.domain.model.Artist
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.presentation.components.EmptyContent
import com.crackmod.flowave.presentation.components.FlowaveTopAppBar
import com.crackmod.flowave.presentation.player.PlayerViewModel
import com.crackmod.flowave.presentation.screens.library.LibraryViewModel
import com.crackmod.flowave.presentation.screens.library.components.AlbumGridItem
import com.crackmod.flowave.presentation.screens.library.components.ArtistListItem
import com.crackmod.flowave.presentation.screens.library.components.TrackListItem
import com.crackmod.flowave.presentation.screens.search.components.SectionHeader
import com.crackmod.flowave.presentation.screens.search.components.TopSearchResultItem
import kotlin.math.absoluteValue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onAlbumClick: (Long) -> Unit,
    onArtistClick: (Long) -> Unit,
    onShowTrackOptions: (Track) -> Unit,
    playerViewModel: PlayerViewModel,
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.searchQuery.collectAsState()
    val state by viewModel.uiState.collectAsState()
    val playerUiState by playerViewModel.uiState.collectAsState()

    var isSearchActive by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            FlowaveTopAppBar(
                title = "Поиск",
                navigationIcon = null, // На экране поиска нет кнопки "назад" в обычном режиме
                isSearchable = true,
                searchQuery = query,
                onSearchQueryChange = viewModel::onQueryChange,
                isSearchActive = isSearchActive,
                // Когда пользователь закрывает поиск, мы просто очищаем поле, но остаемся на экране
                onToggleSearch = {
                    isSearchActive = !isSearchActive
                    if (!isSearchActive) {
                        viewModel.onQueryChange("")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                query.isBlank() -> {
                    EmptyContent(
                        title = "Ищите в своей медиатеке",
                        subtitle = "Находите любимые композиции и открывайте для себя что-то новое.",
                        drawableIconResId = R.drawable.ic_flowave_search
                    )
                }
                !state.hasResults -> {
                    EmptyContent(
                        title = "Ничего не найдено",
                        subtitle = "По запросу \"$query\" ничего не найдено. Попробуйте другой запрос.",
                        drawableIconResId = R.drawable.ic_flowave_music_off
                    )
                }
                else -> {
                    SearchResults(
                        state = state,
                        playerUiState = playerUiState,
                        onResultClick = { result ->
                            when (result) {
                                is Track -> playerViewModel.playTrackList(listOf(result), 0)
                                is Album -> onAlbumClick(result.id)
                                is Artist -> onArtistClick(result.id)
                            }
                        },
                        onTrackClick = { tracks, index ->
                            playerViewModel.playTrackList(tracks, index)
                        },
                        onShowTrackOptions = onShowTrackOptions,
                        onToggleFavorite = { libraryViewModel.toggleFavorite(it) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchResults(
    state: SearchUiState,
    playerUiState: com.crackmod.flowave.presentation.player.PlayerUiState,
    onResultClick: (Any) -> Unit,
    onTrackClick: (List<Track>, Int) -> Unit,
    onShowTrackOptions: (Track) -> Unit,
    onToggleFavorite: (Track) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = if (playerUiState.isPlayerVisible) 80.dp else 16.dp)
    ) {
        state.topResult?.let {
            item {
                SectionHeader("Лучший результат")
                TopSearchResultItem(result = it, onResultClick = onResultClick)
            }
        }

        if (state.tracks.isNotEmpty()) {
            item { SectionHeader("Треки") }
            item {
                val trackPages = state.tracks.chunked(4)
                val pagerState = rememberPagerState { trackPages.size }
                val allTracksForPlayback = listOfNotNull(state.topResult as? Track) + state.tracks

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) { pageIndex ->
                        val page = trackPages[pageIndex]
                        val pageOffset = (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                        val scale = lerp(0.85f, 1f, 1f - pageOffset.absoluteValue.coerceIn(0f, 1f))

                        Column(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .padding(horizontal = 8.dp)
                        ) {
                            page.forEach { track ->
                                TrackListItem(
                                    track = track,
                                    isPlaying = playerUiState.currentTrack?.id == track.id,
                                    onTrackClick = {
                                        val index = allTracksForPlayback.indexOf(it)
                                        onTrackClick(allTracksForPlayback, index)
                                    },
                                    onLongClick = onShowTrackOptions,
                                    showFavoriteButton = false,
                                    showTrailingContent = false,
                                )
                            }
                        }
                    }

                    if (trackPages.size > 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            Modifier.height(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(trackPages.size) { iteration ->
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
        }

        if (state.albums.isNotEmpty()) {
            item { SectionHeader("Альбомы") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.albums, key = { it.id }) { album ->
                        Box(modifier = Modifier.width(160.dp)) {
                            AlbumGridItem(album = album, onAlbumClick = { onResultClick(it) })
                        }
                    }
                }
            }
        }

        if (state.artists.isNotEmpty()) {
            item { SectionHeader("Исполнители") }
            items(state.artists, key = { it.id }) { artist ->
                ArtistListItem(artist = artist, onArtistClick = { onResultClick(it) })
            }
        }
    }
}