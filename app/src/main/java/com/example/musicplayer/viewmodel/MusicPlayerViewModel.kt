package com.example.musicplayer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import com.example.musicplayer.data.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1
)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@HiltViewModel
class MusicPlayerViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)  // Note: AUDIO_CONTENT_TYPE_MUSIC
        .build()

    val player: ExoPlayer = ExoPlayer.Builder(application)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            true
        )
        .setHandleAudioBecomingNoisy(true)
        .setWakeMode(C.WAKE_MODE_LOCAL)
        .build()

    private var mediaSession: MediaSession? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val playerListener = object : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                _playerState.value = _playerState.value.copy(
                    duration = player.duration
                )
            }
        }
    }

    init {
        mediaSession = MediaSession.Builder(application, player).build()

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _playerState.value = _playerState.value.copy(
                        duration = player.duration
                    )
                }
            }
        })

        // ADD THE NEW LISTENER HERE
        player.addListener(object : Player.Listener {
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                _playerState.value = _playerState.value.copy(
                    currentPosition = player.currentPosition
                )
            }
        })

        startProgressUpdate()
    }


    private fun startProgressUpdate() {
        viewModelScope.launch {
            while (isActive) {
                if (player.isPlaying) {
                    _playerState.value = _playerState.value.copy(
                        currentPosition = player.currentPosition,
                        duration = player.duration
                    )
                }
                delay(50)   // smoother updates
            }
        }
    }


    fun playSong(song: Song, queue: List<Song> = listOf(song)) {

        player.stop()
        player.clearMediaItems()

        val currentQueue = _playerState.value.queue.toMutableList()

        if (!currentQueue.any { it.id == song.id }) {
            currentQueue.add(song)
        }

        val index = currentQueue.indexOfFirst { it.id == song.id }

        _playerState.value = _playerState.value.copy(
            currentSong = song,
            queue = currentQueue,
            currentIndex = index
        )

        val mediaItem = MediaItem.Builder()
            .setUri(song.streamUrl)
            .setMediaId(song.id)
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun playPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    fun playNext() {
        val state = _playerState.value

        if (state.currentIndex < state.queue.size - 1) {
            val nextSong = state.queue[state.currentIndex + 1]
            playSong(nextSong, state.queue)
        }
    }

    fun playPrevious() {
        val state = _playerState.value

        if (state.currentIndex > 0) {
            val previousSong = state.queue[state.currentIndex - 1]
            playSong(previousSong, state.queue)
        }
    }

    fun addToQueue(song: Song) {
        val currentQueue = _playerState.value.queue.toMutableList()

        if (!currentQueue.any { it.id == song.id }) {
            currentQueue.add(song)

            _playerState.value = _playerState.value.copy(queue = currentQueue)
        }
    }

    fun removeFromQueue(song: Song) {
        val currentQueue = _playerState.value.queue.toMutableList()

        currentQueue.removeAll { it.id == song.id }

        _playerState.value = _playerState.value.copy(queue = currentQueue)
    }

    override fun onCleared() {
        super.onCleared()

        player.removeListener(playerListener)

        mediaSession?.release()
        mediaSession = null

        player.release()
    }
}
