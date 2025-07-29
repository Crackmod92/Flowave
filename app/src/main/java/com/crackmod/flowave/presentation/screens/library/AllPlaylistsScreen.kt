// ПУТЬ: /app/src/main/java/com/crackmod/flowave/presentation/screens/library/AllPlaylistsScreen.kt
package com.crackmod.flowave.presentation.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crackmod.flowave.domain.model.Playlist
import com.crackmod.flowave.domain.util.AppConstants
import com.crackmod.flowave.presentation.components.EmptyContent
import com.crackmod.flowave.presentation.components.FlowaveTopAppBar
import com.crackmod.flowave.presentation.player.PlayerViewModel
import com.crackmod.flowave.presentation.screens.library.components.PlaylistListItem
import com.crackmod.flowave.presentation.screens.library.components.PlaylistSortBottomSheet
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllPlaylistsScreen(
    onCreatePlaylistClick: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onBackPress: () -> Unit,
    viewModel: AllPlaylistsViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val playerUiState by playerViewModel.uiState.collectAsState()
    var showSortSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (showSortSheet) {
        PlaylistSortBottomSheet(
            currentSortBy = state.sortBy,
            currentSortOrder = state.sortOrder,
            onSortByChange = viewModel::onSortByChange,
            onSortOrderChange = viewModel::onSortOrderChange,
            onDismiss = { showSortSheet = false }
        )
    }

    val listState = rememberLazyListState()
    LaunchedEffect(state.userPlaylists) {
        if (state.userPlaylists.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    val fabBottomPadding = if (playerUiState.isPlayerVisible) 64.dp else 0.dp
    val listBottomPadding = 80.dp + fabBottomPadding

    Scaffold(
        topBar = {
            FlowaveTopAppBar(
                title = "Все плейлисты",
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val tracks = viewModel.getTracksForRandomPlaylist().firstOrNull()
                                if (!tracks.isNullOrEmpty()) {
                                    playerViewModel.playTrackList(tracks, 0, shuffle = true)
                                }
                            }
                        },
                        enabled = state.userPlaylists.any { it.trackCount > 0 } || state.systemPlaylists.any { it.trackCount > 0 }
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = "Случайный плейлист")
                    }
                    IconButton(onClick = { showSortSheet = true }) {
                        Icon(Icons.Default.FilterList, "Сортировка")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreatePlaylistClick,
                modifier = Modifier.padding(bottom = fabBottomPadding)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Создать плейлист")
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.systemPlaylists.isEmpty() && state.userPlaylists.isEmpty()) {
            EmptyContent(
                title = "У вас пока нет плейлистов",
                subtitle = "Нажмите кнопку '+' чтобы создать свой первый плейлист."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                state = listState,
                contentPadding = PaddingValues(bottom = listBottomPadding)
            ) {
                items(state.systemPlaylists, key = { it.id }) { playlist ->
                    val icon = when (playlist.id) {
                        AppConstants.FAVORITES_PLAYLIST_ID -> Icons.Default.Favorite
                        AppConstants.RECENTLY_ADDED_PLAYLIST_ID -> Icons.Default.History
                        AppConstants.MOST_PLAYED_PLAYLIST_ID -> Icons.Default.TrendingUp
                        else -> null
                    }
                    SystemPlaylistListItem(
                        playlist = playlist,
                        icon = icon,
                        onClick = { onPlaylistClick(playlist) }
                    )
                }

                if (state.userPlaylists.isNotEmpty()) {
                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
                    }
                    items(state.userPlaylists, key = { it.id }) { playlist ->
                        PlaylistListItem(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SystemPlaylistListItem(
    playlist: Playlist,
    icon: ImageVector?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        headlineContent = { Text(playlist.name) },
        supportingContent = { Text("${playlist.trackCount} треков") },
        leadingContent = {
            icon?.let {
                Icon(imageVector = it, contentDescription = playlist.name)
            }
        }
    )
}