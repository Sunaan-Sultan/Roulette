package com.project.roulette.util.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages audio feedback (sound effects).
 * Demonstrates Abstraction: encapsulates audio complexity.
 * Single Responsibility: only handles sound playback.
 */
@Singleton
class SoundManager @Inject constructor(@ApplicationContext private val context: Context) {
    // Use ToneGenerator so we can play simple tones without external resources
    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    } catch (e: Exception) {
        null
    }

    /**
     * Play spin start sound (short whoosh/tick).
     */
    fun playSpinStart() {
        try {
            // Short start beep (200ms)
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 180)
        } catch (e: Exception) {
            // ignore
        }
    }

    /**
     * Play spin end sound (victory/selection sound).
     */
    fun playSpinEnd() {
        try {
            // A slightly longer confirmation tone
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 320)
        } catch (e: Exception) {
            // ignore
        }
    }

    /**
     * Play click sound for UI interactions.
     */
    fun playClick() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
        } catch (e: Exception) {
            // ignore
        }
    }

    /**
     * Stop any ongoing tone.
     */
    fun stopAll() {
        try {
            toneGenerator?.stopTone()
        } catch (e: Exception) {
            // ignore
        }
    }

    /**
     * Release resources.
     */
    fun release() {
        try {
            toneGenerator?.release()
        } catch (e: Exception) {
            // ignore
        }
        toneGenerator = null
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
