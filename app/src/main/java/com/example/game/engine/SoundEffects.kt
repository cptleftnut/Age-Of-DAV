package com.example.game.engine

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

/**
 * Procedural RTS Sound Generator for Age of Mythology audio feedback.
 * Generates unit voice lines, attack clashes, god power thunder, building completion chime, and victory horns.
 */
object SoundEffects {
    private var toneGen: ToneGenerator? = null
    var isMuted: Boolean = false

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (e: Exception) {
            Log.e("SoundEffects", "ToneGenerator init failed", e)
        }
    }

    /**
     * Play Greek/Egyptian/Norse RTS Unit selection voice chime ("Prostagma!", "Etimos!", "Vulome!")
     */
    fun playUnitSelect() {
        if (isMuted) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        } catch (_: Exception) {}
    }

    /**
     * Play Unit Order confirmation chime ("Malista!", "Is Machin!")
     */
    fun playUnitCommand() {
        if (isMuted) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 100)
        } catch (_: Exception) {}
    }

    /**
     * Play UI Button click feedback
     */
    fun playButtonClick() {
        if (isMuted) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
        } catch (_: Exception) {}
    }

    /**
     * Play Sword Clash / Attack Sound
     */
    fun playAttack() {
        if (isMuted) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 60)
        } catch (_: Exception) {}
    }

    /**
     * Play God Power / Magic Spell Effect
     */
    fun playGodPowerCast() {
        if (isMuted) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_DTMF_D, 250)
        } catch (_: Exception) {}
    }

    /**
     * Play Building Complete Chime
     */
    fun playBuildingComplete() {
        if (isMuted) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_PROMPT, 180)
        } catch (_: Exception) {}
    }

    /**
     * Play Age Up Trumpet Fanfare
     */
    fun playAgeUp() {
        if (isMuted) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_DTMF_0, 300)
        } catch (_: Exception) {}
    }

    /**
     * Play Victory Fanfare
     */
    fun playVictory() {
        if (isMuted) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_DTMF_A, 500)
        } catch (_: Exception) {}
    }

    /**
     * Play Defeat Chime
     */
    fun playDefeat() {
        if (isMuted) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_SUP_ERROR, 500)
        } catch (_: Exception) {}
    }
}
