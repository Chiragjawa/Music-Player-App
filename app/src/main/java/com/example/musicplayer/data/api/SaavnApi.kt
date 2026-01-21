package com.example.musicplayer.data.api

import com.example.musicplayer.data.model.*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SaavnApi {

    /* ---------------- SEARCH ---------------- */

    @GET("search/songs")
    suspend fun searchSongs(
        @Query("query") query: String,
        @Query("limit") limit: Int = 20
    ): SearchResponse

    @GET("search/albums")
    suspend fun searchAlbums(
        @Query("query") query: String,
        @Query("limit") limit: Int = 20
    ): AlbumSearchResponse

    @GET("search/playlists")
    suspend fun searchPlaylists(
        @Query("query") query: String,
        @Query("limit") limit: Int = 20
    ): PlaylistSearchResponse

    /* ---------------- SONG ---------------- */

    @GET("songs/{id}")
    suspend fun getSongById(
        @Path("id") id: String
    ): SongDetailResponse

    /* ---------------- ARTIST ---------------- */

    @GET("artists/{id}")
    suspend fun getArtist(
        @Path("id") id: String
    ): ArtistResponse

    @GET("artists/{id}/songs")
    suspend fun getArtistSongs(
        @Path("id") id: String
    ): SongListResponse

    @GET("artists/{id}/albums")
    suspend fun getArtistAlbums(
        @Path("id") id: String
    ): AlbumListResponse

    /* ---------------- ALBUM / PLAYLIST ---------------- */

    @GET("albums")
    suspend fun getAlbum(
        @Query("id") id: String
    ): AlbumDetailResponse

    @GET("playlists")
    suspend fun getPlaylist(
        @Query("id") id: String
    ): PlaylistDetailResponse
}
