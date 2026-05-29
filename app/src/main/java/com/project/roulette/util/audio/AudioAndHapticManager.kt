package com.project.roulette.util.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.project.roulette.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages audio feedback (sound effects).
 */
@Singleton
class SoundManager @Inject constructor(@ApplicationContext private val context: Context) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(10)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var tickSoundId: Int = 0
    private var isTickLoaded = false
    
    private var mediaPlayer: MediaPlayer? = null
    
    // Fallback pip sound
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (sampleId == tickSoundId && status == 0) {
                isTickLoaded = true
            }
        }
        loadSounds()
    }

    private fun loadSounds() {
        try {
            tickSoundId = soundPool.load(context, R.raw.wheel_tick, 1)
        } catch (e: Exception) {
            // Ignore, will use fallback
        }
    }

    /**
     * Play spin start sound.
     */
    fun playSpinStart() {
        try {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
        } catch (e: Exception) {}
    }

    /**
     * Play a single tick sound (call this when wheel rotates past a segment).
     */
    fun playTick() {
        if (isTickLoaded) {
            val result = soundPool.play(tickSoundId, 1f, 1f, 1, 0, 1.2f)
            if (result == 0) {
                // Play fallback if play fails
                playFallbackTick()
            }
        } else {
            playFallbackTick()
        }
    }

    private fun playFallbackTick() {
        try {
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 50)
        } catch (e: Exception) {}
    }

    /**
     * Play winner applause sound using MediaPlayer for maximum compatibility.
     */
    fun playWinnerClap() {
        try {
            stopWinnerSound()
            mediaPlayer = MediaPlayer.create(context, R.raw.winner_claps).apply {
                setOnCompletionListener { 
                    it.release()
                    if (mediaPlayer == it) mediaPlayer = null
                }
                start()
            }
        } catch (e: Exception) {
            // Fallback victory tone
            try {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 500)
            } catch (ex: Exception) {}
        }
    }

    fun playSpinEnd() {
        playWinnerClap()
    }

    /**
     * Stop all sounds (especially the long applause).
     */
    fun stopAll() {
        stopWinnerSound()
        soundPool.autoPause()
    }
    
    private fun stopWinnerSound() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {}
    }

    /**
     * Release resources.
     */
    fun release() {
        stopAll()
        soundPool.release()
        toneGenerator.release()
    }
}

/**
 * Manages haptic feedback (vibration).
 * Single Responsibility: only handles haptic patterns.
 */
@Singleton
class HapticFeedback @Inject constructor(@ApplicationContext private val context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Light tap feedback.
     */
    fun lightTap() {
        vibrate(intArrayOf(0, 20), -1)
    }

    /**
     * Medium vibration feedback.
     */
    fun mediumVibration() {
        vibrate(intArrayOf(0, 50), -1)
    }

    /**
     * Strong vibration for selection/completion.
     */
    fun strongVibration() {
        vibrate(intArrayOf(0, 100), -1)
    }

    /**
     * Complex vibration pattern (e.g., success pattern).
     */
    fun successPattern() {
        // Pattern: wait, vibrate, wait, vibrate, wait, vibrate
        vibrate(intArrayOf(0, 30, 100, 30, 100, 30), -1)
    }

    /**
     * Continuous vibration during spin.
     */
    fun spinVibration() {
        vibrate(intArrayOf(0, 15, 20, 15, 20, 15), -1)
    }

    /**
     * Stop all vibrations.
     */
    fun cancel() {
        vibrator?.cancel()
    }

    private fun vibrate(pattern: IntArray, repeat: Int) {
        if (!isHapticFeedbackEnabled()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = VibrationEffect.createWaveform(pattern.map { it.toLong() }.toLongArray(), repeat)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern.map { it.toLong() }.toLongArray(), repeat)
            }
        } catch (e: Exception) {
            // Silently handle errors
        }
    }

    private fun isHapticFeedbackEnabled(): Boolean {
        return vibrator?.hasVibrator() == true
    }
}
