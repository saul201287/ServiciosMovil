package com.saul223655.servicios.music.domain

import android.content.Context
import android.content.Intent
import com.saul223655.servicios.music.domain.services.MusicService

class StopMusicUseCase {
    operator fun invoke(context: Context): Result<Unit> {
        return try {
            val intent = Intent(context, MusicService::class.java)
            context.stopService(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
