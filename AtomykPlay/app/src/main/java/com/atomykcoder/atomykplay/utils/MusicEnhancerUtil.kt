package com.atomykcoder.atomykplay.utils

import android.media.audiofx.BassBoost
import android.media.audiofx.Virtualizer

class MusicEnhancerUtil(audioSessionId: Int) {

    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    init {
        bassBoost = try {
            BassBoost(0, audioSessionId)
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
            bassBoost?.enabled = enable
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
    fun setBassBandLevel(bassLevel: Int) {
        require(bassLevel <= 100 || bassLevel >= 0) { "Band level must be between 0 to 100" }
        val totalIncrease = (100 * bassLevel).toShort()
        try {
            bassBoost!!.setStrength(totalIncrease)
        } catch (_: Exception) {
        }
    }

    /**
     * sets the strength of the virtualizer, strength can be between 0 and 100
     * throws IllegalArgumentException if the strength is not between 0 and 100
     */
    fun setVirtualizerStrength(strength: Int) {
        require(strength <= 100 || strength >= 0) { "Strength must be between 0 to 100" }
        val totalIncrease = (100 * strength).toShort()
        try {
            virtualizer?.setStrength(totalIncrease)
        } catch (_: Exception) {
        }
    }

    fun releaseEqualizer() {
        bassBoost?.release()
        virtualizer?.release()
        bassBoost = null
        virtualizer = null
    }
}