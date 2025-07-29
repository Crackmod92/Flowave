package com.crackmod.flowave.presentation.screens.queue

import android.content.ContentUris
import android.provider.MediaStore
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.crackmod.flowave.R
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.presentation.components.EmptyContent
import com.crackmod.flowave.presentation.components.PlayingIndicator
import com.crackmod.flowave.presentation.player.PlayerViewModel
import com.crackmod.flowave.presentation.player.QueueViewModel
import com.crackmod.flowave.presentation.screens.library.LibraryViewModel
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.*

@Composable
private fun NowPlayingHeader(
    track: Track,
    isMusicPlaying: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
    ) {
        SectionHeader("Играет сейчас")
        CompactQueueTrackItem(
            track = track,
            onTrackClick = onClick,
            isPlaying = true,
            isReorderable = false,
            trailingContent = {
                PlayingIndicator(isPlaying = isMusicPlaying)
            }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun QueuePage(
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    queueViewModel: QueueViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    val playerUiState by playerViewModel.uiState.collectAsState()
    val queueUiState by queueViewModel.uiState.collectAsState()
    val nowPlaying = queueUiState.nowPlaying
    var reorderedList by remember(queueUiState.nextUp) { mutableStateOf(queueUiState.nextUp) }

    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val reorderableState = rememberReorderableLazyListState(
        listState = lazyListState,
        onMove = { from, to ->
            reorderedList = reorderedList.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        },
        onDragEnd = { fromIndex, toIndex ->
            if (fromIndex != toIndex) {
                queueViewModel.moveTrackInQueue(fromIndex, toIndex)
            }
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (nowPlaying == null && reorderedList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) { EmptyContent("Очередь пуста", "Добавьте треки для начала воспроизведения.") }
        } else {
            if (nowPlaying != null) {
                NowPlayingHeader(
                    track = nowPlaying,
                    isMusicPlaying = playerUiState.isPlaying,
                    onClick = {
                        scope.launch {
                            lazyListState.animateScrollToItem(0)
                        }
                    }
                )
            }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .reorderable(reorderableState),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (reorderedList.isNotEmpty()) {
                    item(key = "next_up_header") {
                        SectionHeader("Далее в очереди")
                    }
                    itemsIndexed(
                        items = reorderedList,
                        // ### ИСПРАВЛЕНИЕ: Используем `queueUiState` ###
                        key = { index, track -> track.id.toString() + queueUiState.nextUp.indexOfFirst { it.id == track.id && it.dateAdded == track.dateAdded } }
                    ) { index, track ->
                        ReorderableItem(
                            reorderableState,
                            // ### ИСПРАВЛЕНИЕ: Используем `queueUiState` ###
                            key = track.id.toString() + queueUiState.nextUp.indexOfFirst { it.id == track.id && it.dateAdded == track.dateAdded },
                            modifier = Modifier.animateItemPlacement(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        ) { isItemDragging ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    when (dismissValue) {
                                        SwipeToDismissBoxValue.EndToStart -> {
                                            queueViewModel.removeTrackFromNextUp(index)
                                            libraryViewModel.showBanner("\"${track.displayTitle}\" удален из очереди.")
                                            true
                                        }
                                        SwipeToDismissBoxValue.StartToEnd -> {
                                            queueViewModel.moveTrackToPlayNext(index)
                                            libraryViewModel.showBanner("\"${track.displayTitle}\" будет играть следующим.")
                                            false
                                        }
                                        else -> false
                                    }
                                }
                            )
                            LaunchedEffect(dismissState.currentValue) {
                                if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                                    dismissState.reset()
                                }
                            }
                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = true,
                                enableDismissFromEndToStart = true,
                                backgroundContent = { SwipeBackground(dismissState.targetValue) }
                            ) {
                                CompactQueueTrackItem(
                                    track = track,
                                    onTrackClick = { queueViewModel.playTrackFromNextUp(index) },
                                    isPlaying = false,
                                    isReorderable = true,
                                    isDragging = isItemDragging,
                                    reorderableState = reorderableState
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}

private data class SwipeBackgroundConfig(
    val color: Color,
    val icon: ImageVector,
    val alignment: Alignment
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBackground(targetValue: SwipeToDismissBoxValue) {
    val config = when (targetValue) {
        SwipeToDismissBoxValue.EndToStart -> SwipeBackgroundConfig(
            color = Color.Red.copy(alpha = 0.7f),
            icon = Icons.Default.Delete,
            alignment = Alignment.CenterEnd
        )
        SwipeToDismissBoxValue.StartToEnd -> SwipeBackgroundConfig(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            icon = Icons.AutoMirrored.Filled.PlaylistPlay,
            alignment = Alignment.CenterStart
        )
        else -> null
    }

    if (config != null) {
        val animatedColor by animateColorAsState(targetValue = config.color, label = "dismiss_color")
        val animatedScale by animateFloatAsState(
            targetValue = if (targetValue != SwipeToDismissBoxValue.Settled) 1.2f else 1f,
            label = "dismiss_icon_scale"
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(animatedColor)
                .padding(horizontal = 20.dp),
            contentAlignment = config.alignment
        ) {
            Icon(
                imageVector = config.icon,
                contentDescription = null,
                modifier = Modifier.scale(animatedScale),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CompactQueueTrackItem(
    track: Track,
    onTrackClick: () -> Unit,
    isPlaying: Boolean,
    isReorderable: Boolean,
    modifier: Modifier = Modifier,
    isDragging: Boolean = false,
    backgroundColor: Color = Color.Transparent,
    zIndex: Float = 0f,
    reorderableState: ReorderableLazyListState? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {

    val elevation by animateDpAsState(
            targetValue = if (isDragging) 8.dp else 0.dp,
            animationSpec = spring(), label = "elevation_anim"
    )
    val finalBackgroundColor = if (isPlaying) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    } else {
        backgroundColor
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation, RoundedCornerShape(8.dp))
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable { onTrackClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(track.albumId?.let { ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, it) })
                .crossfade(true)
                .error(R.drawable.ic_flowave_album)
                .build(),
            contentDescription = "Обложка альбома",
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(5.dp))
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = track.displayTitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.SemiBold
                ),
                color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = track.displayArtist,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = if (isPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (trailingContent != null) {
            trailingContent()
        } else if (isReorderable && reorderableState != null) {
            Icon(
                painter = painterResource(id = R.drawable.ic_flowave_drag_handle),
                contentDescription = "Перетащить",
                modifier = Modifier
                    .size(28.dp)
                    .detectReorderAfterLongPress(reorderableState),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}