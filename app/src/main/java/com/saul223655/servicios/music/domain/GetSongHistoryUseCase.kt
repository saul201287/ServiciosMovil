package com.saul223655.servicios.music.domain

import android.app.Application
import com.saul223655.servicios.core.database.AppDatabase

class GetSongHistoryUseCase(application: Application) {
    private val songDao = AppDatabase.getDatabase(application).songDao()

    suspend operator fun invoke(): Result<List<String>> {
        return try {
            val history = songDao.getAllSongs().map { it.title }
            Result.success(history)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
