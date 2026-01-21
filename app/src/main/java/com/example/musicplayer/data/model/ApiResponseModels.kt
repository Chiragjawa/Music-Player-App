package com.example.musicplayer.data.model

import com.google.gson.annotations.SerializedName

/* ---------- COMMON WRAPPERS ---------- */

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: T
)

/* ---------- SEARCH RESPONSES ---------- */

data class SearchResultWrapper<T>(
    @SerializedName("total") val total: Int?,
    @SerializedName("start") val start: Int?,
    @SerializedName("results") val results: List<T>
)

/* ---------- SONG DETAIL RESPONSE ---------- */

typealias SongDetailResponse = ApiResponse<List<ApiSong>>

/* ---------- SEARCH SONG RESPONSE ---------- */

typealias SearchSongsResponse = ApiResponse<SearchResultWrapper<ApiSong>>

/* ---------- SEARCH ALBUM RESPONSE ---------- */

typealias SearchAlbumsResponse = ApiResponse<SearchResultWrapper<ApiAlbum>>

/* ---------- SEARCH PLAYLIST RESPONSE ---------- */

typealias SearchPlaylistsResponse = ApiResponse<SearchResultWrapper<ApiPlaylist>>
