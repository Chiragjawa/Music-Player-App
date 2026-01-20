package com.example.musicplayer.data.api

import com.example.musicplayer.data.model.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SaavnApi {

    @GET("search/songs")
    suspend fun searchSongs(
        @Query("query") query: String,
        @Query("limit") limit: Int = 20
    ): SearchResponse
}
