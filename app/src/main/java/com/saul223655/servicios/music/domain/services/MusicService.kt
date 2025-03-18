package com.saul223655.servicios.music.domain.services

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.saul223655.servicios.R

class MusicService : Service() {

    private val binder = MusicBinder()
    private var mediaPlayer: MediaPlayer? = null
    var isPlaying = false

    override fun onCreate() {
        super.onCreate()
        Log.d("ForegroundService", "Servicio creado")
        mediaPlayer = MediaPlayer.create(this, R.raw.audio_sample)
        mediaPlayer?.isLooping = true
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.getStringExtra("ACTION")
        when (action) {
            "PLAY" -> playMusic()
            "PAUSE" -> pauseMusic()
            "STOP" -> stopMusic()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "music_channel",
                "Reproductor de Música",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun playMusic() {
        mediaPlayer?.start()
        isPlaying = true
        showNotification()
    }

    private fun pauseMusic() {
        mediaPlayer?.pause()
        isPlaying = false
        showNotification()
    }

    private fun stopMusic() {
        mediaPlayer?.pause()
        mediaPlayer?.seekTo(0)
        isPlaying = false
    }

    private fun showNotification() {
        val notification = NotificationCompat.Builder(this, "music_channel")
            .setContentTitle("Reproductor de Música")
            .setContentText(if (isPlaying) "Reproduciendo" else "Pausado")
            .setSmallIcon(R.drawable.ic_music_note)
            .build()

        startForeground(1, notification)
    }

}
