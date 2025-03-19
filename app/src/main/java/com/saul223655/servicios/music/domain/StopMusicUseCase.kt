package com.saul223655.servicios.music.domain

import android.content.Context
import android.content.Intent
import com.saul223655.servicios.music.data.repository.MusicRepository
import com.saul223655.servicios.music.domain.services.MusicService

class StopMusicUseCase(private val repository: MusicRepository) {
    suspend operator fun invoke(context: Context) {
        context.startService(Intent(context, MusicService::class.java).apply {
            putExtra("ACTION", "STOP")
        })
    }
}
