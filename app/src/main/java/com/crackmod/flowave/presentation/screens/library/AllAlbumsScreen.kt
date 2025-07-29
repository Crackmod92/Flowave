package com.crackmod.flowave.presentation.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crackmod.flowave.domain.model.Album
import com.crackmod.flowave.presentation.components.EmptyContent
import com.crackmod.flowave.presentation.player.PlayerViewModel
import com.crackmod.flowave.presentation.screens.library.components.AlbumGridItem
import com.crackmod.flowave.presentation.screens.library.components.AlbumListItem
import com.crackmod.flowave.presentation.screens.library.components.AlbumSortBottomSheet
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllAlbumsScreen(
    onAlbumClick: (Album) -> Unit,
    onBackPress: () -> Unit,
    viewModel: AllAlbumsViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val playerUiState by playerViewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showSortSheet by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (showSortSheet) {
        AlbumSortBottomSheet(
            currentSortBy = state.sortBy,
            currentSortOrder = state.sortOrder,
            isGridView = state.isGridView,
            onSortByChange = viewModel::onSortByChange,
            onSortOrderChange = viewModel::onSortOrderChange,
            onLayoutChange = viewModel::onLayoutChange,
            onDismiss = { showSortSheet = false }
        )
    }

    val gridState = rememberLazyGridState()
    val listState = rememberLazyListState()

    LaunchedEffect(state.albums) {
        if (state.albums.isNotEmpty()) {
            if (state.isGridView) gridState.scrollToItem(0) else listState.scrollToItem(0)
        }
    }

    val bottomPadding = if (playerUiState.isPlayerVisible) 64.dp else 0.dp

    Scaffold(
        topBar = {
            SearchableTopAppBar(
                title = "Все альбомы",
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onToggleSearch = { isSearchActive = !isSearchActive },
                onBackPress = onBackPress
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            val tracks = viewModel.getTracksForRandomAlbum().firstOrNull()
                            if (!tracks.isNullOrEmpty()) {
                                playerViewModel.playTrackList(tracks, 0, shuffle = false)
                            }
                        }
                    },
                    enabled = state.albums.isNotEmpty()
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = "Случайный альбом")
                }
                IconButton(onClick = { showSortSheet = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Сортировка и вид")
                }
            }
        }
    ) { paddingValues ->
        if (state.albums.isEmpty()) {
            val emptyMessage = if (searchQuery.isNotEmpty()) {
                "По запросу \"$searchQuery\" ничего не найдено."
            } else {
                "Добавьте папки с музыкой в Настройках и запустите сканирование."
            }
            EmptyContent(
                title = "Альбомы не найдены",
                subtitle = emptyMessage
            )
        } else {
            if (state.isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    state = gridState,
                    contentPadding = PaddingValues(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 12.dp + bottomPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = state.albums, key = { it.id }) { album ->
                        AlbumGridItem(album = album, onAlbumClick = onAlbumClick)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    state = listState,
                    contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp + bottomPadding)
                ) {
                    items(items = state.albums, key = { it.id }) { album ->
                        AlbumListItem(album = album, onAlbumClick = onAlbumClick)
                    }
                }
            }
        }
    }
}