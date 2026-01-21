package com.example.musicplayer.data.repository

import com.example.musicplayer.data.api.SaavnApi
import com.example.musicplayer.data.model.Song
import javax.inject.Inject

class MusicRepository @Inject constructor(
    private val api: SaavnApi
) {

    suspend fun searchSongs(query: String): List<Song> {
        val response = api.searchSongs(query, limit = 30)

        return response.data.results.mapNotNull { apiSong ->
            val streamUrl = apiSong.downloadUrl
                .firstOrNull { it.quality == "160kbps" }
                ?.url ?: return@mapNotNull null

            Song(
                id = apiSong.id,
                name = apiSong.name,
                artists = apiSong.artists.primary.joinToString(", ") { it.name },
                duration = apiSong.duration ?: 0,
                imageUrl = apiSong.image.lastOrNull()?.url.orEmpty(),
                streamUrl = streamUrl
            )
        }
    }

    suspend fun getSongsByArtistName(artistName: String): List<Song> {
        val cleanArtist = artistName.trim().lowercase()
        val response = api.searchSongs(artistName, limit = 50)

        return response.data.results.mapNotNull { apiSong ->
            val match = apiSong.artists.primary.any {
                val apiName = it.name.lowercase()
                apiName.contains(cleanArtist) || cleanArtist.contains(apiName)
            }

            if (!match) return@mapNotNull null

            val streamUrl = apiSong.downloadUrl
                .firstOrNull { it.quality == "160kbps" }
                ?.url ?: return@mapNotNull null

            Song(
                id = apiSong.id,
                name = apiSong.name,
                artists = apiSong.artists.primary.joinToString(", ") { it.name },
                duration = apiSong.duration ?: 0,
                imageUrl = apiSong.image.lastOrNull()?.url.orEmpty(),
                streamUrl = streamUrl
            )
        }
    }
}
