package com.saul223655.servicios.storagePermission.presentation

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.saul223655.servicios.storagePermission.data.datasource.SongFile
import com.saul223655.servicios.music.presentation.MusicViewModel
import kotlinx.coroutines.launch

 class StoragePermissionViewModel(
    application: Application,
    private val musicViewModel: MusicViewModel
) : AndroidViewModel(application) {

    private val TAG = "StoragePermissionViewModel"

    private val _permissionGranted = MutableLiveData<Boolean>()
    val permissionGranted: LiveData<Boolean> = _permissionGranted

    private val _songsLoaded = MutableLiveData<Boolean>()
    val songsLoaded: LiveData<Boolean> = _songsLoaded

    fun checkAndRequestPermission(permissionLauncher: (String) -> Unit) {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(getApplication(), permission) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "Permiso ya concedido, cargando canciones")
                _permissionGranted.value = true
                loadSongs()
            }
            else -> {
                Log.d(TAG, "Solicitando permiso: $permission")
                permissionLauncher(permission)
            }
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            Log.d(TAG, "Permiso concedido, cargando canciones")
            _permissionGranted.value = true
            loadSongs()
        } else {
            Log.w(TAG, "Permiso denegado")
            _permissionGranted.value = false
        }
    }

    private fun loadSongs() {
        viewModelScope.launch {
            val songs = getAudioFilesFromStorage()
            musicViewModel.setAvailableSongs(songs)
            Log.d(TAG, "loadSongs: Canciones cargadas - ${songs.size} encontradas")
            _songsLoaded.value = true
        }
    }

    private fun getAudioFilesFromStorage(): List<SongFile> {
        val audioFiles = mutableListOf<SongFile>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA
        )

        getApplication<Application>().contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val path = cursor.getString(dataColumn)
                audioFiles.add(SongFile(id, title, path))
            }
        }
        return audioFiles
    }
}