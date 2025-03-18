package com.saul223655.servicios.music.presentation

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.*
import com.saul223655.servicios.music.domain.PlayMusicUseCase
import com.saul223655.servicios.music.domain.StopMusicUseCase
import com.saul223655.servicios.music.domain.GetSongHistoryUseCase
import com.saul223655.servicios.music.domain.services.MusicService
import kotlinx.coroutines.launch

class MusicViewModel() : ViewModel() {

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private var musicService: MusicService? = null
    private var serviceConnection: ServiceConnection? = null

    fun bindService(context: Context) {
        val intent = Intent(context, MusicService::class.java)
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                musicService = (binder as MusicService.MusicBinder).getService()
                _isPlaying.value = musicService?.isPlaying ?: false
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                musicService = null
            }
        }
        context.bindService(intent, serviceConnection!!, Context.BIND_AUTO_CREATE)
    }

    fun unbindService(context: Context) {
        serviceConnection?.let {
            context.unbindService(it)
            serviceConnection = null
        }
    }

    fun playMusic(context: Context) {
        context.startService(Intent(context, MusicService::class.java).apply {
            putExtra("ACTION", "PLAY")
        })
        _isPlaying.value = true
    }

    fun pauseMusic(context: Context) {
        context.startService(Intent(context, MusicService::class.java).apply {
            putExtra("ACTION", "PAUSE")
        })
        _isPlaying.value = false
    }

    fun stopMusic(context: Context) {
        context.startService(Intent(context, MusicService::class.java).apply {
            putExtra("ACTION", "STOP")
        })
        _isPlaying.value = false
    }
}

