package com.crackmod.flowave.data.remote.dto

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LrcLibApiService {

    @GET("api/get")
    suspend fun getLyrics(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String,
        @Query("album_name") albumName: String,
        @Query("duration") duration: Double
    ): Response<LyricsResponse>

    // НОВЫЙ МЕТОД ДЛЯ БЫСТРОГО ЗАПРОСА
    @GET("api/get-cached")
    suspend fun getCachedLyrics(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String,
        @Query("album_name") albumName: String,
        @Query("duration") duration: Double
    ): Response<LyricsResponse>
}