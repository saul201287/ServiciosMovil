package com.saul223655.servicios.core.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.saul223655.servicios.core.database.AppDatabase
import com.saul223655.servicios.music.data.repository.MusicRepository
import com.saul223655.servicios.music.domain.PlayMusicUseCase
import com.saul223655.servicios.music.domain.StopMusicUseCase
import com.saul223655.servicios.music.presentation.MusicScreen
import com.saul223655.servicios.music.presentation.MusicViewModel
import com.saul223655.servicios.storagePermission.presentation.StoragePermissionScreen
import com.saul223655.servicios.storagePermission.presentation.StoragePermissionViewModel

class MusicViewModelFactory(
    private val playMusicUseCase: PlayMusicUseCase,
    private val stopMusicUseCase: StopMusicUseCase,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MusicViewModel(playMusicUseCase, stopMusicUseCase, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class StoragePermissionViewModelFactory(
    private val application: Application,
    private val musicViewModel: MusicViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoragePermissionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoragePermissionViewModel(application, musicViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun NavigationWrapper(application: Application) {
    val navController = rememberNavController()

    val db = remember { AppDatabase.getDatabase(application) }
    val repository = remember { MusicRepository(db.songDao()) }
    val playMusicUseCase = remember { PlayMusicUseCase(repository) }
    val stopMusicUseCase = remember { StopMusicUseCase(repository) }

    val musicViewModel: MusicViewModel = viewModel(
        factory = MusicViewModelFactory(playMusicUseCase, stopMusicUseCase, application)
    )

    val storagePermissionViewModel: StoragePermissionViewModel = viewModel(
        factory = StoragePermissionViewModelFactory(application, musicViewModel)
    )

    NavHost(navController = navController, startDestination = Screen.Music.route) {
        composable(Screen.Music.route) {
            MusicScreen(viewModel = musicViewModel, navController = navController)
        }
        composable(Screen.StoragePermission.route) {
            StoragePermissionScreen(
                navController = navController,
                viewModel = storagePermissionViewModel
            )
        }
    }
}