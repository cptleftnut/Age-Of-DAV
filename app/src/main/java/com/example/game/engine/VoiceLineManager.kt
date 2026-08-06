package com.example.game.engine

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.game.model.Faction
import java.util.Locale
import kotlin.random.Random

enum class VoiceLineAction {
    SELECT,
    MOVE,
    ATTACK,
    GATHER
}

data class VoiceLineData(
    val phrase: String,
    val translation: String,
    val action: VoiceLineAction,
    val faction: Faction
) {
    val displayMessage: String get() = "\"$phrase\" ($translation)"
}

/**
 * Localized Voice Response & Sound Management Engine for Age of Mythology 3D.
 * Synthesizes authentic pantheon voice lines (Greek, Egyptian, Norse, Atlantean)
 * using Android TextToSpeech and audio tone cues upon unit selection and orders.
 */
object VoiceLineManager : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    var isMuted: Boolean = false

    fun init(context: Context) {
        if (tts == null) {
            try {
                tts = TextToSpeech(context.applicationContext, this)
            } catch (e: Exception) {
                Log.e("VoiceLineManager", "Failed to initialize TextToSpeech", e)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
                tts?.setSpeechRate(1.05f)
                tts?.setPitch(1.15f)
            }
        } else {
            Log.w("VoiceLineManager", "TextToSpeech init failed with status: $status")
        }
    }

    private val greekPhrases = mapOf(
        VoiceLineAction.SELECT to listOf(
            "Prostagma?" to "At your command?",
            "Etimos!" to "Ready!",
            "Vulome!" to "At your service!",
            "Legis?" to "Speak?"
        ),
        VoiceLineAction.MOVE to listOf(
            "Orthos!" to "Right away!",
            "Malista!" to "Understood!",
            "Pame!" to "Let's go!",
            "Prochorei!" to "Advancing!"
        ),
        VoiceLineAction.ATTACK to listOf(
            "Is Machin!" to "To battle!",
            "Eis Volin!" to "To attack!",
            "Proseche!" to "Watch out!",
            "Polemomen!" to "We fight!"
        ),
        VoiceLineAction.GATHER to listOf(
            "Metalleus!" to "Gathering!",
            "Kharis!" to "For the harvest!",
            "Ergazomai!" to "Working!"
        )
    )

    private val egyptianPhrases = mapOf(
        VoiceLineAction.SELECT to listOf(
            "Yu'a!" to "Yes!",
            "Seneb-ti!" to "Ready!",
            "Nefer!" to "Good!",
            "Hekat?" to "My lord?"
        ),
        VoiceLineAction.MOVE to listOf(
            "Aah!" to "Understood!",
            "Au-k!" to "On my way!",
            "Ma'at!" to "As ordered!",
            "Iyi-m-hetep!" to "In peace!"
        ),
        VoiceLineAction.ATTACK to listOf(
            "Khepesh!" to "To arms!",
            "Seket!" to "Destroy them!",
            "Sutekh!" to "By Seth!",
            "Mawet!" to "To death!"
        ),
        VoiceLineAction.GATHER to listOf(
            "Akhet!" to "Harvesting!",
            "Hapi!" to "Nile abundance!",
            "Ir-set!" to "Building!"
        )
    )

    private val norsePhrases = mapOf(
        VoiceLineAction.SELECT to listOf(
            "Já!" to "Yes!",
            "Hvat er?" to "What is it?",
            "Komið þér!" to "Greetings!",
            "Vilja?" to "Your wish?"
        ),
        VoiceLineAction.MOVE to listOf(
            "Allt í lagi!" to "All right!",
            "Svo skal vera!" to "So it shall be!",
            "Fram!" to "Forward!",
            "Ganga!" to "Marching!"
        ),
        VoiceLineAction.ATTACK to listOf(
            "Til bardaga!" to "To battle!",
            "Fyrir Óðin!" to "For Odin!",
            "Sverð!" to "Swords out!",
            "Drepa!" to "Slay them!"
        ),
        VoiceLineAction.GATHER to listOf(
            "Vinna!" to "Working!",
            "Matr!" to "Gathering food!",
            "Safna!" to "Harvesting!"
        )
    )

    private val atlanteanPhrases = mapOf(
        VoiceLineAction.SELECT to listOf(
            "Imperium?" to "Command me?",
            "Directo!" to "Ready!",
            "Audio!" to "I hear!",
            "Paratus!" to "Prepared!"
        ),
        VoiceLineAction.MOVE to listOf(
            "Eito!" to "Done!",
            "Kalos!" to "Noble!",
            "Celeriter!" to "Swiftly!",
            "Perge!" to "Proceeding!"
        ),
        VoiceLineAction.ATTACK to listOf(
            "Bello!" to "To war!",
            "Invictus!" to "Unconquered!",
            "Pugna!" to "Fight!",
            "Caede!" to "Strike!"
        ),
        VoiceLineAction.GATHER to listOf(
            "Copia!" to "Abundance!",
            "Fabor!" to "Harvesting!",
            "Laboro!" to "At work!"
        )
    )

    fun triggerVoiceLine(
        faction: Faction,
        action: VoiceLineAction
    ): VoiceLineData {
        val phrasesMap = when (faction) {
            Faction.GREEK -> greekPhrases
            Faction.EGYPTIAN -> egyptianPhrases
            Faction.NORSE -> norsePhrases
            Faction.ATLANTEAN -> atlanteanPhrases
        }

        val options = phrasesMap[action] ?: listOf("Prostagma?" to "At your command?")
        val selectedPair = options[Random.nextInt(options.size)]
        val voiceData = VoiceLineData(
            phrase = selectedPair.first,
            translation = selectedPair.second,
            action = action,
            faction = faction
        )

        // Trigger Audio Tone Chime via SoundEffects
        when (action) {
            VoiceLineAction.SELECT -> SoundEffects.playUnitSelect()
            VoiceLineAction.MOVE -> SoundEffects.playUnitCommand()
            VoiceLineAction.ATTACK -> SoundEffects.playAttack()
            VoiceLineAction.GATHER -> SoundEffects.playUnitCommand()
        }

        // Trigger TextToSpeech synthesized speech
        if (!isMuted && isTtsReady && tts != null) {
            try {
                // Slightly randomize pitch for unit voice variety
                val pitch = 0.95f + (Random.nextFloat() * 0.3f)
                tts?.setPitch(pitch)
                tts?.speak(voiceData.phrase, TextToSpeech.QUEUE_FLUSH, null, "VoiceLine_${System.currentTimeMillis()}")
            } catch (e: Exception) {
                Log.e("VoiceLineManager", "Speech error", e)
            }
        }

        return voiceData
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isTtsReady = false
        } catch (_: Exception) {}
    }
}
