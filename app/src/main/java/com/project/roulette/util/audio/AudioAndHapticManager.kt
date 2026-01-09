package com.project.roulette.util.audio

import android.content.Context
import android.media.MediaPlayer
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
    private var spinStartSound: MediaPlayer? = null
    private var spinEndSound: MediaPlayer? = null
    private var clickSound: MediaPlayer? = null

    init {
        // Initialize sounds (could load from resources)
        // For now, we'll create dummy MediaPlayers
        try {
            spinStartSound = MediaPlayer()
            spinEndSound = MediaPlayer()
            clickSound = MediaPlayer()
        } catch (e: Exception) {
            // Handle initialization errors
        }
    }

    /**
     * Play spin start sound.
     */
    fun playSpinStart() {
        try {
            spinStartSound?.reset()
            // Load from resources and play
            // soundPool.play(spinStartId, 1f, 1f, 1, 0, 1f)
        } catch (e: Exception) {
            // Silently handle errors
        }
    }

    /**
     * Play spin end sound (victory/selection sound).
     */
    fun playSpinEnd() {
        try {
            spinEndSound?.reset()
            // soundPool.play(spinEndId, 1f, 1f, 1, 0, 1f)
        } catch (e: Exception) {
            // Silently handle errors
        }
    }

    /**
     * Play click sound for UI interactions.
     */
    fun playClick() {
        try {
            clickSound?.reset()
            // soundPool.play(clickId, 1f, 1f, 1, 0, 1f)
        } catch (e: Exception) {
            // Silently handle errors
        }
    }

    /**
     * Stop all sounds.
     */
    fun stopAll() {
        spinStartSound?.stop()
        spinEndSound?.stop()
        clickSound?.stop()
    }

    /**
     * Release all resources.
     */
    fun release() {
        spinStartSound?.release()
        spinEndSound?.release()
        clickSound?.release()
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

