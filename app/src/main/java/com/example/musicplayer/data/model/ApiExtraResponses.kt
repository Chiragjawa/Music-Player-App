package com.example.musicplayer.data.model

/* ---------------- SONG DETAIL ---------------- */

data class SongDetailResponse(
    val success: Boolean,
    val data: List<ApiSong>
)

/* ---------------- ALBUM SEARCH ---------------- */

data class AlbumSearchResponse(
    val success: Boolean,
    val data: AlbumSearchData
)

data class AlbumSearchData(
    val results: List<Album>
)

/* ---------------- PLAYLIST SEARCH ---------------- */

data class PlaylistSearchResponse(
    val success: Boolean,
    val data: PlaylistSearchData
)

data class PlaylistSearchData(
    val results: List<Playlist>
)

/* ---------------- ALBUM / PLAYLIST DETAILS ---------------- */

data class AlbumDetailResponse(
    val success: Boolean,
    val data: Album
)

data class PlaylistDetailResponse(
    val success: Boolean,
    val data: Playlist
)

/* ---------------- BASE MODELS ---------------- */

data class Album(
    val id: String,
    val name: String,
    val image: List<Image>
)

data class Playlist(
    val id: String,
    val name: String,
    val image: List<Image>
)
/* ---------------- ARTIST DETAIL ---------------- */

data class ArtistResponse(
    val success: Boolean,
    val data: Artist
)

data class Artist(
    val id: String,
    val name: String,
    val image: List<Image>
)

/* ---------------- ARTIST SONGS ---------------- */

data class SongListResponse(
    val success: Boolean,
    val data: List<ApiSong>
)

/* ---------------- ARTIST ALBUMS ---------------- */

data class AlbumListResponse(
    val success: Boolean,
    val data: List<Album>
)
