// ФАЙЛ: AlbumGridItem.txt
// ПУТЬ: com/crackmod/flowave/presentation/screens/library/components/AlbumGridItem.txt

package com.crackmod.flowave.presentation.screens.library.components

import android.content.ContentUris
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision // <-- ИМПОРТ
import coil.size.Size // <-- ИМПОРТ
import com.crackmod.flowave.R
import com.crackmod.flowave.domain.model.Album

@Composable
fun AlbumGridItem(
    album: Album,
    onAlbumClick: (Album) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onAlbumClick(album) }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        val albumArtUri = try {
            ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, album.id)
        } catch (e: Exception) {
            null
        }

        // --- ГЛАВНОЕ ИЗМЕНЕНИЕ: Оптимизируем запрос изображения ---
        val imageRequest = ImageRequest.Builder(LocalContext.current)
            .data(albumArtUri)
            .crossfade(true)
            .error(R.drawable.ic_flowave_album)
            .placeholder(R.drawable.ic_flowave_album) // Добавляем placeholder для лучшего UX
            .size(Size(300, 300)) // Говорим Coil'у загружать превью 300x300
            .precision(Precision.EXACT) // Убеждаемся, что размер будет именно таким
            .build()
        // --- КОНЕЦ ИЗМЕНЕНИЯ ---

        AsyncImage(
            model = imageRequest, // Используем наш оптимизированный запрос
            contentDescription = album.displayTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.2f to Color.Transparent,
                        1.0f to Color.Black
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Text(
                text = album.displayTitle,
                style = MaterialTheme.typography.titleSmall.copy(shadow = Shadow(color = Color.Black.copy(alpha = 1f), offset = Offset(2f, 2f), blurRadius = 4f)),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = album.displayArtist,
                style = MaterialTheme.typography.bodySmall.copy(shadow = Shadow(color = Color.Black.copy(alpha = 1f), offset = Offset(2f, 2f), blurRadius = 4f)),
                color = Color.White.copy(alpha = 0.95f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}