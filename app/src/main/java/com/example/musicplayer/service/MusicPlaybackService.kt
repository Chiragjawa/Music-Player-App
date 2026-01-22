package com.example.musicplayer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.transform.RoundedCornersTransformation
import com.example.musicplayer.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@UnstableApi
class MusicPlaybackService : MediaSessionService() {

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "music_playback_channel"
        private const val CHANNEL_NAME = "Music Playback"
    }

    interface PlaybackController {
        fun onPlayPause()
        fun onNext()
        fun onPrevious()
    }

    private var mediaSession: MediaSession? = null
    private var playbackController: PlaybackController? = null
    private val binder = MusicBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var currentAlbumArt: Bitmap? = null

    inner class MusicBinder : Binder() {
        fun getService(): MusicPlaybackService = this@MusicPlaybackService
    }

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    fun setMediaSession(session: MediaSession, controller: PlaybackController) {
        if (mediaSession != null) return // Already set

        mediaSession = session
        playbackController = controller

        // Add player listener to update notification
        session.player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateNotification(session)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        updateNotification(session)
                    }
                    Player.STATE_IDLE, Player.STATE_ENDED -> {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    }
                }
            }

            override fun onMediaMetadataChanged(mediaMetadata: androidx.media3.common.MediaMetadata) {
                // Load new album art when song changes
                val artworkUri = mediaMetadata.artworkUri
                if (artworkUri != null) {
                    loadAlbumArt(artworkUri.toString()) { bitmap ->
                        currentAlbumArt = bitmap
                        updateNotification(session)
                    }
                } else {
                    currentAlbumArt = null
                    updateNotification(session)
                }
            }
        })
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music playback controls"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(mediaSession: MediaSession) {
        val player = mediaSession.player
        if (player.isPlaying || player.playbackState == Player.STATE_READY) {
            startForeground(NOTIFICATION_ID, createNotification(mediaSession))
        } else {
            stopForeground(STOP_FOREGROUND_DETACH)
        }
    }

    private fun loadAlbumArt(url: String, onLoaded: (Bitmap?) -> Unit) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                val imageLoader = ImageLoader.Builder(this@MusicPlaybackService)
                    .crossfade(true)
                    .build()

                val request = ImageRequest.Builder(this@MusicPlaybackService)
                    .data(url)
                    .size(512, 512)
                    .transformations(RoundedCornersTransformation(16f))
                    .build()

                val result = imageLoader.execute(request)
                if (result is SuccessResult) {
                    val bitmap = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                    launch(Dispatchers.Main) {
                        onLoaded(bitmap)
                    }
                } else {
                    launch(Dispatchers.Main) {
                        onLoaded(null)
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    onLoaded(null)
                }
            }
        }
    }

    private fun createNotification(mediaSession: MediaSession): Notification {
        val player = mediaSession.player

        // Create intent to launch the app
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = player.mediaMetadata.title?.toString() ?: "Music Player"
        val artist = player.mediaMetadata.artist?.toString() ?: "Unknown Artist"

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentIntent(pendingIntent)
            .setOngoing(player.isPlaying)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionCompatToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .addAction(
                R.drawable.ic_skip_previous,
                "Previous",
                createMediaActionPendingIntent("previous")
            )
            .addAction(
                if (player.isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                if (player.isPlaying) "Pause" else "Play",
                createMediaActionPendingIntent("play_pause")
            )
            .addAction(
                R.drawable.ic_skip_next,
                "Next",
                createMediaActionPendingIntent("next")
            )

        // Add album art if available
        currentAlbumArt?.let {
            notificationBuilder.setLargeIcon(it)
        }

        return notificationBuilder.build()
    }

    private fun createMediaActionPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicPlaybackService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "play_pause" -> playbackController?.onPlayPause()
            "next" -> playbackController?.onNext()
            "previous" -> playbackController?.onPrevious()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        // Don't release the player - it's owned by the ViewModel
        // Just release the MediaSession reference
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}
