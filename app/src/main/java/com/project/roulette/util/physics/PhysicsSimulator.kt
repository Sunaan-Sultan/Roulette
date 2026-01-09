package com.project.roulette.util.physics

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

/**
 * A collection of easing functions used by the spin physics simulator.
 * These functions map a normalized progress value (0..1) to an eased progress (0..1).
 */
object EasingFunctions {
    /** Linear easing (constant speed). */
    fun linear(progress: Float): Float = progress

    /**
     * Ease out cubic - starts fast and decelerates smoothly towards the end.
     * Formula: 1 - (1 - x)^3
     */
    fun easeOutCubic(progress: Float): Float {
        val x = progress.coerceIn(0f, 1f)
        return (1f - (1f - x).pow(3))
    }

    /**
     * Ease in-out cubic - smooth acceleration and deceleration.
     */
    fun easeInOutCubic(progress: Float): Float {
        val x = progress.coerceIn(0f, 1f)
        return if (x < 0.5f) {
            4f * x * x * x
        } else {
            1f - (-2f * x + 2f).let { it * it * it } / 2f
        }
    }

    /**
     * Ease out exponential - rapid deceleration. Be careful with floating precision.
     */
    fun easeOutExpo(progress: Float): Float {
        val x = progress.coerceIn(0f, 1f)
        return if (x >= 1f) 1f else (1f - 2.0.pow((-10f * x).toDouble()).toFloat())
    }

    /**
     * Cosine-based ease out - mimics gentle physical friction.
     */
    fun easeOutCosine(progress: Float): Float {
        val x = progress.coerceIn(0f, 1f)
        return ((1.0 + cos(PI * x)) / 2.0).toFloat()
    }
}

/**
 * SpinPhysicsSimulator calculates rotation angles for a wheel spin animation.
 * Single Responsibility: only handles animation timing/angle math.
 *
 * @param easingFunction easing function to use for the animation
 * @param baseDuration overall animation duration in milliseconds
 * @param totalRotations number of full rotations before settling on final sector
 */
data class SpinPhysicsSimulator(
    val easingFunction: (Float) -> Float = EasingFunctions::easeOutCubic,
    val baseDuration: Long = 3000L,
    val totalRotations: Float = 5f
) {

    /**
     * Calculate the normalized progress [0..1] for a given elapsed time.
     */
    fun getProgress(elapsedTime: Long): Float =
        (elapsedTime.toFloat() / baseDuration).coerceIn(0f, 1f)

    /**
     * Whether the animation has completed at the given elapsed time.
     */
    fun isComplete(elapsedTime: Long): Boolean = elapsedTime >= baseDuration

    /**
     * Calculate the current rotation angle (in degrees, 0..360) for a given elapsed time.
     * The returned angle represents how many degrees the wheel has rotated from the start.
     */
    fun calculateAngle(elapsedTime: Long): Float {
        val progress = getProgress(elapsedTime)
        val eased = easingFunction(progress).coerceIn(0f, 1f)

        // total degrees = full rotations + the eased fraction of the final rotation
        val totalDegrees = totalRotations * 360f + (1f - eased) * 360f

        // Normalize to [0, 360)
        return (totalDegrees % 360f + 360f) % 360f
    }
}
