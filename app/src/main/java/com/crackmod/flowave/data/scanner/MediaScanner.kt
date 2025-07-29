package com.crackmod.flowave.data.scanner

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.crackmod.flowave.data.local.entity.AlbumEntity
import com.crackmod.flowave.data.local.entity.ArtistEntity
import com.crackmod.flowave.data.local.entity.TrackEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val contentResolver: ContentResolver = context.contentResolver

    private fun getTrackProjection(): Array<String> {
        val projection = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.SIZE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projection.add(MediaStore.Audio.Media.GENRE)
            projection.add(MediaStore.Audio.Media.ALBUM_ARTIST)
        }
        return projection.toTypedArray()
    }

    private val albumProjection = arrayOf(
        MediaStore.Audio.Albums._ID,
        MediaStore.Audio.Albums.ALBUM,
        MediaStore.Audio.Albums.ARTIST,
        MediaStore.Audio.Albums.FIRST_YEAR,
        MediaStore.Audio.Albums.NUMBER_OF_SONGS,
        MediaStore.Audio.Albums.ALBUM_ART
    )

    private val artistProjection = arrayOf(
        MediaStore.Audio.Artists._ID,
        MediaStore.Audio.Artists.ARTIST,
        MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
        MediaStore.Audio.Artists.NUMBER_OF_TRACKS
    )

    private fun getPathFromUri(uri: Uri): String? {
        val path = uri.path ?: return null
        val treeId = path.substringAfter("/tree/", "").ifEmpty { return null }
        val parts = treeId.split(":")
        if (parts.size != 2) return null
        val volume = parts[0]
        val subPath = parts[1]
        return when (volume) {
            "primary" -> "${Environment.getExternalStorageDirectory().path}/$subPath"
            else -> null
        }
    }

    suspend fun scanAudioFiles(folders: List<Uri>?): List<TrackEntity> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<TrackEntity>()
        val trackProjection = getTrackProjection()
        val selection = StringBuilder("${MediaStore.Audio.Media.IS_MUSIC} = 1")
        val selectionArgs = mutableListOf<String>()

        val pathsToScan: List<String> = if (folders.isNullOrEmpty()) {
            listOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
            )
        } else {
            folders.mapNotNull { getPathFromUri(it) }
        }

        if (pathsToScan.isNotEmpty()) {
            selection.append(" AND (")
            pathsToScan.forEachIndexed { index, path ->
                selection.append("${MediaStore.Audio.Media.DATA} LIKE ?")
                selectionArgs.add("$path%")
                if (index < pathsToScan.size - 1) {
                    selection.append(" OR ")
                }
            }
            selection.append(")")
        } else if (!folders.isNullOrEmpty()) {
            return@withContext emptyList()
        }

        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            trackProjection,
            selection.toString(),
            selectionArgs.toTypedArray(),
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val artistIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val albumArtistColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ARTIST) else -1
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val genreColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) cursor.getColumnIndex(MediaStore.Audio.Media.GENRE) else -1

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn) // <-- Читаем как Long
                val contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    .buildUpon()
                    .appendPath(id.toString())
                    .build()

                tracks.add(
                    TrackEntity(
                        id = id, // <-- Сохраняем как Long
                        title = cursor.getString(titleColumn) ?: "Unknown",
                        artist = cursor.getString(artistColumn) ?: "Unknown Artist",
                        album = cursor.getString(albumColumn) ?: "Unknown Album",
                        albumId = cursor.getLong(albumIdColumn),
                        artistId = cursor.getLong(artistIdColumn), // <-- Сохраняем как Long
                        albumArtist = if (albumArtistColumn != -1) cursor.getString(albumArtistColumn) ?: cursor.getString(artistColumn) ?: "" else cursor.getString(artistColumn) ?: "",
                        duration = cursor.getLong(durationColumn),
                        year = cursor.getInt(yearColumn).takeIf { it != 0 },
                        genre = if (genreColumn != -1) cursor.getString(genreColumn) else null,
                        trackNumber = cursor.getInt(trackColumn).takeIf { it != 0 },
                        path = cursor.getString(dataColumn) ?: "",
                        contentUri = contentUri.toString(),
                        bitrate = null,
                        dateAdded = cursor.getLong(dateAddedColumn) * 1000,
                        dateModified = cursor.getLong(dateModifiedColumn) * 1000,
                        size = cursor.getLong(sizeColumn),
                        albumArtPath = null,
                        isAvailable = true
                    )
                )
            }
        }
        tracks
    }

    suspend fun scanAlbums(): List<AlbumEntity> = withContext(Dispatchers.IO) {
        val albums = mutableListOf<AlbumEntity>()
        contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            albumProjection,
            null,
            null,
            "${MediaStore.Audio.Albums.ALBUM} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
            val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.FIRST_YEAR)
            val trackCountColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)
            val albumArtColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART)
            while (cursor.moveToNext()) {
                albums.add(
                    AlbumEntity(
                        id = cursor.getLong(idColumn), // <-- Сохраняем как Long
                        title = cursor.getString(albumColumn) ?: "Unknown Album",
                        artist = cursor.getString(artistColumn) ?: "Unknown Artist",
                        year = cursor.getInt(yearColumn).takeIf { it != 0 },
                        trackCount = cursor.getInt(trackCountColumn),
                        albumArtPath = cursor.getString(albumArtColumn),
                        dateAdded = System.currentTimeMillis()
                    )
                )
            }
        }
        albums
    }

    suspend fun scanArtists(): List<ArtistEntity> = withContext(Dispatchers.IO) {
        val artists = mutableListOf<ArtistEntity>()
        contentResolver.query(
            MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
            artistProjection,
            null,
            null,
            "${MediaStore.Audio.Artists.ARTIST} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)
            val albumCountColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)
            val trackCountColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
            while (cursor.moveToNext()) {
                artists.add(
                    ArtistEntity(
                        id = cursor.getLong(idColumn), // <-- Сохраняем как Long
                        name = cursor.getString(artistColumn) ?: "Unknown Artist",
                        albumCount = cursor.getInt(albumCountColumn),
                        trackCount = cursor.getInt(trackCountColumn),
                        dateAdded = System.currentTimeMillis()
                    )
                )
            }
        }
        artists
    }
}