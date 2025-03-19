package com.saul223655.servicios.music.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "song_history")
data class Song(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val timestamp: Long
)