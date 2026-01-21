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
            val cleanQuery = query.trim()
            if (cleanQuery.isEmpty()) {
                return Result.success(emptyList())
            }

            // 1️⃣ Search API (lightweight)
            val searchResponse = withContext(Dispatchers.IO) {
                api.searchSongs(cleanQuery, limit = 20)
            }

            if (!searchResponse.success || searchResponse.data.results.isEmpty()) {
                // IMPORTANT: empty list → UI handles "No songs found"
                return Result.success(emptyList())
            }

            val firstWord = cleanQuery.lowercase().split(" ").first()

            val resolvedSongs = mutableListOf<Song>()

            // 2️⃣ Resolve each song via /songs/{id} for stable audio
            for (apiSong in searchResponse.data.results) {

                // FIRST WORD RULE (your requirement)
                val nameMatch = apiSong.name.lowercase().startsWith(firstWord)
                val artistMatch = apiSong.artists.primary.any {
                    it.name.lowercase().startsWith(firstWord)
                }

                if (!nameMatch && !artistMatch) continue

                try {
                    val detailResponse = api.getSongById(apiSong.id)
                    if (!detailResponse.success || detailResponse.data.isEmpty()) continue

                    val songDetail = detailResponse.data.first()

                    val streamUrl = getPreferredQualityUrl(songDetail.downloadUrl)
                    if (streamUrl.isBlank()) continue

                    resolvedSongs.add(
                        Song(
                            id = songDetail.id,
                            name = songDetail.name,
                            duration = songDetail.duration,
                            imageUrl = getHighestQualityImage(songDetail.image),
                            streamUrl = streamUrl,
                            artists = formatArtists(songDetail.artists.primary)
                        )
                    )
                } catch (_: Exception) {
                    // Skip broken song safely
                }
            }

            // 3️⃣ Final result
            Result.success(resolvedSongs)

        } catch (e: IOException) {
            Result.failure(Exception("Network error. Please check your connection."))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Unknown error"))
        }
    }
    suspend fun searchArtistWithSongs(query: String): Result<Pair<String, List<Song>>> {
        return try {
            val artistSearch = api.searchSongs(query, limit = 5)

            if (!artistSearch.success || artistSearch.data.results.isEmpty()) {
                return Result.failure(Exception("Artist not found"))
            }

            val artist = artistSearch.data.results
                .flatMap { it.artists.primary }
                .firstOrNull { it.name.lowercase().contains(query.lowercase()) }
                ?: return Result.failure(Exception("Artist not found"))

            val artistSongsResponse = api.getArtistSongs(artist.id)

            if (!artistSongsResponse.success || artistSongsResponse.data.isEmpty()) {
                return Result.failure(Exception("No songs found"))
            }

            val songs = artistSongsResponse.data.mapNotNull { apiSong ->
                val stream = getPreferredQualityUrl(apiSong.downloadUrl)
                if (stream.isBlank()) return@mapNotNull null

                Song(
                    id = apiSong.id,
                    name = apiSong.name,
                    duration = apiSong.duration,
                    imageUrl = getHighestQualityImage(apiSong.image),
                    streamUrl = stream,
                    artists = formatArtists(apiSong.artists.primary)
                )
            }

            Result.success(artist.name to songs)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /* ---------------- HELPERS ---------------- */

    private fun getHighestQualityImage(
        images: List<com.example.musicplayer.data.model.Image>
    ): String {
        return images.find { it.quality == "500x500" }?.url
            ?: images.find { it.quality == "150x150" }?.url
            ?: images.lastOrNull()?.url
            ?: ""
    }

    private fun getPreferredQualityUrl(
        downloadUrls: List<com.example.musicplayer.data.model.DownloadUrl>
    ): String {
        // 160kbps FIRST → most stable streaming
        return downloadUrls.find { it.quality == "160kbps" }?.url
            ?: downloadUrls.find { it.quality == "320kbps" }?.url
            ?: downloadUrls.find { it.quality == "96kbps" }?.url
            ?: ""
    }

    private fun formatArtists(
        artists: List<com.example.musicplayer.data.model.PrimaryArtist>
    ): String {
        return artists.joinToString(", ") { it.name }
    }
}
