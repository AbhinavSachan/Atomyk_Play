package com.atomykcoder.atomykplay.service.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atomykcoder.atomykplay.models.Music
import com.atomykcoder.atomykplay.state.PlaybackStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Interface defining the contract for PlayerService
 */
interface PlayerService {
    fun play()
    fun pause()
    fun stop()
    fun seekTo(position: Int)
    fun skipToNext()
    fun skipToPrevious()
    fun setQueue(queue: List<Music>, startIndex: Int = 0)
    fun setRepeatMode(mode: PlaybackStateManager.RepeatMode)
    fun setShuffleMode(mode: PlaybackStateManager.ShuffleMode)

    val playbackState: StateFlow<PlaybackStateManager.PlaybackState>
    val currentTrack: StateFlow<Music?>
    val position: StateFlow<Pair<Int, Int>>
    val queue: StateFlow<List<Music>>
}

/**
 * Implementation of PlayerService that delegates to PlaybackStateManager
 * This service handles only playback control (state machine)
 */
class PlayerServiceImpl(private val stateManager: PlaybackStateManager) : PlayerService, ViewModel() {

    override val playbackState: StateFlow<PlaybackStateManager.PlaybackState> = stateManager.playbackState
    override val currentTrack: StateFlow<Music?> = stateManager.currentTrack
    override val position: StateFlow<Pair<Int, Int>> = stateManager.position
    override val queue: StateFlow<List<Music>> = stateManager.queue

    override fun play() {
        // In a real implementation, this would trigger actual playback
        // For now, we update the state to reflect playing
        val currentState = stateManager.playbackState.value
        stateManager.setPlaybackState(true, currentState.isBuffering, currentState.error)
    }

    override fun pause() {
        val currentState = stateManager.playbackState.value
        stateManager.setPlaybackState(false, currentState.isBuffering, currentState.error)
    }

    override fun stop() {
        stateManager.setPlaybackState(false, false)
        stateManager.setPosition(0, 0)
    }

    override fun seekTo(position: Int) {
        val (_, duration) = stateManager.position.value
        stateManager.setPosition(position, duration)
    }

    override fun skipToNext() {
        // Implementation would depend on queue logic
        // This is a placeholder - actual implementation would be in the service layer
    }

    override fun skipToPrevious() {
        // Implementation would depend on queue logic
        // This is a placeholder - actual implementation would be in the service layer
    }

    override fun setQueue(queue: List<Music>, startIndex: Int) {
        stateManager.setQueue(queue)
        if (queue.isNotEmpty() && startIndex in queue.indices) {
            stateManager.setCurrentTrack(queue[startIndex])
        }
    }

    override fun setRepeatMode(mode: PlaybackStateManager.RepeatMode) {
        stateManager.setRepeatMode(mode)
    }

    override fun setShuffleMode(mode: PlaybackStateManager.ShuffleMode) {
        stateManager.setShuffleMode(mode)
    }
}