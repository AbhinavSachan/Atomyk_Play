package com.atomykcoder.atomykplay.utils

import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import com.atomykcoder.atomykplay.helperFunctions.Logger

class EqualizerUtil(audioSessionId: Int) {

    private var equalizer: Equalizer? = null
    private var virtualizer: Virtualizer? = null

    init {
        equalizer = try {
            Equalizer(0, audioSessionId)
        } catch (e: Exception) {
            null
        }
        virtualizer = try {
            Virtualizer(0, audioSessionId)
        } catch (e: Exception) {
            null
        }
    }

    fun enableEffects(enable: Boolean) {
        try {
            equalizer?.enabled = enable
        } catch (_: Exception) {
        }
        try {
            virtualizer?.enabled = enable
        } catch (_: Exception) {
        }
    }

    /**
     * sets the bass band level of the equalizer, level can be between 0 and 100
     * throws IllegalArgumentException if the bandLevel is not between 0 and 100
     */
    fun setBassBandLevel(bandLevel: Int) {
        require(bandLevel <= 100 || bandLevel >= 0) { "Band level must be between 0 to 100" }
        val bandRange = equalizer?.bandLevelRange
        val onePart = (bandRange?.get(1) ?: 1000) / 100
        val totalIncrease = (onePart * bandLevel).toShort()
        try {
            equalizer?.setBandLevel(0, totalIncrease)
        } catch (_: Exception) {
        }
    }

    /**
     * sets the strength of the virtualizer, strength can be between 0 and 100
     * throws IllegalArgumentException if the strength is not between 0 and 100
     */
    fun setVirtualizerStrength(strength: Int) {
        require(strength <= 100 || strength >= 0) { "Strength must be between 0 to 100" }
        val onePart = 1000 / 100
        val totalIncrease = (onePart * strength).toShort()
        try {
            virtualizer?.setStrength(totalIncrease)
        } catch (_: Exception) {
        }
    }

    fun releaseEqualizer() {
        equalizer?.release()
        virtualizer?.release()
        equalizer = null
        virtualizer = null
    }
}