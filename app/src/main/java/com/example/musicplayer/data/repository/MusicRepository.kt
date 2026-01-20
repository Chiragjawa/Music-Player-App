package com.example.musicplayer.data.repository

import com.example.musicplayer.data.api.SaavnApi
import com.example.musicplayer.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MusicRepository @Inject constructor(
    private val api: SaavnApi
) {

    suspend fun searchSongs(query: String): Result<List<Song>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.searchSongs(query)
            }

            val songs = response.data.results.map { apiSong ->
                Song(
                    id = apiSong.id,
                    name = apiSong.name,
                    duration = apiSong.duration,
                    imageUrl = apiSong.image.lastOrNull()?.url ?: "",
                    streamUrl = apiSong.downloadUrl.lastOrNull()?.url ?: "",
                    artists = apiSong.artists.primary.joinToString { it.name }
                )
            }

            Result.success(songs)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
