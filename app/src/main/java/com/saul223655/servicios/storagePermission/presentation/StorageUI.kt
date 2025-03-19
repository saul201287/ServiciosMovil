package com.saul223655.servicios.storagePermission.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StoragePermissionUI(
    viewModel: StoragePermissionViewModel,
    onPermissionRequested: (String) -> Unit,
    onFinish: () -> Unit
) {
    val permissionGranted by viewModel.permissionGranted.observeAsState()
    val songsLoaded by viewModel.songsLoaded.observeAsState()

    // Cierra la pantalla cuando las canciones estén cargadas
    LaunchedEffect(songsLoaded) {
        if (songsLoaded == true) {
            onFinish()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (permissionGranted == null) {
            // Muestra el botón para solicitar permisos
            Button(onClick = { viewModel.checkAndRequestPermission(onPermissionRequested) }) {
                Text("Solicitar Permiso de Almacenamiento")
            }
        } else if (permissionGranted == false) {
            Text("Permiso denegado. No se pueden cargar canciones.")
        } else {
            Text("Cargando canciones...")
        }
    }
}