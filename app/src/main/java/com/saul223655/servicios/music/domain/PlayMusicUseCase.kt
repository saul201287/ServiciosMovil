package com.saul223655.servicios.music.domain

import android.content.Context
import android.content.Intent
import com.saul223655.servicios.music.domain.services.MusicService

class PlayMusicUseCase {
    operator fun invoke(context: Context, title: String): Result<Unit> {
        return try {
            val intent = Intent(context, MusicService::class.java).apply {
                putExtra("SONG_TITLE", title)
            }
            context.startService(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
