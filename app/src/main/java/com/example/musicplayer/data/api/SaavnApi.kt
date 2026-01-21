package com.example.musicplayer.data.api

import com.example.musicplayer.data.model.*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SaavnApi {

    // SEARCH
    @GET("search/songs")
    suspend fun searchSongs(
        @Query("query") query: String,
        @Query("limit") limit: Int = 20
    ): SearchSongsResponse

    @GET("search/albums")
    suspend fun searchAlbums(
        @Query("query") query: String
    ): SearchAlbumsResponse

    @GET("search/playlists")
    suspend fun searchPlaylists(
        @Query("query") query: String
    ): SearchPlaylistsResponse


    // SONGS
    @GET("songs")
    suspend fun getSongById(
        @Query("id") id: String
    ): SongDetailResponse

    @GET("songs/{id}")
    suspend fun getSongByPath(
        @Path("id") id: String
    ): SongDetailResponse


    // ALBUMS
    @GET("albums")
    suspend fun getAlbumById(
        @Query("id") id: String
    ): ApiResponse<ApiAlbum>


    // PLAYLISTS
    @GET("playlists")
    suspend fun getPlaylistById(
        @Query("id") id: String
    ): ApiResponse<ApiPlaylist>


    // ARTISTS
    @GET("artists")
    suspend fun getArtistById(
        @Query("id") id: String
    ): ArtistDetailResponse

    @GET("artists/{id}/songs")
    suspend fun getArtistSongs(
        @Path("id") id: String
    ): ArtistSongsResponse

    @GET("artists/{id}/albums")
    suspend fun getArtistAlbums(
        @Path("id") id: String
    ): ArtistAlbumsResponse
}
