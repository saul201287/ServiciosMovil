package com.saul223655.servicios.music.domain.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.saul223655.servicios.MainActivity
import com.saul223655.servicios.R
import com.saul223655.servicios.core.database.AppDatabase

class MusicService : Service() {
    private val binder = MusicBinder()
    private lateinit var mediaPlayer: MediaPlayer
    var isPlaying = false
    private lateinit var db: AppDatabase

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
        Log.d(TAG, "onCreate: Inicializando MediaPlayer y Room")
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.audio_sample)
            if (mediaPlayer == null) {
                Log.e(TAG, "onCreate: Error al crear MediaPlayer, es null")
            } else {
                Log.d(TAG, "onCreate: MediaPlayer creado exitosamente")
            }
            mediaPlayer.setOnCompletionListener {
                Log.d(TAG, "MediaPlayer: Reproducción completada, llamando a stop()")
                stop()
            }
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
        Log.d(TAG, "onStartCommand: Intent recibido con acción: ${intent?.getStringExtra("ACTION")}")
        val action = intent?.getStringExtra("ACTION")
        val title = intent?.getStringExtra("TITLE") ?: "Unknown Song"

        when (action) {
            "PLAY" -> play(title)
            "PAUSE" -> pause()
            "STOP" -> stop()
            else -> Log.w(TAG, "onStartCommand: Acción desconocida: $action")
        }
        return START_STICKY
    }

    @SuppressLint("ForegroundServiceType")
    private fun play(title: String) {
        Log.d(TAG, "play: Intentando reproducir canción: $title")
        if (!mediaPlayer.isPlaying) {
            try {
                mediaPlayer.start()
                isPlaying = true
                Log.d(TAG, "play: Reproducción iniciada")
                val notification = createNotification("Reproduciendo: $title")
                startForeground(NOTIFICATION_ID, notification)
                Log.d(TAG, "play: Servicio foreground iniciado con notificación")
            } catch (e: Exception) {
                Log.e(TAG, "play: Error al iniciar reproducción: ${e.message}")
            }
        } else {
            Log.w(TAG, "play: MediaPlayer ya está reproduciendo")
        }
    }

    @SuppressLint("ForegroundServiceType")
    private fun pause() {
        Log.d(TAG, "pause: Intentando pausar reproducción")
        if (mediaPlayer.isPlaying) {
            try {
                mediaPlayer.pause()
                isPlaying = false
                Log.d(TAG, "pause: Reproducción pausada")
                val notification = createNotification("En pausa")
                startForeground(NOTIFICATION_ID, notification)
                Log.d(TAG, "pause: Notificación actualizada a 'En pausa'")
            } catch (e: Exception) {
                Log.e(TAG, "pause: Error al pausar: ${e.message}")
            }
        } else {
            Log.w(TAG, "pause: MediaPlayer no está reproduciendo")
        }
    }

    private fun stop() {
        Log.d(TAG, "stop: Intentando detener reproducción")
        if (mediaPlayer.isPlaying) {
            try {
                mediaPlayer.stop()
                mediaPlayer.prepare()
                Log.d(TAG, "stop: Reproducción detenida y MediaPlayer preparado")
            } catch (e: Exception) {
                Log.e(TAG, "stop: Error al detener y preparar: ${e.message}")
            }
        }
        isPlaying = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        Log.d(TAG, "stop: Servicio foreground detenido")
        stopSelf()
        Log.d(TAG, "stop: Servicio detenido completamente")
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: Liberando recursos")
        try {
            mediaPlayer.release()
            Log.d(TAG, "onDestroy: MediaPlayer liberado")
        } catch (e: Exception) {
            Log.e(TAG, "onDestroy: Error al liberar MediaPlayer: ${e.message}")
        }
        super.onDestroy()
    }

    private fun createNotification(status: String): Notification {
        Log.d(TAG, "createNotification: Creando notificación con estado: $status")
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return try {
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Music Player")
                .setContentText(status)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(R.drawable.ic_play, "Play", createActionIntent("PLAY"))
                .addAction(R.drawable.ic_pause, "Pause", createActionIntent("PAUSE"))
                .addAction(R.drawable.ic_stop, "Stop", createActionIntent("STOP"))
                .build()
            Log.d(TAG, "createNotification: Notificación creada exitosamente")
            notification
        } catch (e: Exception) {
            Log.e(TAG, "createNotification: Error al crear notificación: ${e.message}")
            throw e
        }
    }

    private fun createActionIntent(action: String): PendingIntent {
        Log.d(TAG, "createActionIntent: Creando PendingIntent para acción: $action")
        val intent = Intent(this, MusicService::class.java).apply { putExtra("ACTION", action) }
        return PendingIntent.getService(this, action.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}