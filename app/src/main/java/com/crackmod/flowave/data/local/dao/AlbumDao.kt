package com.crackmod.flowave.data.local.dao

import androidx.room.*
import com.crackmod.flowave.data.local.entity.AlbumEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Query("SELECT * FROM albums ORDER BY title ASC")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE id = :id")
    fun getAlbumById(id: Long): Flow<AlbumEntity?> // <-- ИЗМЕНЕНИЕ

    @Query("SELECT * FROM albums WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchAlbums(query: String): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE artist = :artistName ORDER BY year DESC, title ASC")
    fun getAlbumsByArtist(artistName: String): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums ORDER BY dateAdded DESC LIMIT :limit")
    fun getRecentlyAddedAlbums(limit: Int = 50): Flow<List<AlbumEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>)

    @Update
    suspend fun updateAlbum(album: AlbumEntity)

    @Delete
    suspend fun deleteAlbum(album: AlbumEntity)

    @Query("DELETE FROM albums")
    suspend fun deleteAllAlbums()

    @Query("SELECT COUNT(*) FROM albums")
    suspend fun getAlbumCount(): Int
}