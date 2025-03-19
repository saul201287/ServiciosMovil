package com.saul223655.servicios.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.saul223655.servicios.music.data.Song

@Dao
interface SongDao {
    @Insert
    suspend fun insert(song: Song)

    @Query("SELECT * FROM song_history ORDER BY timestamp DESC")
    suspend fun getHistory(): List<Song>

    @Query("DELETE FROM song_history")
    suspend fun deleteHistory()
}