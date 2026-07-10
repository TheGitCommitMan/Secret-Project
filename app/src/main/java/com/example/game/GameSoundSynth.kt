package com.example.game

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object GameSoundSynth {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (e: Exception) {
            Log.e("GameSoundSynth", "Failed to initialize ToneGenerator", e)
        }
    }

    fun playSound(type: SoundType, isSoundEnabled: Boolean = true) {
        if (!isSoundEnabled) return
        val gen = toneGenerator ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (type) {
                    SoundType.Alarm -> {
                        // Flashing alarm sirens
                        repeat(3) {
                            gen.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 300)
                            delay(400)
                        }
                    }
                    SoundType.Kill -> {
                        // Horror strike
                        gen.startTone(ToneGenerator.TONE_PROP_BEEP2, 150)
                        delay(100)
                        gen.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 250)
                    }
                    SoundType.Vent -> {
                        // Low whoosh tone
                        gen.startTone(ToneGenerator.TONE_CDMA_LOW_SS, 200)
                    }
                    SoundType.Report -> {
                        // High megaphone call
                        gen.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 400)
                    }
                    SoundType.Emergency -> {
                        // High priority call
                        gen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500)
                    }
                    SoundType.TaskComplete -> {
                        // High cheerful double beep
                        gen.startTone(ToneGenerator.TONE_PROP_ACK, 150)
                        delay(120)
                        gen.startTone(ToneGenerator.TONE_PROP_ACK, 150)
                    }
                    SoundType.MeetingVote -> {
                        // Single soft beep
                        gen.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                    }
                    SoundType.GameWin -> {
                        // Melodic chime
                        gen.startTone(ToneGenerator.TONE_PROP_ACK, 200)
                        delay(150)
                        gen.startTone(ToneGenerator.TONE_PROP_PROMPT, 200)
                        delay(150)
                        gen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 350)
                    }
                    SoundType.GameLose -> {
                        // Melodic downer chime
                        gen.startTone(ToneGenerator.TONE_PROP_BEEP2, 250)
                        delay(200)
                        gen.startTone(ToneGenerator.TONE_PROP_BEEP2, 350)
                    }
                }
            } catch (e: Exception) {
                Log.e("GameSoundSynth", "Error playing synth tone", e)
            }
        }
    }
}
