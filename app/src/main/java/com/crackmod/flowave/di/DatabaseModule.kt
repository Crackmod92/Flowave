package com.crackmod.flowave.di

import android.content.Context
import androidx.room.Room
import com.crackmod.flowave.data.local.FloWaveDatabase
import com.crackmod.flowave.data.local.MIGRATION_3_4 // ИМПОРТИРУЕМ МИГРАЦИЮ
import com.crackmod.flowave.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFloWaveDatabase(
        @ApplicationContext context: Context
    ): FloWaveDatabase {
        return Room.databaseBuilder(
            context,
            FloWaveDatabase::class.java,
            FloWaveDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_3_4) // ДОБАВЛЯЕМ МИГРАЦИЮ
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTrackDao(database: FloWaveDatabase): TrackDao {
        return database.trackDao()
    }

    @Provides
    fun provideAlbumDao(database: FloWaveDatabase): AlbumDao {
        return database.albumDao()
    }

    @Provides
    fun provideArtistDao(database: FloWaveDatabase): ArtistDao {
        return database.artistDao()
    }

    @Provides
    fun providePlaylistDao(database: FloWaveDatabase): PlaylistDao {
        return database.playlistDao()
    }
}