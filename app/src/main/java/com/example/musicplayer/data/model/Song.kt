package com.example.musicplayer.data.model

data class Song(
    val id: String,
    val name: String,
    val artists: String,
    val duration: Int,
    val imageUrl: String,
    val streamUrl: String
)
