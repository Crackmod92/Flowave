package com.crackmod.flowave.presentation.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.presentation.components.EmptyContent
import com.crackmod.flowave.presentation.player.PlayerViewModel
import com.crackmod.flowave.presentation.screens.library.components.TrackListItem
import com.crackmod.flowave.presentation.screens.library.components.TrackSortBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTracksScreen(
    onBackPress: () -> Unit,
    onShowTrackOptions: (Track) -> Unit,
    viewModel: AllTracksViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val playerUiState by playerViewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showSortSheet by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }

    if (showSortSheet) {
        TrackSortBottomSheet(
            currentSortBy = state.sortBy,
            currentSortOrder = state.sortOrder,
            onSortByChange = viewModel::onSortByChange,
            onSortOrderChange = viewModel::onSortOrderChange,
            onPlayClick = {
                if (state.tracks.isNotEmpty()) {
                    playerViewModel.playTrackList(state.tracks, 0)
                }
            },
            onDismiss = { showSortSheet = false }
        )
    }

    val listState = rememberLazyListState()
    LaunchedEffect(state.tracks) {
        if (state.tracks.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    val bottomPadding = if (playerUiState.isPlayerVisible) 64.dp else 0.dp

    Scaffold(
        topBar = {
            SearchableTopAppBar(
                title = "Все треки",
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onToggleSearch = { isSearchActive = !isSearchActive },
                onBackPress = onBackPress
            ) {
                // Действия для стандартного TopAppBar
                IconButton(
                    onClick = {
                        if (state.tracks.isNotEmpty()) {
                            playerViewModel.playTrackList(state.tracks, 0, shuffle = true)
                        }
                    },
                    enabled = state.tracks.isNotEmpty()
                ) {
                    Icon(imageVector = Icons.Default.Shuffle, contentDescription = "Перемешать все")
                }
                IconButton(onClick = { showSortSheet = true }) {
                    Icon(imageVector = Icons.Default.FilterList, contentDescription = "Сортировка")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.tracks.isEmpty()) {
                val emptyMessage = if (searchQuery.isNotEmpty()) {
                    "По запросу \"$searchQuery\" ничего не найдено."
                } else {
                    "Добавьте папки с музыкой в Настройках и запустите сканирование."
                }
                EmptyContent(
                    title = "Треки не найдены",
                    subtitle = emptyMessage
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    state = listState,
                    contentPadding = PaddingValues(bottom = bottomPadding)
                ) {
                    itemsIndexed(state.tracks, key = { _, track -> track.id }) { index, track ->
                        TrackListItem(
                            track = track,
                            isPlaying = playerUiState.currentTrack?.id == track.id,
                            onTrackClick = { playerViewModel.playTrackList(state.tracks, index) },
                            onFavoriteClick = { libraryViewModel.toggleFavorite(it) },
                            onLongClick = { onShowTrackOptions(it) },
                            trailingContent = {
                                IconButton(onClick = { onShowTrackOptions(track) }) {
                                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Опции трека")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}