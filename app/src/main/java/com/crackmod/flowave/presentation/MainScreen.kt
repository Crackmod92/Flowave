package com.crackmod.flowave.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost as AnimatedNavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.presentation.navigation.Screen
import com.crackmod.flowave.presentation.navigation.bottomNavItems
import com.crackmod.flowave.presentation.player.PlayerBottomSheet
import com.crackmod.flowave.presentation.player.PlayerViewModel
import com.crackmod.flowave.presentation.player.QueueViewModel
import com.crackmod.flowave.presentation.screens.album_detail.AlbumDetailScreen
import com.crackmod.flowave.presentation.screens.artist_detail.ArtistDetailScreen
import com.crackmod.flowave.presentation.screens.audio_effects.AudioEffectsScreen
import com.crackmod.flowave.presentation.screens.equalizer.EqualizerScreen
import com.crackmod.flowave.presentation.screens.home.HomeScreen
import com.crackmod.flowave.presentation.screens.library.*
import com.crackmod.flowave.presentation.screens.library.components.AddToPlaylistDialog
import com.crackmod.flowave.presentation.screens.library.components.ConfirmationDialog
import com.crackmod.flowave.presentation.screens.library.components.CreatePlaylistDialog
import com.crackmod.flowave.presentation.screens.library.components.TrackOptionsBottomSheet
import com.crackmod.flowave.presentation.screens.now_playing.NowPlayingScreen
import com.crackmod.flowave.presentation.screens.playlist_detail.PlaylistDetailScreen
import com.crackmod.flowave.presentation.screens.queue.QueuePage
import com.crackmod.flowave.presentation.screens.search.SearchScreen
import com.crackmod.flowave.presentation.screens.settings.*
import com.crackmod.flowave.presentation.screens.tag_editor.TagEditorSheet
import com.crackmod.flowave.ui.theme.AmoledBackground
import com.crackmod.flowave.ui.theme.getNavigationIconColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val libraryViewModel: LibraryViewModel = hiltViewModel()
    val queueViewModel: QueueViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val playerUiState by playerViewModel.uiState.collectAsState()
    val libraryState by libraryViewModel.state.collectAsState()

    var isShowingLyrics by remember { mutableStateOf(false) }

    // ### ИСПРАВЛЕНИЕ: Возвращаем удаленные переменные ###
    var showQueueSheet by remember { mutableStateOf(false) }
    val queueSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current
    // ### КОНЕЦ ИСПРАВЛЕНИЯ ###

    val playerSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            if (sheetValue == SheetValue.Hidden && isShowingLyrics) {
                isShowingLyrics = false
                false
            } else {
                true
            }
        }
    )

    LaunchedEffect(playerSheetState.isVisible) {
        if (!playerSheetState.isVisible) {
            isShowingLyrics = false
        }
    }

    val onNavigateToArtist: (Long) -> Unit = { artistId -> navController.navigate("artist_detail/$artistId") }
    val onNavigateToAlbum: (Long) -> Unit = { albumId -> navController.navigate("album_detail/$albumId") }
    val onNavigateToPlaylist: (String) -> Unit = { playlistId -> navController.navigate("playlist_detail/$playlistId") }
    val onNavigateToAudioEffects: () -> Unit = { navController.navigate(Screen.AudioEffects.route) }
    val onNavigateToEqualizer: () -> Unit = { navController.navigate(Screen.Equalizer.route) }
    val onNavigateToAppearanceSettings: () -> Unit = { navController.navigate(Screen.AppearanceSettings.route) }
    val onNavigateToAudioSettings: () -> Unit = { navController.navigate(Screen.AudioSettings.route) }
    val onNavigateToLibrarySettings: () -> Unit = { navController.navigate(Screen.LibrarySettings.route) }
    val onBackPress: () -> Unit = { navController.popBackStack() }
    val onNavigateToAllTracks: () -> Unit = { navController.navigate(Screen.AllTracks.route) }
    val onNavigateToAllAlbums: () -> Unit = { navController.navigate(Screen.Albums.route) }
    val onNavigateToAllArtists: () -> Unit = { navController.navigate(Screen.Artists.route) }
    val onNavigateToAllPlaylists: () -> Unit = { navController.navigate(Screen.Playlists.route) }

    val onShowTrackOptions: (Track) -> Unit = { track ->
        libraryViewModel.onTrackOptionsClick(track)
    }

    val screensWithBottomUI = listOf(
        Screen.Home.route,
        Screen.AllTracks.route,
        Screen.Albums.route,
        Screen.Artists.route,
        Screen.Playlists.route,
        Screen.Search.route,
        Screen.Settings.route,
        "album_detail/{albumId}",
        "artist_detail/{artistId}",
        "playlist_detail/{playlistId}",
        Screen.AudioEffects.route,
        Screen.Equalizer.route,
        Screen.AppearanceSettings.route,
        Screen.AudioSettings.route,
        Screen.LibrarySettings.route
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val shouldShowBottomUI = screensWithBottomUI.any { routePattern ->
        val baseRoute = routePattern.substringBefore('/')
        currentRoute?.startsWith(baseRoute) == true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0.dp),
            bottomBar = {
                if (shouldShowBottomUI) {
                    BottomNavigationBar(navController = navController)
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                NavigationHost(
                    navController = navController,
                    playerViewModel = playerViewModel,
                    libraryViewModel = libraryViewModel,
                    onNavigateToAlbum = onNavigateToAlbum,
                    onNavigateToArtist = onNavigateToArtist,
                    onNavigateToPlaylist = onNavigateToPlaylist,
                    onBackPress = onBackPress,
                    onNavigateToAllTracks = onNavigateToAllTracks,
                    onNavigateToAllAlbums = onNavigateToAllAlbums,
                    onNavigateToAllArtists = onNavigateToAllArtists,
                    onNavigateToAllPlaylists = onNavigateToAllPlaylists,
                    onNavigateToAudioEffects = onNavigateToAudioEffects,
                    onNavigateToEqualizer = onNavigateToEqualizer,
                    onNavigateToAppearanceSettings = onNavigateToAppearanceSettings,
                    onNavigateToAudioSettings = onNavigateToAudioSettings,
                    onNavigateToLibrarySettings = onNavigateToLibrarySettings,
                    onShowTrackOptions = onShowTrackOptions
                )

                if (playerUiState.isPlayerVisible && shouldShowBottomUI) {
                    Column(
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        PlayerBottomSheet(
                            uiState = playerUiState,
                            onContainerClick = { scope.launch { playerSheetState.show() } },
                            onPlayPauseClick = { playerViewModel.togglePlayPause() },
                            onSkipNextClick = { playerViewModel.skipNext() },
                            onSkipPreviousClick = { playerViewModel.skipPrevious() },
                            onStopClick = { playerViewModel.stopAndClearPlayer() }
                        )
                    }
                }
            }
        }

        val trackToDelete by libraryViewModel.trackToDelete.collectAsState()
        trackToDelete?.let { track ->
            ConfirmationDialog(
                title = "Удалить трек",
                text = "Вы уверены, что хотите удалить трек \"${track.displayTitle}\"? Он будет удален с вашего устройства.",
                onConfirm = { libraryViewModel.confirmDeleteTrack() },
                onDismiss = { libraryViewModel.onDismissDeleteTrackConfirmation() }
            )
        }

        val trackToAdd by libraryViewModel.trackToAdd.collectAsState()
        if (trackToAdd != null) {
            AddToPlaylistDialog(
                playlists = libraryState.playlists.filter { !it.isSystem },
                onDismiss = { libraryViewModel.onDismissPlaylistSelection() },
                onPlaylistSelected = { playlistId -> libraryViewModel.addTrackToPlaylist(playlistId) }
            )
        }

        val showCreatePlaylistDialog by libraryViewModel.showCreatePlaylistDialog.collectAsState()
        if (showCreatePlaylistDialog) {
            CreatePlaylistDialog(
                onDismiss = { libraryViewModel.onDismissCreatePlaylistDialog() },
                onCreate = { name -> libraryViewModel.createPlaylist(name) }
            )
        }

        val trackToEdit by libraryViewModel.trackToEdit.collectAsState()
        if (trackToEdit != null) {
            TagEditorSheet(
                trackId = trackToEdit!!.id,
                onDismiss = { didSave ->
                    if (didSave) {
                        libraryViewModel.showBanner("Теги успешно сохранены!")
                    }
                    libraryViewModel.onEditTagsDismiss()
                }
            )
        }

        val selectedTrackForOptions by libraryViewModel.selectedTrackForOptions.collectAsState()
        selectedTrackForOptions?.let { track ->
            TrackOptionsBottomSheet(
                track = track,
                onDismiss = { libraryViewModel.onDismissTrackOptions() },
                onPlayNext = {
                    queueViewModel.playTrackNext(it)
                    libraryViewModel.showBanner("\"${it.displayTitle}\" будет играть следующим.")
                },
                onAddToQueue = {
                    queueViewModel.addToQueue(it)
                    libraryViewModel.showBanner("\"${it.displayTitle}\" добавлен в очередь.")
                },
                onAddToPlaylist = { libraryViewModel.onAddTrackToPlaylistRequest(it) },
                onEditTags = { libraryViewModel.onEditTagsRequest(it) },
                onDelete = { libraryViewModel.onDeleteTrackRequest(it) }
            )
        }

        if (showQueueSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showQueueSheet = false
                },
                sheetState = queueSheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets(0.dp)
            ) {
                val configuration = LocalConfiguration.current
                val screenHeight = configuration.screenHeightDp.dp
                val queueHeight = screenHeight * 0.5f

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(queueHeight)
                ) {
                    QueuePage(
                        playerViewModel = playerViewModel,
                        queueViewModel = queueViewModel,
                        libraryViewModel = libraryViewModel
                    )
                }
            }
        }

        if (playerSheetState.isVisible && playerUiState.currentTrack != null) {
            ModalBottomSheet(
                onDismissRequest = { scope.launch { playerSheetState.hide() } },
                sheetState = playerSheetState,
                modifier = Modifier.fillMaxSize(),
                windowInsets = WindowInsets(0.dp),
                dragHandle = null,
                scrimColor = Color.Transparent
            ) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)) {
                    NowPlayingScreen(
                        onDismissRequest = { scope.launch { playerSheetState.hide() } },
                        onArtistClick = onNavigateToArtist,
                        onAlbumClick = onNavigateToAlbum,
                        isShowingLyrics = isShowingLyrics,
                        onToggleLyrics = { isShowingLyrics = it },
                        onNavigateToAudioEffects = onNavigateToAudioEffects
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val (selectedIconColor, unselectedIconColor) = getNavigationIconColors()

    val navBarColor = if (MaterialTheme.colorScheme.background == AmoledBackground) {
        Color.Black
    } else {
        MaterialTheme.colorScheme.surface
    }
    NavigationBar(
        modifier = Modifier.height(64.dp),
        containerColor = navBarColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
    ) {
        bottomNavItems.forEach { screen ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = screen.iconResId),
                        contentDescription = screen.title,
                        modifier = Modifier.size(26.dp),
                    )
                },
                label = null,
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedIconColor,
                    unselectedIconColor = unselectedIconColor,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun NavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel,
    libraryViewModel: LibraryViewModel,
    onNavigateToAlbum: (Long) -> Unit,
    onNavigateToArtist: (Long) -> Unit,
    onNavigateToPlaylist: (String) -> Unit,
    onBackPress: () -> Unit,
    onNavigateToAllTracks: () -> Unit,
    onNavigateToAllAlbums: () -> Unit,
    onNavigateToAllArtists: () -> Unit,
    onNavigateToAllPlaylists: () -> Unit,
    onNavigateToAudioEffects: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToAppearanceSettings: () -> Unit,
    onNavigateToAudioSettings: () -> Unit,
    onNavigateToLibrarySettings: () -> Unit,
    onShowTrackOptions: (Track) -> Unit
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier.windowInsetsPadding(
            WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Top
            )
        )
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onAlbumClick = onNavigateToAlbum,
                onNavigateToAllTracks = onNavigateToAllTracks,
                onNavigateToAllAlbums = onNavigateToAllAlbums,
                onNavigateToAllArtists = onNavigateToAllArtists,
                onNavigateToAllPlaylists = onNavigateToAllPlaylists,
                playerViewModel = playerViewModel
            )
        }
        composable(route = Screen.Search.route) {
            SearchScreen(
                onArtistClick = onNavigateToArtist,
                onAlbumClick = onNavigateToAlbum,
                onShowTrackOptions = onShowTrackOptions,
                playerViewModel = playerViewModel
            )
        }

        composable(route = Screen.AllTracks.route) {
            AllTracksScreen(
                onBackPress = onBackPress,
                onShowTrackOptions = onShowTrackOptions,
                playerViewModel = playerViewModel
            )
        }
        composable(route = Screen.Playlists.route) {
            AllPlaylistsScreen(
                onPlaylistClick = { playlist -> onNavigateToPlaylist(playlist.id) },
                onCreatePlaylistClick = { libraryViewModel.onShowCreatePlaylistDialog() },
                onBackPress = onBackPress,
                playerViewModel = playerViewModel
            )
        }
        composable(route = Screen.Artists.route) {
            AllArtistsScreen(
                onArtistClick = { artist -> onNavigateToArtist(artist.id) },
                onBackPress = onBackPress,
                playerViewModel = playerViewModel
            )
        }
        composable(route = Screen.Albums.route) {
            AllAlbumsScreen(
                onAlbumClick = { album -> onNavigateToAlbum(album.id) },
                onBackPress = onBackPress,
                playerViewModel = playerViewModel
            )
        }

        composable(
            route = "album_detail/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.LongType })
        ) {
            AlbumDetailScreen(
                onBackPress = onBackPress,
                onArtistClick = onNavigateToArtist,
                onAlbumClick = onNavigateToAlbum,
                playerViewModel = playerViewModel,
                onShowTrackOptions = onShowTrackOptions,
            )
        }
        composable(
            route = "playlist_detail/{playlistId}",
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
        ) {
            PlaylistDetailScreen(
                onBackPress = onBackPress,
                playerViewModel = playerViewModel,
                onShowTrackOptions = onShowTrackOptions,
            )
        }
        composable(
            route = "artist_detail/{artistId}",
            arguments = listOf(navArgument("artistId") { type = NavType.LongType })
        ) {
            ArtistDetailScreen(
                onBackPress = onBackPress,
                onAlbumClick = onNavigateToAlbum,
                playerViewModel = playerViewModel,
                onShowTrackOptions = onShowTrackOptions
            )
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onNavigateToAppearance = onNavigateToAppearanceSettings,
                onNavigateToAudio = onNavigateToAudioSettings,
                onNavigateToLibrary = onNavigateToLibrarySettings
            )
        }
        composable(route = Screen.AppearanceSettings.route) {
            AppearanceSettingsScreen(onBackPress = onBackPress)
        }
        composable(route = Screen.AudioSettings.route) {
            AudioSettingsScreen(onBackPress = onBackPress, onNavigateToAudioEffects = onNavigateToAudioEffects)
        }
        composable(route = Screen.LibrarySettings.route) {
            LibrarySettingsScreen(onBackPress = onBackPress)
        }
        composable(route = Screen.AudioEffects.route) {
            AudioEffectsScreen(
                onBackPress = onBackPress,
                onNavigateToEqualizer = onNavigateToEqualizer
            )
        }
        composable(route = Screen.Equalizer.route) {
            EqualizerScreen(onBackPress = onBackPress)
        }
    }
}