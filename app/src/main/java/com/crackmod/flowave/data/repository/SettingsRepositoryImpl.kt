// ПУТЬ: com/crackmod/flowave/data/repository/SettingsRepositoryImpl.kt
// КОД:

package com.crackmod.flowave.data.repository

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.media3.common.Player
import com.crackmod.flowave.domain.repository.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json
) : SettingsRepository {

    private object PreferencesKeys {
        val THEME_SETTING = stringPreferencesKey("theme_setting")
        val SCAN_FOLDERS = stringPreferencesKey("scan_folders")
        val NOW_PLAYING_SCREEN_STYLE = stringPreferencesKey("now_playing_screen_style")
        val ALBUM_LIST_VIEW_TYPE_IS_GRID = booleanPreferencesKey("album_list_view_type_is_grid")
        val INITIAL_SCAN_COMPLETED = booleanPreferencesKey("initial_scan_completed")
        // +++ НОВЫЙ КЛЮЧ +++
        val ORIGINAL_QUEUE_IDS = stringSetPreferencesKey("original_queue_ids")
        val LAST_QUEUE_IDS = stringSetPreferencesKey("last_queue_ids")
        val LAST_TRACK_INDEX = intPreferencesKey("last_track_index")
        val LAST_TRACK_POSITION = longPreferencesKey("last_track_position")
        val LAST_SHUFFLE_MODE = booleanPreferencesKey("last_shuffle_mode")
        val LAST_REPEAT_MODE = intPreferencesKey("last_repeat_mode")
        val EQUALIZER_SETTINGS = stringPreferencesKey("equalizer_settings_v3")
        val BASS_BOOST_SETTINGS = stringPreferencesKey("bass_boost_settings_v2")
        val VIRTUALIZER_SETTINGS = stringPreferencesKey("virtualizer_settings_v2")
        val ENABLE_VOLUME_NORMALIZATION = booleanPreferencesKey("enable_volume_normalization")
        val GAPLESS_PLAYBACK_ENABLED = booleanPreferencesKey("gapless_playback_enabled")
    }

    override val themeSetting: Flow<ThemeSetting> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[PreferencesKeys.THEME_SETTING] ?: ThemeSetting.SYSTEM.name
            ThemeSetting.valueOf(themeName)
        }

    override suspend fun setThemeSetting(theme: ThemeSetting) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.THEME_SETTING] = theme.name
        }
    }

    override val scanFolders: Flow<List<Uri>> = context.dataStore.data
        .map { preferences ->
            val jsonString = preferences[PreferencesKeys.SCAN_FOLDERS] ?: "[]"
            try {
                json.decodeFromString<List<String>>(jsonString).map { Uri.parse(it) }
            } catch (e: Exception) {
                emptyList()
            }
        }

    override suspend fun setScanFolders(folders: List<Uri>) {
        context.dataStore.edit { settings ->
            val jsonString = json.encodeToString(folders.map { it.toString() })
            settings[PreferencesKeys.SCAN_FOLDERS] = jsonString
        }
    }

    override val nowPlayingScreenStyle: Flow<NowPlayingScreenStyle> = context.dataStore.data
        .map { preferences ->
            val styleName = preferences[PreferencesKeys.NOW_PLAYING_SCREEN_STYLE] ?: NowPlayingScreenStyle.SOLAR_FLARE.name
            try {
                NowPlayingScreenStyle.valueOf(styleName)
            } catch (e: IllegalArgumentException) {
                NowPlayingScreenStyle.SOLAR_FLARE
            }
        }

    override suspend fun setNowPlayingScreenStyle(style: NowPlayingScreenStyle) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.NOW_PLAYING_SCREEN_STYLE] = style.name
        }
    }

    override val albumListViewTypeIsGrid: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ALBUM_LIST_VIEW_TYPE_IS_GRID] ?: true
        }

    override suspend fun setAlbumListViewType(isGrid: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.ALBUM_LIST_VIEW_TYPE_IS_GRID] = isGrid
        }
    }

    override val initialScanCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.INITIAL_SCAN_COMPLETED] ?: false
        }

    override suspend fun setInitialScanCompleted(completed: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.INITIAL_SCAN_COMPLETED] = completed
        }
    }

    // +++ НОВЫЙ КОД +++
    override val originalQueue: Flow<List<Long>> = context.dataStore.data
        .map { preferences ->
            (preferences[PreferencesKeys.ORIGINAL_QUEUE_IDS] ?: emptySet()).mapNotNull { it.toLongOrNull() }
        }

    override suspend fun setOriginalQueue(ids: List<Long>) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.ORIGINAL_QUEUE_IDS] = ids.map { it.toString() }.toSet()
        }
    }
    // +++ КОНЕЦ +++

    override val lastQueue: Flow<List<Long>> = context.dataStore.data
        .map { preferences ->
            (preferences[PreferencesKeys.LAST_QUEUE_IDS] ?: emptySet()).mapNotNull { it.toLongOrNull() }
        }

    override suspend fun setLastQueue(ids: List<Long>) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.LAST_QUEUE_IDS] = ids.map { it.toString() }.toSet()
        }
    }

    override val lastTrackIndex: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_TRACK_INDEX] ?: 0
        }

    override suspend fun setLastTrackIndex(index: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.LAST_TRACK_INDEX] = index
        }
    }

    override val lastTrackPosition: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_TRACK_POSITION] ?: 0L
        }

    override suspend fun setLastTrackPosition(position: Long) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.LAST_TRACK_POSITION] = position
        }
    }

    override val lastShuffleMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_SHUFFLE_MODE] ?: false
        }

    override suspend fun setLastShuffleMode(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.LAST_SHUFFLE_MODE] = enabled
        }
    }

    override val lastRepeatMode: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_REPEAT_MODE] ?: Player.REPEAT_MODE_OFF
        }

    override suspend fun setLastRepeatMode(mode: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.LAST_REPEAT_MODE] = mode
        }
    }

    override val equalizerSettings: Flow<EqualizerSettings> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.EQUALIZER_SETTINGS]?.let {
                try {
                    json.decodeFromString<EqualizerSettings>(it)
                } catch (e: Exception) {
                    EqualizerSettings()
                }
            } ?: EqualizerSettings()
        }

    override suspend fun setEqualizerSettings(settings: EqualizerSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EQUALIZER_SETTINGS] = json.encodeToString(settings)
        }
    }

    override val bassBoostSettings: Flow<BassBoostSettings> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.BASS_BOOST_SETTINGS]?.let {
                try {
                    json.decodeFromString<BassBoostSettings>(it)
                } catch (e: Exception) {
                    BassBoostSettings()
                }
            } ?: BassBoostSettings()
        }

    override suspend fun setBassBoostSettings(settings: BassBoostSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BASS_BOOST_SETTINGS] = json.encodeToString(settings)
        }
    }

    override val virtualizerSettings: Flow<VirtualizerSettings> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.VIRTUALIZER_SETTINGS]?.let {
                try {
                    json.decodeFromString<VirtualizerSettings>(it)
                } catch (e: Exception) {
                    VirtualizerSettings()
                }
            } ?: VirtualizerSettings()
        }

    override suspend fun setVirtualizerSettings(settings: VirtualizerSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIRTUALIZER_SETTINGS] = json.encodeToString(settings)
        }
    }

    override val enableVolumeNormalization: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ENABLE_VOLUME_NORMALIZATION] ?: false
        }

    override suspend fun setEnableVolumeNormalization(isEnabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.ENABLE_VOLUME_NORMALIZATION] = isEnabled
        }
    }

    override val gaplessPlaybackEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.GAPLESS_PLAYBACK_ENABLED] ?: false
        }

    override suspend fun setGaplessPlaybackEnabled(isEnabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.GAPLESS_PLAYBACK_ENABLED] = isEnabled
        }
    }
}