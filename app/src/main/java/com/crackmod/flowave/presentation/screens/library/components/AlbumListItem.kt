package com.crackmod.flowave.presentation.screens.library.components

import android.content.ContentUris
import android.provider.MediaStore
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.crackmod.flowave.R
import com.crackmod.flowave.domain.model.Album

@Composable
fun AlbumListItem(
    album: Album,
    onAlbumClick: (Album) -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier.clickable { onAlbumClick(album) },
        headlineContent = { Text(album.displayTitle) },
        supportingContent = { Text(album.displayArtist) },
        leadingContent = {
            val albumArtUri = try {
                ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, album.id)
            } catch (e: Exception) { null }

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(albumArtUri)
                    .crossfade(true)
                    .error(R.drawable.ic_flowave_album)
                    .placeholder(R.drawable.ic_flowave_album)
                    .build(),
                contentDescription = "Album Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
            )
        }
    )
}