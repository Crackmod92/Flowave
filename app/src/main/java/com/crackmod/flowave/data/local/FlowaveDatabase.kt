package com.crackmod.flowave.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.crackmod.flowave.data.local.dao.*
import com.crackmod.flowave.data.local.entity.*

@Database(
    entities = [
        TrackEntity::class,
        AlbumEntity::class,
        ArtistEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class
    ],
    version = 4, // УВЕЛИЧИВАЕМ ВЕРСИЮ
    exportSchema = false
)
abstract class FloWaveDatabase : RoomDatabase() {

    abstract fun trackDao(): TrackDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        const val DATABASE_NAME = "flowave_database"
    }
}

// ДОБАВЛЯЕМ ОБЪЕКТ МИГРАЦИИ
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tracks ADD COLUMN syncedLyrics TEXT")
        db.execSQL("ALTER TABLE tracks ADD COLUMN plainLyrics TEXT")
        db.execSQL("ALTER TABLE tracks ADD COLUMN areLyricsInstrumental INTEGER NOT NULL DEFAULT 0")
    }
}