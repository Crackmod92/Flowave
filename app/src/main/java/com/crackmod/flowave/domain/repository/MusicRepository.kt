// ФАЙЛ: MusicRepository.txt
// ПУТЬ: com/crackmod/flowave/domain/repository/MusicRepository.txt

package com.crackmod.flowave.domain.repository

import android.net.Uri
import com.crackmod.flowave.domain.model.Track
import com.crackmod.flowave.domain.model.Album
import com.crackmod.flowave.domain.model.Artist
import com.crackmod.flowave.domain.model.Lyrics
import com.crackmod.flowave.domain.model.Playlist
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    // Tracks
    fun getAllTracks(): Flow<List<Track>>
    fun getTrackById(id: Long): Flow<Track?>
    suspend fun getTracksByIds(ids: List<Long>): List<Track>
    fun getFavoriteTracks(): Flow<List<Track>>
    fun getFavoriteTrackCount(): Flow<Int>
    fun searchTracks(query: String): Flow<List<Track>>
    fun getRecentlyAddedTracks(limit: Int = 50): Flow<List<Track>>
    fun getMostPlayedTracks(limit: Int = 50): Flow<List<Track>>
    fun getTracksByAlbum(albumId: Long): Flow<List<Track>>
    fun getTracksByArtist(artistId: Long): Flow<List<Track>>
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)
    suspend fun incrementPlayCount(id: Long, timestamp: Long)
    suspend fun updateTrackAvailability(id: Long, isAvailable: Boolean)
    suspend fun updateTrackMetadata(
        trackId: Long,
        newTitle: String,
        newArtist: String,
        newAlbum: String,
        newAlbumArtist: String,
        newYear: Int?,
        newGenre: String?,
        newTrackNumber: Int?
    )
    suspend fun deleteTrack(trackId: Long)

    // Albums
    fun getAllAlbums(): Flow<List<Album>>
    fun getAlbumById(id: Long): Flow<Album?>
    fun searchAlbums(query: String): Flow<List<Album>>
    fun getAlbumsByArtist(artistName: String): Flow<List<Album>>
    fun getRecentlyAddedAlbums(limit: Int = 50): Flow<List<Album>>
    suspend fun deleteAlbum(albumId: Long)

    // Artists
    fun getAllArtists(): Flow<List<Artist>>
    fun getArtistById(id: Long): Flow<Artist?>
    fun searchArtists(query: String): Flow<List<Artist>>

    // Playlists
    fun getAllPlaylists(): Flow<List<Playlist>>
    fun getPlaylistById(id: String): Flow<Playlist?>
    suspend fun createPlaylist(name: String, description: String?): Playlist
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun deletePlaylist(playlistId: String)
    suspend fun addTrackToPlaylist(playlistId: String, trackId: Long)
    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: Long)
    fun getPlaylistTracks(playlistId: String): Flow<List<Track>>

    // Media scanning
    suspend fun scanMediaLibrary(scanFolders: List<Uri>? = null): Result<Int>

    // Stats
    suspend fun getTrackCount(): Int
    suspend fun getAlbumCount(): Int
    suspend fun getArtistCount(): Int

    // --- Lyrics ---
    suspend fun getLyricsForTrack(track: Track): Flow<Result<Lyrics>>

}