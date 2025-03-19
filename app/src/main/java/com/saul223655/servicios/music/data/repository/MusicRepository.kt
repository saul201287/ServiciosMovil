package com.saul223655.servicios.music.data.repository

import android.content.ContentValues.TAG
import android.util.Log
import com.saul223655.servicios.music.data.Song
import com.saul223655.servicios.core.database.dao.SongDao

class MusicRepository(private val songDao: SongDao) {
    suspend fun saveSong(title: String) {
        songDao.insert(Song(title = title, timestamp = System.currentTimeMillis()))
    }
    suspend fun getHistory(): List<Song> = songDao.getHistory()

    suspend fun deleteHistory() {
        Log.d(TAG, "deleteHistory: Borrando todo el historial")
        songDao.deleteHistory()
        Log.d(TAG, "deleteHistory: Historial borrado exitosamente")
    }

}