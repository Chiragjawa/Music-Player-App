package com.example.musicplayer.data.model

import com.google.gson.annotations.SerializedName

/* ---------- SONG ---------- */

data class ApiSong(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("duration") val duration: Int?,
    @SerializedName("language") val language: String?,
    @SerializedName("playCount") val playCount: Long?,
    @SerializedName("artists") val artists: ApiArtists,
    @SerializedName("image") val image: List<ApiImage>,
    @SerializedName("downloadUrl") val downloadUrl: List<ApiDownload>
)

/* ---------- ARTISTS ---------- */

data class ApiArtists(
    @SerializedName("primary") val primary: List<ApiArtist>
)

data class ApiArtist(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String
)

/* ---------- ALBUM ---------- */

data class ApiAlbum(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("year") val year: String?,
    @SerializedName("language") val language: String?,
    @SerializedName("songCount") val songCount: Int?,
    @SerializedName("image") val image: List<ApiImage>,
    @SerializedName("artists") val artists: ApiArtists?
)

/* ---------- PLAYLIST ---------- */

data class ApiPlaylist(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("songCount") val songCount: Int?,
    @SerializedName("image") val image: List<ApiImage>
)

/* ---------- ARTIST DETAILS ---------- */

data class ArtistDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ApiArtistDetail
)

data class ApiArtistDetail(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: List<ApiImage>
)

/* ---------- ARTIST SONGS / ALBUMS ---------- */

typealias ArtistSongsResponse =
        ApiResponse<SearchResultWrapper<ApiSong>>

typealias ArtistAlbumsResponse =
        ApiResponse<SearchResultWrapper<ApiAlbum>>

/* ---------- SHARED ---------- */

data class ApiImage(
    @SerializedName("quality") val quality: String,
    @SerializedName("url") val url: String
)

data class ApiDownload(
    @SerializedName("quality") val quality: String,
    @SerializedName("url") val url: String
)
