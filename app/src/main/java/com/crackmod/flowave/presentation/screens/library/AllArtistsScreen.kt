package com.crackmod.flowave.presentation.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.crackmod.flowave.domain.model.Artist
import com.crackmod.flowave.presentation.components.EmptyContent
import com.crackmod.flowave.presentation.player.PlayerViewModel
import com.crackmod.flowave.presentation.screens.library.components.ArtistListItem
import com.crackmod.flowave.presentation.screens.library.components.ArtistSortBottomSheet
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllArtistsScreen(
    onArtistClick: (Artist) -> Unit,
    onBackPress: () -> Unit,
    viewModel: AllArtistsViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val playerUiState by playerViewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showSortSheet by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (showSortSheet) {
        ArtistSortBottomSheet(
            currentSortBy = state.sortBy,
            currentSortOrder = state.sortOrder,
            onSortByChange = viewModel::onSortByChange,
            onSortOrderChange = viewModel::onSortOrderChange,
            onDismiss = { showSortSheet = false }
        )
    }

    val listState = rememberLazyListState()
    LaunchedEffect(state.artists) {
        if (state.artists.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    val bottomPadding = if (playerUiState.isPlayerVisible) 64.dp else 0.dp

    Scaffold(
        topBar = {
            SearchableTopAppBar(
                title = "Все артисты",
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onToggleSearch = { isSearchActive = !isSearchActive },
                onBackPress = onBackPress
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            val tracks = viewModel.getTracksForRandomArtist().firstOrNull()
                            if (!tracks.isNullOrEmpty()) {
                                playerViewModel.playTrackList(tracks, 0, shuffle = true)
                            }
                        }
                    },
                    enabled = state.artists.isNotEmpty()
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = "Случайный артист")
                }
                IconButton(onClick = { showSortSheet = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Сортировка")
                }
            }
        }
    ) { paddingValues ->
        if (state.artists.isEmpty()) {
            val emptyMessage = if (searchQuery.isNotEmpty()) {
                "По запросу \"$searchQuery\" ничего не найдено."
            } else {
                "Добавьте папки с музыкой в Настройках и запустите сканирование."
            }
            EmptyContent(
                title = "Артисты не найдены",
                subtitle = emptyMessage
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                state = listState,
                contentPadding = PaddingValues(bottom = bottomPadding)
            ) {
                items(state.artists, key = { it.id }) { artist ->
                    ArtistListItem(
                        artist = artist,
                        onArtistClick = onArtistClick
                    )
                }
            }
        }
    }
}
