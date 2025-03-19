package com.saul223655.servicios.storagePermission.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavController
import com.saul223655.servicios.core.navigation.Screen

@Composable
fun StoragePermissionScreen(
    navController: NavController,
    viewModel: StoragePermissionViewModel
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    val songsLoaded by viewModel.songsLoaded.observeAsState()
    LaunchedEffect(songsLoaded) {
        if (songsLoaded == true) {
            navController.popBackStack(Screen.Music.route, inclusive = false)
        }
    }

    StoragePermissionUI(
        viewModel = viewModel,
        onPermissionRequested = { permission ->
            permissionLauncher.launch(permission)
        },
        onFinish = {
            navController.popBackStack(Screen.Music.route, inclusive = false)
        }
    )
}