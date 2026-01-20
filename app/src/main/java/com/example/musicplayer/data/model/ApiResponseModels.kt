package com.example.musicplayer.data.model

data class SearchResponse(
    val success: Boolean,
    val data: SearchData
)

data class SearchData(
    val results: List<ApiSong>
)

data class ApiSong(
    val id: String,
    val name: String,
    val duration: Int,
    val image: List<Image>,
    val downloadUrl: List<DownloadUrl>,
    val artists: Artists
)

data class Image(
    val quality: String,
    val url: String
)

data class DownloadUrl(
    val quality: String,
    val url: String
)

data class Artists(
    val primary: List<PrimaryArtist>
)

data class PrimaryArtist(
    val id: String,
    val name: String
)
