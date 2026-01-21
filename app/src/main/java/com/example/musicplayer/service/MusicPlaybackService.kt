package com.example.musicplayer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.musicplayer.R

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
                if (isPlaying) {
                    startForeground(NOTIFICATION_ID, createNotification(session))
                } else {
                    stopForeground(STOP_FOREGROUND_DETACH)
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        startForeground(NOTIFICATION_ID, createNotification(session))
                    }
                    Player.STATE_IDLE, Player.STATE_ENDED -> {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    }
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

        // Build notification
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(player.mediaMetadata.title ?: "Music Player")
            .setContentText(player.mediaMetadata.artist ?: "Unknown Artist")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(player.isPlaying)
            .setOnlyAlertOnce(true)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionCompatToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Previous",
                createMediaActionPendingIntent("previous")
            )
            .addAction(
                if (player.isPlaying) R.drawable.ic_launcher_foreground else R.drawable.ic_launcher_foreground,
                if (player.isPlaying) "Pause" else "Play",
                createMediaActionPendingIntent("play_pause")
            )
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Next",
                createMediaActionPendingIntent("next")
            )
            .build()
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
