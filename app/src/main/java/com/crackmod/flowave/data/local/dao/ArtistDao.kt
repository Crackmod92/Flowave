package com.crackmod.flowave.data.local.dao

import androidx.room.*
import com.crackmod.flowave.data.local.entity.ArtistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {

    @Query("SELECT * FROM artists ORDER BY name ASC")
    fun getAllArtists(): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM artists WHERE id = :id")
    fun getArtistById(id: Long): Flow<ArtistEntity?> // <-- ИЗМЕНЕНИЕ

    @Query("SELECT * FROM artists WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchArtists(query: String): Flow<List<ArtistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: ArtistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artists: List<ArtistEntity>)

    @Update
    suspend fun updateArtist(artist: ArtistEntity)

    @Delete
    suspend fun deleteArtist(artist: ArtistEntity)

    @Query("DELETE FROM artists")
    suspend fun deleteAllArtists()

    @Query("SELECT COUNT(*) FROM artists")
    suspend fun getArtistCount(): Int
}