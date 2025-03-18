package com.saul223655.servicios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.saul223655.servicios.music.presentation.MusicScreen
import com.saul223655.servicios.music.presentation.MusicViewModel

class MainActivity : ComponentActivity() {
    private val musicViewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicScreen(musicViewModel)
        }
    }
}