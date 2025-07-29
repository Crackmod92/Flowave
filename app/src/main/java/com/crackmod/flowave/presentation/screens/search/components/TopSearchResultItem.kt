package com.crackmod.flowave.presentation.screens.search.components

import android.content.ContentUris
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.crackmod.flowave.R
import com.crackmod.flowave.domain.model.Album
import com.crackmod.flowave.domain.model.Artist
import com.crackmod.flowave.domain.model.Track

@Composable
fun TopSearchResultItem(
    result: Any,
    onResultClick: (Any) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onResultClick(result) },
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Изображение
            Box(
                modifier = Modifier
                    .size(80.dp)
            ) {
                when (result) {
                    is Track -> {
                        val albumArtUri = result.albumId?.let {
                            ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, it)
                        }
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(albumArtUri)
                                .crossfade(true)
                                .error(R.drawable.ic_flowave_album)
                                .build(),
                            contentDescription = "Обложка",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    is Album -> {
                        val albumArtUri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, result.id)
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(albumArtUri)
                                .crossfade(true)
                                .error(R.drawable.ic_flowave_album)
                                .build(),
                            contentDescription = "Обложка альбома",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    is Artist -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = result.name.firstOrNull()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            // Текст
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                val title = when (result) {
                    is Track -> result.displayTitle
                    is Album -> result.displayTitle
                    is Artist -> result.displayName
                    else -> ""
                }
                val subtitle = when (result) {
                    is Track -> "Трек • ${result.displayArtist}"
                    is Album -> "Альбом • ${result.displayArtist}"
                    is Artist -> "Исполнитель"
                    else -> ""
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}