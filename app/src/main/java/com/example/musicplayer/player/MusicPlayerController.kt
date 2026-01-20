package com.example.musicplayer.player

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicPlayerController @Inject constructor(
    @ApplicationContext context: Context
) {
    val player = ExoPlayer.Builder(context).build()

    fun play(url: String) {
        player.setMediaItem(androidx.media3.common.MediaItem.fromUri(url))
        player.prepare()
        player.play()
    }
}
