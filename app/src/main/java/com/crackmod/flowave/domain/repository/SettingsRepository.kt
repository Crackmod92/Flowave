// ПУТЬ: com/crackmod/flowave/domain/repository/SettingsRepository.kt
// КОД:

package com.crackmod.flowave.domain.repository

import android.annotation.SuppressLint
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

enum class ThemeSetting {
    SYSTEM, LIGHT, DARK, AMOLED
}

enum class NowPlayingScreenStyle {
    SOLAR_FLARE, PULSAR, EVENT_HORIZON, WARP_DRIVE, AURORA,
    NEBULA, CONSTELLATION, ASTEROID_BELT, GALACTIC_CORE, SPACE_ODYSSEY
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class EqualizerSettings(
    val isEnabled: Boolean = false,
    val bandLevels: Map<Int, Short> = emptyMap(),
    val preampLevel: Short = 0,
    val currentPreset: String = "Custom"
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class BassBoostSettings(
    val isEnabled: Boolean = false,
    val strength: Short = 0 // 0-1000
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class VirtualizerSettings(
    val isEnabled: Boolean = false,
    val strength: Short = 0 // 0-1000
)

interface SettingsRepository {
    val themeSetting: Flow<ThemeSetting>
    suspend fun setThemeSetting(theme: ThemeSetting)

    val scanFolders: Flow<List<Uri>>
    suspend fun setScanFolders(folders: List<Uri>)

    val nowPlayingScreenStyle: Flow<NowPlayingScreenStyle>
    suspend fun setNowPlayingScreenStyle(style: NowPlayingScreenStyle)

    val albumListViewTypeIsGrid: Flow<Boolean>
    suspend fun setAlbumListViewType(isGrid: Boolean)

    val initialScanCompleted: Flow<Boolean>
    suspend fun setInitialScanCompleted(completed: Boolean)

    // +++ НОВОЕ ПОЛЕ И МЕТОД +++
    val originalQueue: Flow<List<Long>>
    suspend fun setOriginalQueue(ids: List<Long>)
    // +++ КОНЕЦ +++

    val lastQueue: Flow<List<Long>>
    suspend fun setLastQueue(ids: List<Long>)

    val lastTrackIndex: Flow<Int>
    suspend fun setLastTrackIndex(index: Int)

    val lastTrackPosition: Flow<Long>
    suspend fun setLastTrackPosition(position: Long)

    val lastShuffleMode: Flow<Boolean>
    suspend fun setLastShuffleMode(enabled: Boolean)

    val lastRepeatMode: Flow<Int>
    suspend fun setLastRepeatMode(mode: Int)

    val equalizerSettings: Flow<EqualizerSettings>
    suspend fun setEqualizerSettings(settings: EqualizerSettings)

    val bassBoostSettings: Flow<BassBoostSettings>
    suspend fun setBassBoostSettings(settings: BassBoostSettings)

    val virtualizerSettings: Flow<VirtualizerSettings>
    suspend fun setVirtualizerSettings(settings: VirtualizerSettings)

    val enableVolumeNormalization: Flow<Boolean>
    suspend fun setEnableVolumeNormalization(isEnabled: Boolean)

    val gaplessPlaybackEnabled: Flow<Boolean>
    suspend fun setGaplessPlaybackEnabled(isEnabled: Boolean)
}