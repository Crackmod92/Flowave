package com.crackmod.flowave.data.local.dao

import androidx.room.*
import com.crackmod.flowave.data.local.entity.PlaylistEntity
import com.crackmod.flowave.data.local.entity.PlaylistTrackEntity
import com.crackmod.flowave.data.local.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    fun getPlaylistById(id: String): Flow<PlaylistEntity?>

    @Query("""
        SELECT t.* FROM tracks t
        INNER JOIN playlist_tracks pt ON t.id = pt.trackId
        WHERE pt.playlistId = :playlistId
        ORDER BY pt.position ASC
    """)
    fun getPlaylistTracks(playlistId: String): Flow<List<TrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrack(playlistTrack: PlaylistTrackEntity)

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: Long)

    @Query("SELECT MAX(position) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getLastPosition(playlistId: String): Int?

    // --- ИЗМЕНЕНИЕ: Превращаем suspend fun в Flow ---
    @Query("SELECT COUNT(*) FROM playlist_tracks WHERE playlistId = :playlistId")
    fun getPlaylistTrackCount(playlistId: String): Flow<Int>
}