package com.crackmod.flowave.presentation.screens.library.components

import android.content.ContentUris
import android.provider.MediaStore
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Size
import com.crackmod.flowave.R
import com.crackmod.flowave.domain.model.Track

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackListItem(
    track: Track,
    onTrackClick: (Track) -> Unit,
    modifier: Modifier = Modifier,
    trackNumber: Int? = null,
    onLongClick: ((Track) -> Unit)? = null,
    onFavoriteClick: ((Track) -> Unit)? = null,
    // --- ИЗМЕНЕНИЕ 1: Добавляем флаги для управления видимостью кнопок ---
    showFavoriteButton: Boolean = true,
    showTrailingContent: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null,
    isPlaying: Boolean = false,
    showAlbumArt: Boolean = true
) {
    val isTrackAvailable = track.isAvailable

    val headlineColor = when {
        !isTrackAvailable -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        isPlaying -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isTrackAvailable) {
                    Modifier.combinedClickable(
                        onClick = { onTrackClick(track) },
                        onLongClick = { onLongClick?.invoke(track) }
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .alpha(if (isTrackAvailable) 1f else 0.5f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showAlbumArt) {
            val albumArtUri = track.albumId?.let {
                try {
                    ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, it)
                } catch (e: Exception) { null }
            }

            val imageRequest = ImageRequest.Builder(LocalContext.current)
                .data(albumArtUri)
                .crossfade(true)
                .placeholder(R.drawable.ic_flowave_album)
                .error(R.drawable.ic_flowave_album)
                .size(Size(256, 256))
                .precision(Precision.EXACT)
                .build()

            AsyncImage(
                model = imageRequest,
                contentDescription = "Обложка трека",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
        } else if (trackNumber != null) {
            Box(
                modifier = Modifier.width(40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isPlaying) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_flowave_equalizer),
                        contentDescription = "Играет",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = trackNumber.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.width(40.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.displayTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = headlineColor
            )
            Text(
                text = track.displayArtist,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isTrackAvailable) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // --- ИЗМЕНЕНИЕ 2: Оборачиваем кнопки в условие ---
        if (showFavoriteButton || (showTrailingContent && trailingContent != null)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showFavoriteButton && onFavoriteClick != null && isTrackAvailable) {
                    IconButton(onClick = { onFavoriteClick(track) }) {
                        Icon(
                            painter = painterResource(id = if (track.isFavorite) R.drawable.ic_flowave_favorite_filled else R.drawable.ic_flowave_favorite_border),
                            contentDescription = "В избранное",
                            modifier = Modifier.size(22.dp),
                            tint = if (track.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (showTrailingContent && trailingContent != null && isTrackAvailable) {
                    trailingContent()
                }
            }
        }
    }
}