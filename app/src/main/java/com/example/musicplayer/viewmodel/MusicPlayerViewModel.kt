package com.example.musicplayer.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.musicplayer.audio.AudioFocusManager
import com.example.musicplayer.data.local.QueueDataStore
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.service.MusicPlaybackService
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
    application: Application,
    private val queueDataStore: QueueDataStore
) : AndroidViewModel(application), MusicPlaybackService.PlaybackController {

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

    private var musicService: MusicPlaybackService? = null

    private var serviceBound = false

    private lateinit var audioFocusManager: AudioFocusManager

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlaybackService.MusicBinder
            musicService = binder.getService()
            serviceBound = true

            // Pass the MediaSession and controller to the service
            mediaSession?.let { session ->
                musicService?.setMediaSession(session, this@MusicPlayerViewModel)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBound = false
            musicService = null
        }
    }

    // Implement PlaybackController interface for notification controls
    override fun onPlayPause() {
        playPause()
    }

    override fun onNext() {
        playNext()
    }

    override fun onPrevious() {
        playPrevious()
    }

    init {
        mediaSession = MediaSession.Builder(application, player).build()

        // Initialize AudioFocusManager
        audioFocusManager = AudioFocusManager(
            context = application,
            onFocusLost = {
                // Pause on focus loss
                player.pause()
            },
            onFocusGained = {
                // Can resume if needed
            },
            onDuck = {
                // Lower volume
                player.volume = 0.3f
            },
            onUnduck = {
                // Restore volume
                player.volume = 1.0f
            }
        )

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
        loadPersistedQueue()
        bindToService()
    }

    private fun bindToService() {
        val intent = Intent(getApplication(), MusicPlaybackService::class.java)
        getApplication<Application>().startService(intent)
        getApplication<Application>().bindService(
            intent,
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    private fun loadPersistedQueue() {
        viewModelScope.launch {
            // Only load once on init, don't continuously collect
            queueDataStore.getQueue().collect { (queue, index) ->
                // Only load if we don't have a queue yet
                if (_playerState.value.queue.isEmpty() && queue.isNotEmpty() && index >= 0 && index < queue.size) {
                    _playerState.value = _playerState.value.copy(
                        queue = queue,
                        currentIndex = index,
                        currentSong = queue.getOrNull(index)
                    )
                }
            }
        }
    }

    private fun persistQueue() {
        viewModelScope.launch {
            val state = _playerState.value
            if (state.queue.isNotEmpty()) {
                queueDataStore.saveQueue(state.queue, state.currentIndex)
            }
        }
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
                delay(50)
            }
        }
    }

    /**
     * ✅ FIXED: Now properly uses the queue parameter
     * When called from search results or artist screen, replaces entire queue
     */
    fun playSong(song: Song, queue: List<Song> = listOf(song)) {
        player.stop()
        player.clearMediaItems()

        // Find the song's position in the provided queue
        val index = queue.indexOfFirst { it.id == song.id }

        // Replace entire queue with new one
        _playerState.value = _playerState.value.copy(
            currentSong = song,
            queue = queue,
            currentIndex = if (index >= 0) index else 0
        )

        // Create media item with metadata for notification
        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(song.name)
            .setArtist(song.artists)
            .setArtworkUri(android.net.Uri.parse(song.imageUrl))
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(song.streamUrl)
            .setMediaId(song.id)
            .setMediaMetadata(mediaMetadata)
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        // Persist queue
        persistQueue()
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

            // Update state first
            _playerState.value = state.copy(
                currentSong = nextSong,
                currentIndex = state.currentIndex + 1
            )

            // Then update player
            player.stop()
            player.clearMediaItems()

            // Create media item with metadata for notification
            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(nextSong.name)
                .setArtist(nextSong.artists)
                .setArtworkUri(android.net.Uri.parse(nextSong.imageUrl))
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(nextSong.streamUrl)
                .setMediaId(nextSong.id)
                .setMediaMetadata(mediaMetadata)
                .build()

            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()

            // Persist queue with new index
            persistQueue()
        }
    }

    fun playPrevious() {
        val state = _playerState.value

        if (state.currentIndex > 0) {
            val previousSong = state.queue[state.currentIndex - 1]

            _playerState.value = state.copy(
                currentSong = previousSong,
                currentIndex = state.currentIndex - 1
            )

            player.stop()
            player.clearMediaItems()

            // Create media item with metadata for notification
            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(previousSong.name)
                .setArtist(previousSong.artists)
                .setArtworkUri(android.net.Uri.parse(previousSong.imageUrl))
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(previousSong.streamUrl)
                .setMediaId(previousSong.id)
                .setMediaMetadata(mediaMetadata)
                .build()

            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()

            // Persist queue with new index
            persistQueue()
        }
    }

    /**
     * Add a song to the end of current queue (optional feature)
     */
    fun addToQueue(song: Song) {
        val currentQueue = _playerState.value.queue.toMutableList()

        if (!currentQueue.any { it.id == song.id }) {
            currentQueue.add(song)
            _playerState.value = _playerState.value.copy(queue = currentQueue)
            persistQueue()
        }
    }

    /**
     * Remove a song from queue
     */
    fun removeFromQueue(song: Song) {
        val currentQueue = _playerState.value.queue.toMutableList()
        val currentIndex = _playerState.value.currentIndex

        val removedIndex = currentQueue.indexOfFirst { it.id == song.id }
        if (removedIndex == -1) return

        currentQueue.removeAt(removedIndex)

        // Adjust current index if necessary
        val newIndex = when {
            removedIndex < currentIndex -> currentIndex - 1
            removedIndex == currentIndex -> -1 // Current song removed
            else -> currentIndex
        }

        _playerState.value = _playerState.value.copy(
            queue = currentQueue,
            currentIndex = newIndex
        )

        // If current song was removed, stop playback
        if (newIndex == -1) {
            player.stop()
            _playerState.value = _playerState.value.copy(
                currentSong = null,
                isPlaying = false
            )
        }

        persistQueue()
    }

    /**
     * Reorder queue by moving an item from one position to another
     */
    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        val currentQueue = _playerState.value.queue.toMutableList()
        val currentIndex = _playerState.value.currentIndex

        if (fromIndex < 0 || fromIndex >= currentQueue.size ||
            toIndex < 0 || toIndex >= currentQueue.size) {
            return
        }

        // Move the item
        val item = currentQueue.removeAt(fromIndex)
        currentQueue.add(toIndex, item)

        // Adjust currentIndex if needed
        val newIndex = when {
            currentIndex == fromIndex -> toIndex
            fromIndex < currentIndex && toIndex >= currentIndex ->
                currentIndex - 1
            fromIndex > currentIndex && toIndex <= currentIndex ->
                currentIndex + 1
            else -> currentIndex
        }

        _playerState.value = _playerState.value.copy(
            queue = currentQueue,
            currentIndex = newIndex
        )

        persistQueue()
    }

    override fun onCleared() {
        super.onCleared()

        // Unbind from service
        if (serviceBound) {
            getApplication<Application>().unbindService(serviceConnection)
            serviceBound = false
        }

        mediaSession?.release()
        mediaSession = null
        player.release()
    }
}