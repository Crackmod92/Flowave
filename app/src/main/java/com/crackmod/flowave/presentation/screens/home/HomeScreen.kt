// ПУТЬ: /app/src/main/java/com/crackmod/flowave/presentation/screens/home/HomeScreen.kt
package com.crackmod.flowave.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crackmod.flowave.R
import com.crackmod.flowave.presentation.components.EmptyContent
import com.crackmod.flowave.presentation.components.FlowaveTopAppBar
import com.crackmod.flowave.presentation.player.PlayerViewModel
import com.crackmod.flowave.presentation.screens.home.components.ScanStatusBanner
import com.crackmod.flowave.presentation.screens.home.components.WavingIndicator
import com.crackmod.flowave.presentation.screens.library.LibraryViewModel
import com.crackmod.flowave.presentation.screens.library.components.AlbumGridItem
import com.crackmod.flowave.presentation.screens.library.components.CategoryCard
import com.crackmod.flowave.presentation.screens.library.components.CompactTrackCard
import com.crackmod.flowave.presentation.screens.library.components.HomeScreenSkeleton
import com.crackmod.flowave.presentation.screens.library.components.animateItemAppearance
import com.crackmod.flowave.presentation.screens.library.components.shimmerBackground
import com.crackmod.flowave.presentation.screens.search.components.SectionHeader
import kotlinx.coroutines.launch

@Composable
private fun formatElementCount(count: Int): String {
    if (count == 1) return "$count элемент"
    val lastDigit = count % 10
    val lastTwoDigits = count % 100
    if (lastTwoDigits in 11..14) return "$count элементов"
    if (lastDigit in 2..4) return "$count элемента"
    return "$count элементов"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAlbumClick: (Long) -> Unit,
    onNavigateToAllTracks: () -> Unit,
    onNavigateToAllAlbums: () -> Unit,
    onNavigateToAllArtists: () -> Unit,
    onNavigateToAllPlaylists: () -> Unit,
    playerViewModel: PlayerViewModel,
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    val libraryState by libraryViewModel.state.collectAsState()
    val mostPlayedTracks by libraryViewModel.mostPlayedTracks.collectAsState()
    val recentlyAddedAlbums by libraryViewModel.recentlyAddedAlbums.collectAsState()
    val recentlyAddedTracks by libraryViewModel.recentlyAddedTracks.collectAsState()
    val playerUiState by playerViewModel.uiState.collectAsState()
    val bannerState by libraryViewModel.bannerState.collectAsState()

    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            libraryViewModel.scanLibrary { result ->
                result.onSuccess { count ->
                    libraryViewModel.showBanner("Найдено $count треков.")
                }.onFailure { error ->
                    libraryViewModel.showBanner("Ошибка: ${error.message}", isError = true)
                }
            }
        }
    }

    LaunchedEffect(libraryState.isScanning) {
        if (libraryState.isScanning) {
            pullToRefreshState.startRefresh()
        } else {
            pullToRefreshState.endRefresh()
        }
    }

    Scaffold(
        topBar = {
            FlowaveTopAppBar(
                title = "Главная",
                navigationIcon = null // На главном экране нет кнопки "назад"
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            when {
                libraryState.isLoading || libraryState.isScanning -> {
                    HomeScreenSkeleton(modifier = Modifier
                        .fillMaxSize()
                        .shimmerBackground())
                }
                libraryState.tracks.isEmpty() -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize()) {
                                EmptyContent(
                                    title = "Ваша медиатека пуста",
                                    subtitle = "Потяните экран вниз, чтобы запустить сканирование.",
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            bottom = if (playerUiState.isPlayerVisible) 64.dp else 0.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    CategoryCard(modifier = Modifier.weight(1f), title = "Треки", countText = formatElementCount(libraryState.tracks.size), iconResId = R.drawable.ic_flowave_music_note, onClick = onNavigateToAllTracks, onLongClick = null)
                                    CategoryCard(modifier = Modifier.weight(1f), title = "Альбомы", countText = formatElementCount(libraryState.albums.size), iconResId = R.drawable.ic_flowave_album, onClick = onNavigateToAllAlbums, onLongClick = null)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    CategoryCard(modifier = Modifier.weight(1f), title = "Артисты", countText = formatElementCount(libraryState.artists.size), iconResId = R.drawable.ic_flowave_artist, onClick = onNavigateToAllArtists, onLongClick = null)
                                    CategoryCard(modifier = Modifier.weight(1f), title = "Плейлисты", countText = formatElementCount(libraryState.playlists.size), iconResId = R.drawable.ic_flowave_playlist, onClick = onNavigateToAllPlaylists, onLongClick = null)
                                }
                            }
                        }

                        if (mostPlayedTracks.isNotEmpty()) {
                            item {
                                SectionHeader("Часто прослушиваемые")
                                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    itemsIndexed(mostPlayedTracks, key = { _, track -> track.id }) { index, track ->
                                        CompactTrackCard(track = track, onTrackClick = { playerViewModel.playTrackList(mostPlayedTracks, mostPlayedTracks.indexOf(track)) }, onPlayClick = { playerViewModel.playTrackList(mostPlayedTracks, mostPlayedTracks.indexOf(track)) }, modifier = Modifier.animateItemAppearance(index))
                                    }
                                }
                            }
                        }
                        if (recentlyAddedTracks.isNotEmpty()) {
                            item {
                                SectionHeader("Недавно добавленные")
                                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    itemsIndexed(recentlyAddedTracks, key = { _, track -> track.id }) { index, track ->
                                        CompactTrackCard(track = track, onTrackClick = { playerViewModel.playTrackList(recentlyAddedTracks, recentlyAddedTracks.indexOf(track)) }, onPlayClick = { playerViewModel.playTrackList(recentlyAddedTracks, recentlyAddedTracks.indexOf(track)) }, modifier = Modifier.animateItemAppearance(index))
                                    }
                                }
                            }
                        }
                        if (recentlyAddedAlbums.isNotEmpty()) {
                            item {
                                SectionHeader("Новые альбомы")
                                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    itemsIndexed(recentlyAddedAlbums, key = { _, album -> album.id }) { index, album ->
                                        Box(modifier = Modifier
                                            .width(115.dp)
                                            .animateItemAppearance(index)) {
                                            AlbumGridItem(album = album, onAlbumClick = { onAlbumClick(it.id) })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            PullToRefreshContainer(
                modifier = Modifier.align(Alignment.TopCenter),
                state = pullToRefreshState,
                indicator = {
                    WavingIndicator(
                        isRefreshing = pullToRefreshState.isRefreshing,
                        pullProgress = it.progress
                    )
                }
            )

            ScanStatusBanner(
                isVisible = bannerState.isVisible,
                message = bannerState.message,
                isError = bannerState.isError,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}