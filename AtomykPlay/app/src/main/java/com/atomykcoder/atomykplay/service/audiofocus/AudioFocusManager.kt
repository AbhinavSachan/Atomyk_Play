package com.atomykcoder.atomykplay.service.audiofocus

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.atomykcoder.atomykplay.ApplicationClass
import com.atomykcoder.atomykplay.helperFunctions.Logger
import com.atomykcoder.atomykplay.utils.StorageUtil.SettingsStorage

/**
 * Handles audio focus requests and changes
 * Separates audio policy from playback mechanics
 */
class AudioFocusManager(private val context: Context, private val settingsStorage: SettingsStorage) :
    OnAudioFocusChangeListener {

    private var isFocused = false
    private var wasPlaying = false
    private lateinit var audioManager: AudioManager
    private lateinit var audioFocusRequest: AudioFocusRequest

    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val builder = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(this)
                .setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setLegacyStreamType(android.media.AudioManager.STREAM_MUSIC)
                        .build()
                )
            audioFocusRequest = builder.build()
        }
    }

    /**
     * Requests audio focus for playback
     * @return true if focus was granted
     */
    fun requestAudioFocus(): Boolean {
        val result: Int
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            result = audioManager.requestAudioFocus(audioFocusRequest)
        } else {
            result = audioManager.requestAudioFocus(
                this, android.media.AudioManager.STREAM_MUSIC, android.media.AudioManager.AUDIOFOCUS_GAIN
            )
        }
        isFocused = result == android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        if (isFocused) {
            Logger.normalLog("AUDIOFOCUS_GAIN")
        }
        return isFocused
    }

    /**
     * Removes audio focus when playback stops
     */
    fun removeAudioFocus() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest)
        } else {
            audioManager.abandonAudioFocus(this)
        }
        isFocused = false
    }

    /**
     * Handles audio focus changes from the system
     */
    override fun onAudioFocusChange(focusState: Int) {
        when (focusState) {
            android.media.AudioManager.AUDIOFOCUS_GAIN -> {
                if (wasPlaying) {
                    isFocused = true
                    Logger.normalLog("AUDIOFOCUS_GAIN")
                    // Resume playback would be handled by the PlayerService
                    wasPlaying = false
                }
            }
            android.media.AudioManager.AUDIOFOCUS_LOSS,
            android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (isFocused) {
                    isFocused = false
                    Logger.normalLog("AUDIOFOCUS_LOSS")
                    // Pause playback would be handled by the PlayerService
                    wasPlaying = true
                }
            }
            android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (settingsStorage.loadLowerVol() && isFocused) {
                    Logger.normalLog("AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK")
                    // Duck volume would be handled by the PlayerService
                }
            }
        }
    }

    /** Returns whether we currently have audio focus */
    fun hasAudioFocus(): Boolean = isFocused
}