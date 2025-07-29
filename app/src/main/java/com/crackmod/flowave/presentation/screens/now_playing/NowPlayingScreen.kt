// ПУТЬ: com/crackmod/flowave/presentation/screens/now_playing/NowPlayingScreen.kt

package com.crackmod.flowave.presentation.screens.now_playing

import android.content.ContentUris
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.crackmod.flowave.R
import com.crackmod.flowave.domain.repository.NowPlayingScreenStyle
import com.crackmod.flowave.presentation.player.PlayerUiState
import com.crackmod.flowave.presentation.player.PlayerViewModel
import com.crackmod.flowave.presentation.player.QueueViewModel
import com.crackmod.flowave.presentation.screens.library.LibraryViewModel
import com.crackmod.flowave.presentation.screens.library.components.AddToPlaylistDialog
import com.crackmod.flowave.presentation.screens.library.components.ConfirmationDialog
import com.crackmod.flowave.presentation.screens.lyrics.LyricsScreen
import com.crackmod.flowave.presentation.screens.queue.QueuePage
import com.crackmod.flowave.presentation.screens.settings.SettingsViewModel
import com.crackmod.flowave.presentation.screens.tag_editor.TagEditorSheet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Speed
import com.crackmod.flowave.presentation.screens.home.components.ScanStatusBanner

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun NowPlayingScreen(
    onDismissRequest: () -> Unit,
    onArtistClick: (Long) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onNavigateToAudioEffects: () -> Unit,
    isShowingLyrics: Boolean,
    onToggleLyrics: (Boolean) -> Unit,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    queueViewModel: QueueViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by playerViewModel.uiState.collectAsState()
    val libraryState by libraryViewModel.state.collectAsState()
    val bannerState by libraryViewModel.bannerState.collectAsState()
    val currentTrack = uiState.currentTrack ?: return
    val haptic = LocalHapticFeedback.current
    val nowPlayingStyle by settingsViewModel.nowPlayingScreenStyle.collectAsState()

    var showQueueSheet by remember { mutableStateOf(false) }
    val queueSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showOptionsMenu by remember { mutableStateOf(false) }
    val optionsMenuSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSpeedSelector by remember { mutableStateOf(false) }
    var showTagEditorSheet by remember { mutableStateOf(false) }

    val trackToAdd by libraryViewModel.trackToAdd.collectAsState()
    val scope = rememberCoroutineScope()

    val trackToDelete by libraryViewModel.trackToDelete.collectAsState()
    trackToDelete?.let { track ->
        ConfirmationDialog(
            title = "Удалить трек",
            text = "Вы уверены, что хотите удалить трек \"${track.displayTitle}\"? Он будет удален с вашего устройства.",
            onConfirm = { libraryViewModel.confirmDeleteTrack() },
            onDismiss = { libraryViewModel.onDismissDeleteTrackConfirmation() }
        )
    }

    BackHandler(enabled = isShowingLyrics) {
        onToggleLyrics(false)
    }

    if (trackToAdd != null) {
        AddToPlaylistDialog(
            playlists = libraryState.playlists.filter { !it.isSystem },
            onDismiss = { libraryViewModel.onDismissPlaylistSelection() },
            onPlaylistSelected = { playlistId -> libraryViewModel.addTrackToPlaylist(playlistId) }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = isShowingLyrics,
            label = "player_lyrics_transition",
            transitionSpec = {
                if (targetState) {
                    slideInVertically(animationSpec = tween(400)) { it } + fadeIn(animationSpec = tween(400)) togetherWith
                            slideOutVertically(animationSpec = tween(400)) { -it } + fadeOut(animationSpec = tween(400))
                } else {
                    slideInVertically(animationSpec = tween(400)) { -it } + fadeIn(animationSpec = tween(400)) togetherWith
                            slideOutVertically(animationSpec = tween(400)) { it } + fadeOut(animationSpec = tween(400))
                }
            }
        ) { showingLyrics ->
            if (showingLyrics) {
                LyricsScreen(
                    trackId = currentTrack.id,
                    playerUiState = uiState,
                    onBackPress = { onToggleLyrics(false) },
                    onLineClick = { timestamp -> playerViewModel.seekTo(timestamp) }
                )
            } else {
                PlayerPage(
                    nowPlayingStyle = nowPlayingStyle,
                    uiState = uiState,
                    onDismissRequest = onDismissRequest,
                    onArtistClick = onArtistClick,
                    onAlbumClick = onAlbumClick,
                    onShowLyrics = { onToggleLyrics(true) },
                    onShowOptionsMenu = { showOptionsMenu = true },
                    onShowQueue = {
                        if (!showQueueSheet) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showQueueSheet = true
                        }
                    },
                    onPlayPauseClick = playerViewModel::togglePlayPause,
                    onSkipNextClick = playerViewModel::skipNext,
                    onSkipPreviousClick = playerViewModel::skipPrevious,
                    onToggleShuffleClick = playerViewModel::toggleShuffleMode,
                    onToggleRepeatClick = playerViewModel::toggleRepeatMode,
                    onSeek = playerViewModel::seekTo,
                    onToggleFavorite = playerViewModel::toggleFavorite,
                    formatTime = playerViewModel::formatTime,
                )
            }
        }

        // --- ГЛАВНОЕ ИСПРАВЛЕНИЕ ЗДЕСЬ ---
        ScanStatusBanner(
            isVisible = bannerState.isVisible,
            message = bannerState.message,
            isError = bannerState.isError,
            modifier = Modifier
                .align(Alignment.TopCenter)
                // Этот отступ сдвинет баннер вниз ровно на высоту статус-бара
                .padding(WindowInsets.statusBars.asPaddingValues())
        )
    }


    if (showQueueSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showQueueSheet = false
            },
            sheetState = queueSheetState,
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
                    queueViewModel = queueViewModel,
                    libraryViewModel = libraryViewModel
                )
            }
        }
    }

    if (showOptionsMenu) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch { optionsMenuSheetState.hide() }.invokeOnCompletion {
                    showOptionsMenu = false
                }
            },
            sheetState = optionsMenuSheetState,
            windowInsets = WindowInsets(0.dp)
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                ListItem(
                    leadingContent = {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(currentTrack.albumId?.let { ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, it) })
                                .crossfade(true).error(R.drawable.ic_flowave_album).build(),
                            contentDescription = "Обложка",
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
                        )
                    },
                    headlineContent = { Text(currentTrack.displayTitle, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    supportingContent = { Text(currentTrack.displayArtist, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ListItem(
                    headlineContent = { Text("Добавить в плейлист") },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null) },
                    modifier = Modifier.clickable {
                        libraryViewModel.onAddTrackToPlaylistRequest(currentTrack)
                        scope.launch { optionsMenuSheetState.hide() }.invokeOnCompletion { showOptionsMenu = false }
                    }
                )
                ListItem(
                    headlineContent = { Text("Перейти к альбому") },
                    leadingContent = { Icon(painterResource(id = R.drawable.ic_flowave_album), null, modifier = Modifier.size(24.dp)) },
                    modifier = Modifier.clickable {
                        currentTrack.albumId?.let { onAlbumClick(it) }
                        scope.launch { optionsMenuSheetState.hide() }.invokeOnCompletion {
                            showOptionsMenu = false
                            onDismissRequest()
                        }
                    }
                )
                ListItem(
                    headlineContent = { Text("Перейти к исполнителю") },
                    leadingContent = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.clickable {
                        currentTrack.artistId?.let { onArtistClick(it) }
                        scope.launch { optionsMenuSheetState.hide() }.invokeOnCompletion {
                            showOptionsMenu = false
                            onDismissRequest()
                        }
                    }
                )
                ListItem(
                    headlineContent = { Text("Текст песни") },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.Article, null) },
                    modifier = Modifier.clickable {
                        onToggleLyrics(true)
                        scope.launch { optionsMenuSheetState.hide() }.invokeOnCompletion { showOptionsMenu = false }
                    }
                )
                ListItem(
                    headlineContent = { Text("Редактировать теги") },
                    leadingContent = { Icon(Icons.Default.Edit, null) },
                    modifier = Modifier.clickable {
                        showTagEditorSheet = true
                        scope.launch { optionsMenuSheetState.hide() }.invokeOnCompletion { showOptionsMenu = false }
                    }
                )
                ListItem(
                    headlineContent = { Text("Аудиоэффекты") },
                    leadingContent = { Icon(Icons.Default.GraphicEq, null) },
                    modifier = Modifier.clickable {
                        onNavigateToAudioEffects()
                        scope.launch { optionsMenuSheetState.hide() }.invokeOnCompletion { showOptionsMenu = false }
                    }
                )
                ListItem(
                    headlineContent = { Text("Скорость воспроизведения (${uiState.playbackSpeed}x)") },
                    leadingContent = { Icon(Icons.Default.Speed, null) },
                    modifier = Modifier.clickable {
                        showSpeedSelector = true
                        scope.launch { optionsMenuSheetState.hide() }.invokeOnCompletion { showOptionsMenu = false }
                    }
                )
                ListItem(
                    headlineContent = { Text("Удалить из медиатеки") },
                    leadingContent = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                    colors = ListItemDefaults.colors(headlineColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.clickable {
                        libraryViewModel.onDeleteTrackRequest(currentTrack)
                        scope.launch { optionsMenuSheetState.hide() }.invokeOnCompletion { showOptionsMenu = false }
                    }
                )
            }
        }
    }

    if (showSpeedSelector) {
        SpeedSelectorDialog(
            currentSpeed = uiState.playbackSpeed,
            onDismiss = { showSpeedSelector = false },
            onSpeedSelected = {
                playerViewModel.setPlaybackSpeed(it)
                showSpeedSelector = false
            }
        )
    }

    if (showTagEditorSheet) {
        TagEditorSheet(
            trackId = currentTrack.id,
            onDismiss = { didSave ->
                showTagEditorSheet = false
                if (didSave) {
                    libraryViewModel.showBanner("Теги сохранены!")
                }
            }
        )
    }
}

@Composable
private fun PlayerPage(
    nowPlayingStyle: NowPlayingScreenStyle,
    uiState: PlayerUiState,
    onDismissRequest: () -> Unit,
    onArtistClick: (Long) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onShowLyrics: () -> Unit,
    onShowOptionsMenu: () -> Unit,
    onShowQueue: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSkipNextClick: () -> Unit,
    onSkipPreviousClick: () -> Unit,
    onToggleShuffleClick: () -> Unit,
    onToggleRepeatClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleFavorite: () -> Unit,
    formatTime: (Long) -> String,
) {
    val haptic = LocalHapticFeedback.current
    val playerPageScope = rememberCoroutineScope()
    val currentTrack = uiState.currentTrack ?: return

    when (nowPlayingStyle) {
        NowPlayingScreenStyle.SOLAR_FLARE -> SolarFlareNowPlayingScreen(
            currentTrack, uiState, onDismissRequest, onArtistClick, onAlbumClick, onShowLyrics, onShowOptionsMenu, onShowQueue, onPlayPauseClick, onSkipNextClick, onSkipPreviousClick, onToggleShuffleClick, onToggleRepeatClick, onSeek, onToggleFavorite, formatTime, haptic, playerPageScope
        )
        NowPlayingScreenStyle.PULSAR -> PulsarNowPlayingScreen(
            currentTrack, uiState, onDismissRequest, onArtistClick, onAlbumClick, onShowLyrics, onShowOptionsMenu, onShowQueue, onPlayPauseClick, onSkipNextClick, onSkipPreviousClick, onToggleShuffleClick, onToggleRepeatClick, onSeek, onToggleFavorite, formatTime, haptic, playerPageScope
        )
        NowPlayingScreenStyle.WARP_DRIVE -> WarpDriveNowPlayingScreen(
            currentTrack, uiState, onDismissRequest, onArtistClick, onAlbumClick, onShowLyrics, onShowOptionsMenu, onShowQueue, onPlayPauseClick, onSkipNextClick, onSkipPreviousClick, onToggleShuffleClick, onToggleRepeatClick, onSeek, onToggleFavorite, formatTime, haptic, playerPageScope
        )
        NowPlayingScreenStyle.EVENT_HORIZON -> EventHorizonNowPlayingScreen(
            currentTrack, uiState, onDismissRequest, onArtistClick, onAlbumClick, onShowLyrics, onShowOptionsMenu, onShowQueue, onPlayPauseClick, onSkipNextClick, onSkipPreviousClick, onToggleShuffleClick, onToggleRepeatClick, onSeek, onToggleFavorite, formatTime, haptic, playerPageScope
        )
        NowPlayingScreenStyle.AURORA -> AuroraNowPlayingScreen(
            currentTrack, uiState, onDismissRequest, onArtistClick, onAlbumClick, onShowLyrics, onShowOptionsMenu, onShowQueue, onPlayPauseClick, onSkipNextClick, onSkipPreviousClick, onToggleShuffleClick, onToggleRepeatClick, onSeek, onToggleFavorite, formatTime, haptic, playerPageScope
        )
        NowPlayingScreenStyle.NEBULA -> NebulaNowPlayingScreen(
            currentTrack, uiState, onDismissRequest, onArtistClick, onAlbumClick, onShowLyrics, onShowOptionsMenu, onShowQueue, onPlayPauseClick, onSkipNextClick, onSkipPreviousClick, onToggleShuffleClick, onToggleRepeatClick, onSeek, onToggleFavorite, formatTime, haptic, playerPageScope
        )
        NowPlayingScreenStyle.CONSTELLATION -> ConstellationNowPlayingScreen(
            currentTrack, uiState, onDismissRequest, onArtistClick, onAlbumClick, onShowLyrics, onShowOptionsMenu, onShowQueue, onPlayPauseClick, onSkipNextClick, onSkipPreviousClick, onToggleShuffleClick, onToggleRepeatClick, onSeek, onToggleFavorite, formatTime, haptic, playerPageScope
        )
        NowPlayingScreenStyle.ASTEROID_BELT -> AsteroidBeltNowPlayingScreen(
            currentTrack, uiState, onDismissRequest, onArtistClick, onAlbumClick, onShowLyrics, onShowOptionsMenu, onShowQueue, onPlayPauseClick, onSkipNextClick, onSkipPreviousClick, onToggleShuffleClick, onToggleRepeatClick, onSeek, onToggleFavorite, formatTime, haptic, playerPageScope
        )
        NowPlayingScreenStyle.GALACTIC_CORE -> GalacticCoreNowPlayingScreen(
            currentTrack, uiState, onDismissRequest, onArtistClick, onAlbumClick, onShowLyrics, onShowOptionsMenu, onShowQueue, onPlayPauseClick, onSkipNextClick, onSkipPreviousClick, onToggleShuffleClick, onToggleRepeatClick, onSeek, onToggleFavorite, formatTime, haptic, playerPageScope
        )
        NowPlayingScreenStyle.SPACE_ODYSSEY -> SpaceOdysseyNowPlayingScreen(
            currentTrack, uiState, onDismissRequest, onArtistClick, onAlbumClick, onShowLyrics, onShowOptionsMenu, onShowQueue, onPlayPauseClick, onSkipNextClick, onSkipPreviousClick, onToggleShuffleClick, onToggleRepeatClick, onSeek, onToggleFavorite, formatTime, haptic, playerPageScope
        )
    }
}

@Composable
private fun SpeedSelectorDialog(
    currentSpeed: Float,
    onDismiss: () -> Unit,
    onSpeedSelected: (Float) -> Unit
) {
    val speedOptions = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f)
    var tempSpeed by remember { mutableStateOf(currentSpeed) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Скорость воспроизведения") },
        text = {
            Column {
                Text(
                    text = "%.2fx".format(tempSpeed),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Slider(
                    value = tempSpeed,
                    onValueChange = { tempSpeed = it },
                    valueRange = 0.25f..2.0f,
                    steps = ((2.0f - 0.25f) / 0.05f - 1).toInt(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    speedOptions.forEach { speed ->
                        TextButton(onClick = { tempSpeed = speed }) {
                            Text("%.2f".format(speed))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSpeedSelected(tempSpeed) }) {
                Text("Применить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}