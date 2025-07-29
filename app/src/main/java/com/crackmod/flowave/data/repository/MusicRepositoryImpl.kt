// ФАЙЛ: MusicRepositoryImpl.txt
// ПУТЬ: com/crackmod/flowave/data/repository/MusicRepositoryImpl.txt

package com.crackmod.flowave.data.repository

import android.app.RecoverableSecurityException
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import com.crackmod.flowave.data.local.dao.*
import com.crackmod.flowave.data.mapper.*
import com.crackmod.flowave.data.remote.dto.LrcLibApiService
import com.crackmod.flowave.data.scanner.MediaScanner
import com.crackmod.flowave.domain.exceptions.RecoverablePermissionException
import com.crackmod.flowave.domain.model.*
import com.crackmod.flowave.domain.repository.MusicRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import retrofit2.Response
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackDao: TrackDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    private val playlistDao: PlaylistDao,
    private val mediaScanner: MediaScanner,
    private val lrcLibApiService: LrcLibApiService
) : MusicRepository {

    // --- Tracks (без изменений) ---
    override fun getAllTracks(): Flow<List<Track>> = trackDao.getAllTracks().map { it.map { e -> e.toDomain() } }
    override fun getTrackById(id: Long): Flow<Track?> = trackDao.getTrackById(id).map { it?.toDomain() }
    override suspend fun getTracksByIds(ids: List<Long>): List<Track> = trackDao.getTracksByIds(ids).map { it.toDomain() }
    override fun getFavoriteTracks(): Flow<List<Track>> = trackDao.getFavoriteTracks().map { it.map { e -> e.toDomain() } }
    override fun getFavoriteTrackCount(): Flow<Int> = trackDao.getFavoriteTrackCount()
    override fun searchTracks(query: String): Flow<List<Track>> = trackDao.searchTracks(query).map { it.map { e -> e.toDomain() } }
    override fun getRecentlyAddedTracks(limit: Int): Flow<List<Track>> = trackDao.getRecentlyAddedTracks(limit).map { it.map { e -> e.toDomain() } }
    override fun getMostPlayedTracks(limit: Int): Flow<List<Track>> = trackDao.getMostPlayedTracks(limit).map { it.map { e -> e.toDomain() } }
    override fun getTracksByAlbum(albumId: Long): Flow<List<Track>> = trackDao.getTracksByAlbum(albumId).map { it.map { e -> e.toDomain() } }
    override fun getTracksByArtist(artistId: Long): Flow<List<Track>> = trackDao.getTracksByArtist(artistId).map { it.map { e -> e.toDomain() } }
    override suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean) = trackDao.updateFavoriteStatus(id, isFavorite)
    override suspend fun incrementPlayCount(id: Long, timestamp: Long) {
        trackDao.incrementPlayCount(id)
        trackDao.updateLastPlayed(id, timestamp)
    }
    override suspend fun updateTrackAvailability(id: Long, isAvailable: Boolean) = trackDao.updateTrackAvailability(id, isAvailable)

    override suspend fun updateTrackMetadata(trackId: Long, newTitle: String, newArtist: String, newAlbum: String, newAlbumArtist: String, newYear: Int?, newGenre: String?, newTrackNumber: Int?) {
        withContext(Dispatchers.IO) {
            val trackEntity = trackDao.getTrackById(trackId).firstOrNull() ?: throw Exception("Трек не найден в базе данных")
            val file = File(trackEntity.path)

            if (!file.exists()) {
                throw Exception("Файл не найден по пути: ${trackEntity.path}")
            }

            try {
                val audioFile = AudioFileIO.read(file)
                val tag = audioFile.tagOrCreateAndSetDefault

                tag.setField(FieldKey.TITLE, newTitle)
                tag.setField(FieldKey.ARTIST, newArtist)
                tag.setField(FieldKey.ALBUM, newAlbum)
                tag.setField(FieldKey.ALBUM_ARTIST, newAlbumArtist)

                if (newYear != null && newYear > 0) tag.setField(FieldKey.YEAR, newYear.toString()) else tag.deleteField(FieldKey.YEAR)
                if (!newGenre.isNullOrBlank()) tag.setField(FieldKey.GENRE, newGenre) else tag.deleteField(FieldKey.GENRE)
                if (newTrackNumber != null && newTrackNumber > 0) tag.setField(FieldKey.TRACK, newTrackNumber.toString()) else tag.deleteField(FieldKey.TRACK)

                audioFile.commit()

                val updatedEntity = trackEntity.copy(
                    title = newTitle,
                    artist = newArtist,
                    album = newAlbum,
                    albumArtist = newAlbumArtist,
                    year = newYear,
                    genre = newGenre,
                    trackNumber = newTrackNumber
                )
                trackDao.updateTrack(updatedEntity)

                MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)

                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))

            } catch (e: Exception) {
                throw Exception("Не удалось записать теги: ${e.message}", e)
            }
        }
    }

    override suspend fun deleteTrack(trackId: Long) {
        withContext(Dispatchers.IO) {
            val trackEntity = trackDao.getTrackById(trackId).firstOrNull()
                ?: throw Exception("Трек с ID $trackId не найден в базе данных.")

            try {
                val uri = trackEntity.contentUri?.let { Uri.parse(it) }
                    ?: throw Exception("Невозможно получить URI для трека.")

                val deletedRows = context.contentResolver.delete(uri, null, null)

                if (deletedRows > 0) {
                    trackDao.deleteTrack(trackEntity)
                } else {
                    // Если файл уже удален, просто удаляем из БД
                    trackDao.deleteTrack(trackEntity)
                    throw Exception("Файл не был удален, но запись из базы данных очищена.")
                }
            } catch (e: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
                    throw RecoverablePermissionException(e.userAction.actionIntent, "Требуется разрешение на удаление файла.")
                } else {
                    throw Exception("Ошибка безопасности при удалении файла: ${e.message}")
                }
            } catch (e: Exception) {
                throw Exception("Ошибка при удалении трека: ${e.message}", e)
            }
        }
    }

    // --- Lyrics (без изменений) ---
    override suspend fun getLyricsForTrack(track: Track): Flow<Result<Lyrics>> = flow {
        if (track.hasLyrics) {
            val lyrics = Lyrics(
                lines = parseLyrics(track.plainLyrics, track.syncedLyrics),
                isInstrumental = track.areLyricsInstrumental
            )
            emit(Result.success(lyrics))
            return@flow
        }
        try {
            val cachedResponse = lrcLibApiService.getCachedLyrics(
                trackName = track.title, artistName = track.artist, albumName = track.album, duration = track.duration / 1000.0
            )
            if (cachedResponse.isSuccessful && cachedResponse.body() != null) {
                handleSuccessfulResponse(cachedResponse, track)
            } else if (cachedResponse.code() == 404) {
                val fullResponse = lrcLibApiService.getLyrics(
                    trackName = track.title, artistName = track.artist, albumName = track.album, duration = track.duration / 1000.0
                )
                if (fullResponse.isSuccessful && fullResponse.body() != null) {
                    handleSuccessfulResponse(fullResponse, track)
                } else {
                    handleNotFound(track)
                }
            } else {
                emit(Result.failure(Exception("Ошибка API (cached): ${cachedResponse.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun FlowCollector<Result<Lyrics>>.handleSuccessfulResponse(response: Response<com.crackmod.flowave.data.remote.dto.LyricsResponse>, track: Track) {
        val body = response.body()!!
        val lyrics = body.toDomain()
        val updatedTrackEntity = track.toEntity().copy(
            syncedLyrics = body.syncedLyrics, plainLyrics = body.plainLyrics, areLyricsInstrumental = body.instrumental
        )
        trackDao.updateTrack(updatedTrackEntity)
        emit(Result.success(lyrics))
    }

    private suspend fun FlowCollector<Result<Lyrics>>.handleNotFound(track: Track) {
        val updatedTrackEntity = track.toEntity().copy(areLyricsInstrumental = true)
        trackDao.updateTrack(updatedTrackEntity)
        emit(Result.failure(Exception("Текст не найден")))
    }

    // --- Albums (без изменений) ---
    override fun getAllAlbums(): Flow<List<Album>> = albumDao.getAllAlbums().map { it.map { e -> e.toDomain() } }
    override fun getAlbumById(id: Long): Flow<Album?> = albumDao.getAlbumById(id).map { it?.toDomain() }
    override fun searchAlbums(query: String): Flow<List<Album>> = albumDao.searchAlbums(query).map { it.map { e -> e.toDomain() } }
    override fun getAlbumsByArtist(artistName: String): Flow<List<Album>> = albumDao.getAlbumsByArtist(artistName).map { it.map { e -> e.toDomain() } }
    override fun getRecentlyAddedAlbums(limit: Int): Flow<List<Album>> = albumDao.getRecentlyAddedAlbums(limit).map { it.map { e -> e.toDomain() } }

    override suspend fun deleteAlbum(albumId: Long) {
        withContext(Dispatchers.IO) {
            val albumEntity = albumDao.getAlbumById(albumId).firstOrNull()
                ?: throw Exception("Альбом с ID $albumId не найден.")

            val tracksToDelete = trackDao.getTracksByAlbum(albumId).firstOrNull() ?: emptyList()

            tracksToDelete.forEach { trackEntity ->
                deleteTrack(trackEntity.id)
            }

            albumDao.deleteAlbum(albumEntity)
        }
    }

    // --- Artists (без изменений) ---
    override fun getAllArtists(): Flow<List<Artist>> = artistDao.getAllArtists().map { it.map { e -> e.toDomain() } }
    override fun getArtistById(id: Long): Flow<Artist?> = artistDao.getArtistById(id).map { it?.toDomain() }
    override fun searchArtists(query: String): Flow<List<Artist>> = artistDao.searchArtists(query).map { it.map { e -> e.toDomain() } }

    // --- Playlists ---
    // --- ГЛАВНОЕ ИЗМЕНЕНИЕ ЗДЕСЬ ---
    override fun getAllPlaylists(): Flow<List<Playlist>> =
        playlistDao.getAllPlaylists().flatMapLatest { entities ->
            if (entities.isEmpty()) {
                flowOf(emptyList())
            } else {
                val countFlows = entities.map { entity ->
                    playlistDao.getPlaylistTrackCount(entity.id)
                }
                combine(countFlows) { countsArray ->
                    entities.mapIndexed { index, entity ->
                        entity.toDomain().copy(trackCount = countsArray[index])
                    }
                }
            }
        }

    override fun getPlaylistById(id: String): Flow<Playlist?> = playlistDao.getPlaylistById(id).map { it?.toDomain() }
    override suspend fun createPlaylist(name: String, description: String?): Playlist {
        val playlist = Playlist(id = UUID.randomUUID().toString(), name = name, description = description, dateCreated = System.currentTimeMillis(), dateModified = System.currentTimeMillis(), trackCount = 0, isSystem = false)
        playlistDao.insertPlaylist(playlist.toEntity())
        return playlist
    }
    override suspend fun updatePlaylist(playlist: Playlist) = playlistDao.updatePlaylist(playlist.toEntity())
    override suspend fun deletePlaylist(playlistId: String) {
        playlistDao.getPlaylistById(playlistId).firstOrNull()?.let { entity ->
            playlistDao.deletePlaylist(entity)
        }
    }
    override suspend fun addTrackToPlaylist(playlistId: String, trackId: Long) {
        val lastPosition = playlistDao.getLastPosition(playlistId) ?: -1
        val playlistTrack = com.crackmod.flowave.data.local.entity.PlaylistTrackEntity(id = UUID.randomUUID().toString(), playlistId = playlistId, trackId = trackId, position = lastPosition + 1, dateAdded = System.currentTimeMillis())
        playlistDao.insertPlaylistTrack(playlistTrack)
    }
    override suspend fun removeTrackFromPlaylist(playlistId: String, trackId: Long) = playlistDao.removeTrackFromPlaylist(playlistId, trackId)
    override fun getPlaylistTracks(playlistId: String): Flow<List<Track>> = playlistDao.getPlaylistTracks(playlistId).map { it.map { e -> e.toDomain() } }

    // --- Media Scanning & Stats (без изменений) ---
    override suspend fun scanMediaLibrary(scanFolders: List<Uri>?): Result<Int> {
        return try {
            trackDao.deleteAllTracks()
            albumDao.deleteAllAlbums()
            artistDao.deleteAllArtists()
            val tracks = mediaScanner.scanAudioFiles(scanFolders)
            val albums = mediaScanner.scanAlbums()
            val artists = mediaScanner.scanArtists()
            trackDao.insertTracks(tracks)
            albumDao.insertAlbums(albums)
            artistDao.insertArtists(artists)
            Result.success(tracks.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun getTrackCount(): Int = trackDao.getTrackCount()
    override suspend fun getAlbumCount(): Int = albumDao.getAlbumCount()
    override suspend fun getArtistCount(): Int = artistDao.getArtistCount()

    private fun parseLyrics(plain: String?, synced: String?): List<LyricsLine> {
        if (!synced.isNullOrBlank()) {
            val parsedLines = parseSyncedLyrics(synced)
            if (parsedLines.isNotEmpty()) return parsedLines
        }
        if (!plain.isNullOrBlank()) {
            return plain.lines().map { LyricsLine(0, it) }
        }
        return emptyList()
    }

    private fun parseSyncedLyrics(syncedLyrics: String): List<LyricsLine> {
        val lines = mutableListOf<LyricsLine>()
        val pattern = java.util.regex.Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)")
        syncedLyrics.lines().forEach { line ->
            val matcher = pattern.matcher(line)
            if (matcher.matches()) {
                try {
                    val minutes = matcher.group(1)!!.toLong()
                    val seconds = matcher.group(2)!!.toLong()
                    val millis = matcher.group(3)!!.toLong()
                    val content = matcher.group(4)!!.trim()
                    val totalMillis = (minutes * 60 + seconds) * 1000 + if (millis.toString().length == 2) millis * 10 else millis
                    lines.add(LyricsLine(totalMillis, content))
                } catch (e: Exception) {
                    println("Could not parse lyric line: $line")
                }
            }
        }
        return lines.sortedBy { it.startTimeMs }
    }
}