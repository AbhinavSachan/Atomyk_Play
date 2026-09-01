package com.atomykcoder.atomykplay.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atomykcoder.atomykplay.models.Music
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Singleton class that manages the global playback state using StateFlows.
 * This serves as the single source of truth for all playback-related data.
 */
class PlaybackStateManager : ViewModel() {

    // StateFlows for playback state
    private val _currentTrack = MutableStateFlow<Music?>(null)
    val currentTrack: StateFlow<Music?> = _currentTrack.asStateFlow()

    private val _playbackState = MutableStateFlow<PlaybackState>(
        PlaybackState(isPlaying = false, isBuffering = false)
    )
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _position = MutableStateFlow<Pair<Int, Int>>(Pair(0, 0)) // currentPosition, duration
    val position: StateFlow<Pair<Int, Int>> = _position.asStateFlow()

    private val _queue = MutableStateFlow<List<Music>>(emptyList())
    val queue: StateFlow<List<Music>> = _queue.asStateFlow()

    private val _repeatMode = MutableStateFlow<RepeatMode>(RepeatMode.NONE)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _shuffleMode = MutableStateFlow<ShuffleMode>(ShuffleMode.NONE)
    val shuffleMode: StateFlow<ShuffleMode> = _shuffleMode.asStateFlow()

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Idle)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()

    // Sleep timer live countdown text, e.g. "4:59". Null when no timer is running.
    private val _timerText = MutableStateFlow<String?>(null)
    val timerText: StateFlow<String?> = _timerText.asStateFlow()

    // Setters for updating state (to be called from services)
    fun setCurrentTrack(music: Music?) {
        viewModelScope.launch {
            _currentTrack.value = music
        }
    }

    fun setPlaybackState(isPlaying: Boolean, isBuffering: Boolean, error: Throwable? = null) {
        viewModelScope.launch {
            _playbackState.value = PlaybackState(isPlaying, isBuffering, error)
        }
    }

    fun setPosition(currentPosition: Int, duration: Int) {
        viewModelScope.launch {
            _position.value = Pair(currentPosition, duration)
        }
    }

    fun setQueue(queue: List<Music>) {
        viewModelScope.launch {
            _queue.value = queue
        }
    }

    fun setRepeatMode(mode: RepeatMode) {
        viewModelScope.launch {
            _repeatMode.value = mode
        }
    }

    fun setShuffleMode(mode: ShuffleMode) {
        viewModelScope.launch {
            _shuffleMode.value = mode
        }
    }

    fun setLoadState(state: LoadState) {
        viewModelScope.launch {
            _loadState.value = state
        }
    }

    fun setTimerText(text: String?) {
        viewModelScope.launch {
            _timerText.value = text
        }
    }

    /**
     * Removes a music from the queue
     * @param musicToRemove The music to remove from the queue
     */
    fun removeFromQueue(musicToRemove: Music?) {
        if (musicToRemove == null) return
        viewModelScope.launch {
            val currentQueue = _queue.value
            val updatedQueue = currentQueue.filterNot { it?.id == musicToRemove?.id }
            _queue.value = updatedQueue
        }
    }

    // Data classes for state representation
    data class PlaybackState(
        val isPlaying: Boolean,
        val isBuffering: Boolean,
        val error: Throwable? = null
    )

    data class LoadState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val message: String? = null
    ) {
        companion object {
            val Idle = LoadState(false, null, null)
            val Loading = LoadState(true, null, null)
            fun Error(error: String, message: String? = null) = LoadState(false, error, message)
            fun Success(message: String? = null) = LoadState(false, null, message)
        }
    }

    enum class RepeatMode { NONE, ALL, ONE }
    enum class ShuffleMode { NONE, ALL }
}