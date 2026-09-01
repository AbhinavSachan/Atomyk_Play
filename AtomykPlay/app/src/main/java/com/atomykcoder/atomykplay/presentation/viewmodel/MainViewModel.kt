package com.atomykcoder.atomykplay.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atomykcoder.atomykplay.models.Music
import com.atomykcoder.atomykplay.state.PlaybackStateManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for MainActivity that collects from PlaybackStateManager
 * This prepares for potential migration to Compose or better UI separation
 */
class MainViewModel(private val stateManager: PlaybackStateManager) : ViewModel() {

    // Expose StateFlows from PlaybackStateManager for UI observation
    val currentTrack: StateFlow<Music?> = stateManager.currentTrack
    val playbackState: StateFlow<PlaybackStateManager.PlaybackState> = stateManager.playbackState
    val position: StateFlow<Pair<Int, Int>> = stateManager.position
    val queue: StateFlow<List<Music>> = stateManager.queue
    val repeatMode: StateFlow<PlaybackStateManager.RepeatMode> = stateManager.repeatMode
    val shuffleMode: StateFlow<PlaybackStateManager.ShuffleMode> = stateManager.shuffleMode
    val loadState: StateFlow<PlaybackStateManager.LoadState> = stateManager.loadState

    // Methods to trigger actions (to be called from UI)
    fun play() {
        // TODO: Implement actual playback triggering through use cases/services
        // For now, this is a placeholder
    }

    fun pause() {
        // TODO: Implement actual pause triggering
    }

    fun stop() {
        // TODO: Implement actual stop triggering
    }

    fun seekTo(position: Int) {
        // TODO: Implement actual seek triggering
    }

    fun skipToNext() {
        // TODO: Implement actual skip next triggering
    }

    fun skipToPrevious() {
        // TODO: Implement actual skip previous triggering
    }

    fun setQueue(queue: List<Music>, startIndex: Int = 0) {
        // TODO: Implement actual queue setting
    }

    fun setRepeatMode(mode: PlaybackStateManager.RepeatMode) {
        // TODO: Implement actual repeat mode setting
    }

    fun setShuffleMode(mode: PlaybackStateManager.ShuffleMode) {
        // TODO: Implement actual shuffle mode setting
    }
}