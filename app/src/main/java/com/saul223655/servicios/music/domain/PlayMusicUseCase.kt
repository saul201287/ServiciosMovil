package com.saul223655.servicios.music.domain

import android.content.Context
import android.content.Intent
import com.saul223655.servicios.music.data.repository.MusicRepository
import com.saul223655.servicios.music.domain.services.MusicService

class PlayMusicUseCase(private val repository: MusicRepository) {
    suspend operator fun invoke(title: String, context: Context) {
        repository.saveSong(title)
        context.startService(Intent(context, MusicService::class.java).apply {
            putExtra("ACTION", "PLAY")
            putExtra("TITLE", title)
        })
    }
}
