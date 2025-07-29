// ПУТЬ: com/crackmod/flowave/presentation/screens/tag_editor/TagEditorSheet.txt

package com.crackmod.flowave.presentation.screens.tag_editor

import android.app.Activity
import android.content.ContentUris
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.crackmod.flowave.R
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.presentation.screens.library.LibraryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagEditorSheet(
    trackId: Long,
    onDismiss: (didSave: Boolean) -> Unit,
    viewModel: TagEditorViewModel = hiltViewModel(key = trackId.toString()),
    // --- ИЗМЕНЕНИЕ: Добавляем LibraryViewModel для баннеров ---
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    LaunchedEffect(trackId) {
        viewModel.loadTrack(trackId)
    }

    val state by viewModel.state.collectAsState()

    var title by remember(state.track) { mutableStateOf(state.track?.title ?: "") }
    var artist by remember(state.track) { mutableStateOf(state.track?.artist ?: "") }
    var album by remember(state.track) { mutableStateOf(state.track?.album ?: "") }
    var albumArtist by remember(state.track) { mutableStateOf(state.track?.albumArtist ?: "") }
    var year by remember(state.track) { mutableStateOf(state.track?.year?.toString() ?: "") }
    var genre by remember(state.track) { mutableStateOf(state.track?.genre ?: "") }
    var trackNumber by remember(state.track) { mutableStateOf(state.track?.trackNumber?.toString() ?: "") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.saveTags(title, artist, album, albumArtist, year.toIntOrNull(), genre, trackNumber.toIntOrNull())
        }
    }

    LaunchedEffect(state.permissionRequest) {
        state.permissionRequest?.let { pendingIntent ->
            val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
            permissionLauncher.launch(intentSenderRequest)
            viewModel.permissionRequestHandled()
        }
    }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            onDismiss(true) // onDismiss теперь отвечает за показ баннера
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            // --- ИЗМЕНЕНИЕ: Используем баннер для ошибок ---
            libraryViewModel.showBanner("Ошибка: $it", isError = true)
            viewModel.clearError()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onDismiss(false) },
        sheetState = sheetState,
        windowInsets = WindowInsets(0.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Scaffold(
            bottomBar = {
                Button(
                    onClick = {
                        viewModel.saveTags(title, artist, album, albumArtist, year.toIntOrNull(), genre, trackNumber.toIntOrNull())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp)
                        .navigationBarsPadding(),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Сохранить")
                    }
                }
            }
        ) { paddingValues ->
            if (state.track == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .imePadding()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    EditorHeader(track = state.track!!)
                    Spacer(Modifier.height(16.dp))
                    TagEditorField(icon = Icons.Outlined.MusicNote, label = "Название трека", value = title, onValueChange = { title = it }, keyboardType = KeyboardType.Text)
                    TagEditorField(icon = Icons.Outlined.Person, label = "Исполнитель", value = artist, onValueChange = { artist = it }, keyboardType = KeyboardType.Text)
                    TagEditorField(icon = Icons.Outlined.Album, label = "Альбом", value = album, onValueChange = { album = it }, keyboardType = KeyboardType.Text)
                    TagEditorField(icon = Icons.Outlined.Group, label = "Исполнитель альбома", value = albumArtist, onValueChange = { albumArtist = it }, keyboardType = KeyboardType.Text)
                    TagEditorField(icon = Icons.Outlined.CalendarToday, label = "Год", value = year, onValueChange = { year = it }, keyboardType = KeyboardType.Number)
                    TagEditorField(icon = Icons.Outlined.Label, label = "Жанр", value = genre, onValueChange = { genre = it }, keyboardType = KeyboardType.Text)
                    TagEditorField(icon = Icons.Outlined.Pin, label = "Номер трека", value = trackNumber, onValueChange = { trackNumber = it }, keyboardType = KeyboardType.Number)
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun EditorHeader(track: Track) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(track.albumId?.let { ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, it) })
                .crossfade(true)
                .error(R.drawable.ic_flowave_album)
                .build(),
            contentDescription = "Обложка альбома",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(track.displayTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(track.displayArtist, style = MaterialTheme.typography.bodyMedium, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    Divider()
}

@Composable
private fun TagEditorField(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(16.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
            )
        }
        Divider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
    }
}