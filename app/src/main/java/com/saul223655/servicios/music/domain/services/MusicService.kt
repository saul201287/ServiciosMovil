package com.saul223655.servicios.music.domain.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.saul223655.servicios.MainActivity
import com.saul223655.servicios.R
import com.saul223655.servicios.core.database.AppDatabase

class MusicService : Service() {
    private val binder = MusicBinder()
    private lateinit var player: Player
    var isPlaying = false
    private lateinit var db: AppDatabase
    private var isDefaultAudioPrepared = false

    companion object {
        private const val TAG = "MusicService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "MusicServiceChannel"
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Inicializando ExoPlayer y Room")
        try {
            player = ExoPlayer.Builder(this).build()
            val defaultMediaItem = MediaItem.fromUri("android.resource://${packageName}/${R.raw.audio_sample}")
            player.setMediaItem(defaultMediaItem)
            player.prepare()
            isDefaultAudioPrepared = true
            Log.d(TAG, "onCreate: ExoPlayer creado exitosamente con R.raw.audio_sample")
            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        Log.d(TAG, "ExoPlayer: Reproducción completada, llamando a stop()")
                        stop()
                    }
                }
            })
            db = AppDatabase.getDatabase(this)
            Log.d(TAG, "onCreate: Base de datos Room inicializada")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Error al inicializar servicio: ${e.message}")
        }
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind: Servicio vinculado")
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: Intent recibido - Acción: ${intent?.getStringExtra("ACTION")}")
        val action = intent?.getStringExtra("ACTION")
        val title = intent?.getStringExtra("TITLE") ?: "Unknown Song"
        val path = intent?.getStringExtra("PATH")
        Log.d(TAG, "onStartCommand: Título: $title, Path: $path")

        when (action) {
            "PLAY" -> play(title, path)
            "PAUSE" -> pause()
            "STOP" -> stop()
        }
        return START_STICKY
    }

    @SuppressLint("ForegroundServiceType")
    private fun play(title: String, path: String?) {
        Log.d(TAG, "play: Reproduciendo $title desde $path")
        if (!player.isPlaying) {
            try {
                if (path != null) {
                    Log.d(TAG, "play: Configurando ExoPlayer con path: $path")
                    player.stop()
                    player.clearMediaItems()
                    val mediaItem = MediaItem.fromUri(path)
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.play()
                    isPlaying = true
                    isDefaultAudioPrepared = false
                    startForeground(NOTIFICATION_ID, createNotification("Reproduciendo: $title"))
                    Log.d(TAG, "play: Reproducción iniciada desde almacenamiento")
                } else if (isDefaultAudioPrepared) {
                    Log.d(TAG, "play: Reproduciendo audio por defecto (R.raw.audio_sample)")
                    player.play()
                    isPlaying = true
                    startForeground(NOTIFICATION_ID, createNotification("Reproduciendo: $title"))
                    Log.d(TAG, "play: Reproducción iniciada desde R.raw.audio_sample")
                } else {
                    Log.w(TAG, "play: Audio por defecto no preparado, intentando recrear")
                    player.clearMediaItems()
                    val defaultMediaItem = MediaItem.fromUri("android.resource://${packageName}/${R.raw.audio_sample}")
                    player.setMediaItem(defaultMediaItem)
                    player.prepare()
                    player.play()
                    isPlaying = true
                    isDefaultAudioPrepared = true
                    startForeground(NOTIFICATION_ID, createNotification("Reproduciendo: $title"))
                    Log.d(TAG, "play: Reproducción iniciada desde R.raw.audio_sample (recreado)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "play: Error al reproducir: ${e.message}")
                isPlaying = false
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
        }
    }

    @SuppressLint("ForegroundServiceType")
    private fun pause() {
        if (player.isPlaying) {
            player.pause()
            isPlaying = false
            startForeground(NOTIFICATION_ID, createNotification("En pausa"))
        }
    }

    private fun stop() {
        if (player.isPlaying) {
            player.stop()
        }
        player.clearMediaItems()
        isPlaying = false
        isDefaultAudioPrepared = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }

    private fun createNotification(status: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Music Player")
            .setContentText(status)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(R.drawable.ic_play, "Play", createActionIntent("PLAY"))
            .addAction(R.drawable.ic_pause, "Pause", createActionIntent("PAUSE"))
            .addAction(R.drawable.ic_stop, "Stop", createActionIntent("STOP"))
            .build()
    }

    private fun createActionIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply { putExtra("ACTION", action) }
        return PendingIntent.getService(this, action.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}