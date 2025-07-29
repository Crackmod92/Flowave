package com.crackmod.flowave.data.local.dao

import androidx.room.*
import com.crackmod.flowave.data.local.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Query("SELECT * FROM tracks ORDER BY title ASC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id")
    fun getTrackById(id: Long): Flow<TrackEntity?> // <-- ИЗМЕНЕНИЕ

    @Query("SELECT * FROM tracks WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%'")
    fun searchTracks(query: String): Flow<List<TrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Update
    suspend fun updateTrack(track: TrackEntity)

    @Delete
    suspend fun deleteTrack(track: TrackEntity)

    @Query("DELETE FROM tracks")
    suspend fun deleteAllTracks()

    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun getTrackCount(): Int

    @Query("SELECT COUNT(*) FROM tracks WHERE isFavorite = 1")
    fun getFavoriteTrackCount(): Flow<Int>

    @Query("UPDATE tracks SET isFavorite = :isFavorite WHERE id = :trackId")
    suspend fun updateFavoriteStatus(trackId: Long, isFavorite: Boolean) // <-- ИЗМЕНЕНИЕ

    @Query("UPDATE tracks SET playCount = playCount + 1 WHERE id = :trackId")
    suspend fun incrementPlayCount(trackId: Long) // <-- ИЗМЕНЕНИЕ

    @Query("UPDATE tracks SET lastPlayed = :timestamp WHERE id = :trackId")
    suspend fun updateLastPlayed(trackId: Long, timestamp: Long) // <-- ИЗМЕНЕНИЕ

    @Query("SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY dateAdded DESC LIMIT :limit")
    fun getRecentlyAddedTracks(limit: Int): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY playCount DESC LIMIT :limit")
    fun getMostPlayedTracks(limit: Int): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE albumId = :albumId ORDER BY trackNumber ASC, title ASC")
    fun getTracksByAlbum(albumId: Long): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE artistId = :artistId ORDER BY year DESC, title ASC")
    fun getTracksByArtist(artistId: Long): Flow<List<TrackEntity>> // <-- ИЗМЕНЕНИЕ

    @Query("UPDATE tracks SET isAvailable = :isAvailable WHERE id = :trackId")
    suspend fun updateTrackAvailability(trackId: Long, isAvailable: Boolean) // <-- ИЗМЕНЕНИЕ

    @Query("SELECT * FROM tracks WHERE id IN (:ids)")
    suspend fun getTracksByIds(ids: List<Long>): List<TrackEntity> // <-- ИЗМЕНЕНИЕ
}