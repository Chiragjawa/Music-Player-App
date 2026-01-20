package com.example.musicplayer.data.repository

import com.example.musicplayer.data.api.SaavnApi
import com.example.musicplayer.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class MusicRepository @Inject constructor(
    private val api: SaavnApi
) {

    suspend fun searchSongs(query: String): Result<List<Song>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.searchSongs(query, limit = 20)
            }

            // Check if API call was successful
            if (!response.success) {
                return Result.failure(Exception("API returned unsuccessful response"))
            }

            // Transform API songs to domain songs
            val songs = response.data.results.map { apiSong ->
                Song(
                    id = apiSong.id,
                    name = apiSong.name,
                    duration = apiSong.duration,
                    imageUrl = getHighestQualityImage(apiSong.image),
                    streamUrl = getPreferredQualityUrl(apiSong.downloadUrl),
                    artists = formatArtists(apiSong.artists.primary)
                )
            }

            // Split query into possible song + artist parts
            val queryParts = query.lowercase().split(" ")

            // Advanced filtering
            val filteredSongs = songs.filter { song ->
                val songName = song.name.lowercase()
                val artistName = song.artists.lowercase()

                // Must contain ALL query parts either in song name or artist name
                queryParts.all { part ->
                    songName.contains(part) || artistName.contains(part)
                }
            }

            val cleanQuery = query.trim().lowercase()

// Split query into words
            val firstWord = cleanQuery.split(" ").firstOrNull() ?: ""

// Filter only songs whose NAME starts with the first word
            val validSongs = songs.filter { song ->
                song.streamUrl.isNotBlank() &&
                        (
                                song.name.lowercase().startsWith(firstWord) ||
                                        song.artists.lowercase().startsWith(firstWord)
                                )
            }

            return Result.success(validSongs)



        } catch (e: IOException) {
            Result.failure(Exception("Network error. Please check your connection."))
        } catch (e: Exception) {
            Result.failure(Exception("An error occurred: ${e.localizedMessage ?: "Unknown error"}"))
        }
    }

    /**
     * Gets the highest quality image URL available
     * Priority: 500x500 > 150x150 > 50x50
     */
    private fun getHighestQualityImage(images: List<com.example.musicplayer.data.model.Image>): String {
        return images.find { it.quality == "500x500" }?.url
            ?: images.find { it.quality == "150x150" }?.url
            ?: images.lastOrNull()?.url
            ?: ""
    }

    /**
     * Gets preferred audio quality URL
     * Priority: 320kbps > 160kbps > 96kbps > others
     */
    private fun getPreferredQualityUrl(downloadUrls: List<com.example.musicplayer.data.model.DownloadUrl>): String {
        return downloadUrls.find { it.quality == "320kbps" }?.url
            ?: downloadUrls.find { it.quality == "160kbps" }?.url
            ?: downloadUrls.find { it.quality == "96kbps" }?.url
            ?: downloadUrls.lastOrNull()?.url
            ?: ""
    }

    /**
     * Formats artist names into a comma-separated string
     */
    private fun formatArtists(artists: List<com.example.musicplayer.data.model.PrimaryArtist>): String {
        return artists.joinToString(", ") { it.name }
    }
}