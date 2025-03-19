package com.saul223655.servicios.music.presentation

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.saul223655.servicios.core.database.AppDatabase
import com.saul223655.servicios.storagePermission.data.datasource.SongFile
import com.saul223655.servicios.music.data.repository.MusicRepository
import com.saul223655.servicios.music.data.Song
import com.saul223655.servicios.music.domain.services.MusicService
import com.saul223655.servicios.music.domain.PlayMusicUseCase
import com.saul223655.servicios.music.domain.StopMusicUseCase
import kotlinx.coroutines.launch

class MusicViewModel(
    private val playMusicUseCase: PlayMusicUseCase,
    private val stopMusicUseCase: StopMusicUseCase,
    application: Application
) : AndroidViewModel(application) {

    private val TAG = "MusicViewModel"
    private val repository = MusicRepository(AppDatabase.getDatabase(application).songDao())

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _history = MutableLiveData<List<Song>>(emptyList())
    val history: LiveData<List<Song>> = _history

    private val _availableSongs = MutableLiveData<List<SongFile>>(emptyList())
    val availableSongs: LiveData<List<SongFile>> = _availableSongs

    private var musicService: MusicService? = null
    private var serviceConnection: ServiceConnection? = null

    init {
        bindService()
    }

    private fun bindService() {
        Log.d(TAG, "bindService: Intentando vincular al servicio")
        val intent = Intent(getApplication(), MusicService::class.java)
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                Log.d(TAG, "onServiceConnected: Servicio conectado")
                musicService = (binder as MusicService.MusicBinder).getService()
                _isPlaying.value = musicService?.isPlaying ?: false
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Log.w(TAG, "onServiceDisconnected: Servicio desconectado")
                musicService = null
            }
        }
        try {
            getApplication<Application>().bindService(intent, serviceConnection!!, Application.BIND_AUTO_CREATE)
            Log.d(TAG, "bindService: bindService llamado exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "bindService: Error al vincular servicio: ${e.message}")
        }
    }

    fun unbindService() {
        serviceConnection?.let {
            getApplication<Application>().unbindService(it)
            serviceConnection = null
        }
    }

    fun playMusic(song: SongFile) {
        Log.d(TAG, "playMusic: Reproduciendo ${song.title}")
        viewModelScope.launch {
            try {
                playMusicUseCase(song.title, getApplication())
                getApplication<Application>().startService(Intent(getApplication(), MusicService::class.java).apply {
                    putExtra("ACTION", "PLAY")
                    putExtra("TITLE", song.title)
                    putExtra("PATH", song.path)
                })
                _isPlaying.value = true
                loadHistory()
            } catch (e: Exception) {
                Log.e(TAG, "playMusic: Error al reproducir: ${e.message}")
            }
        }
    }

    fun playDefaultAudio() {
        Log.d(TAG, "playDefaultAudio: Reproduciendo audio por defecto")
        viewModelScope.launch {
            try {
                playMusicUseCase("Default Audio", getApplication())
                getApplication<Application>().startService(Intent(getApplication(), MusicService::class.java).apply {
                    putExtra("ACTION", "PLAY")
                    putExtra("TITLE", "Default Audio")
                    // No pasamos PATH para que use R.raw.audio_sample
                })
                _isPlaying.value = true
                loadHistory()
            } catch (e: Exception) {
                Log.e(TAG, "playDefaultAudio: Error al reproducir: ${e.message}")
            }
        }
    }

    fun pauseMusic() {
        getApplication<Application>().startService(Intent(getApplication(), MusicService::class.java).apply {
            putExtra("ACTION", "PAUSE")
        })
        _isPlaying.value = false
    }

    fun stopMusic() {
        viewModelScope.launch {
            stopMusicUseCase(getApplication())
            _isPlaying.value = false
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            val historyList = repository.getHistory()
            _history.value = historyList
        }
    }

    fun deleteHistory() {
        viewModelScope.launch {
            repository.deleteHistory()
            _history.value = emptyList()
        }
    }

    fun setAvailableSongs(songs: List<SongFile>) {
        _availableSongs.value = songs
    }

    override fun onCleared() {
        unbindService()
        super.onCleared()
    }
}