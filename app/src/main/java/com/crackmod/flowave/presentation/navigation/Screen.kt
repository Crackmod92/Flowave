// ПУТЬ: com/crackmod/flowave/presentation/navigation/Screen.kt
// КОД:

package com.crackmod.flowave.presentation.navigation

import androidx.annotation.DrawableRes
import com.crackmod.flowave.R

sealed class Screen(val route: String, val title: String, @DrawableRes val iconResId: Int) {
    // Основные экраны (с BottomNav)
    object Home : Screen("home", "Главная", R.drawable.ic_flowave_library_music)
    object AllTracks : Screen("all_tracks", "Треки", R.drawable.ic_flowave_music_note)
    object Search : Screen("search", "Поиск", R.drawable.ic_flowave_search)
    object Settings : Screen("settings", "Настройки", R.drawable.ic_flowave_settings)

    // Экраны коллекций
    object Playlists : Screen("all_playlists", "Плейлисты", R.drawable.ic_flowave_playlist)
    object Artists : Screen("all_artists", "Артисты", R.drawable.ic_flowave_artist)
    object Albums : Screen("all_albums", "Альбомы", R.drawable.ic_flowave_album)

    // Экраны деталей
    object AlbumDetail : Screen("album_detail/{albumId}", "Альбом", 0)
    object PlaylistDetail : Screen("playlist_detail/{playlistId}", "Плейлист", 0)
    object ArtistDetail : Screen("artist_detail/{artistId}", "Исполнитель", 0)

    // Функциональные экраны
    object Lyrics : Screen("lyrics/{trackId}", "Текст песни", 0)
    object Equalizer : Screen("equalizer", "Эквалайзер", 0)
    object AudioEffects : Screen("audio_effects", "Аудиоэффекты", 0)

    // НОВЫЕ ЭКРАНЫ НАСТРОЕК
    object AppearanceSettings : Screen("settings_appearance", "Внешний вид", 0)
    object AudioSettings : Screen("settings_audio", "Звук и воспроизведение", 0)
    object LibrarySettings : Screen("settings_library", "Медиатека", 0)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.AllTracks,
    Screen.Search,
    Screen.Settings,
)