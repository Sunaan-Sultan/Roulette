package com.project.roulette.domain.model

import kotlinx.datetime.Instant

/**
 * Represents the result of a wheel spin.
 * Immutable data class for type-safe result handling.
 */
data class SpinResult(
    val id: String,
    val wheelId: String,
    val selectedSegmentId: String,
    val selectedSegmentName: String,
    val spinTimestamp: Instant,
    val spinDuration: Long, // milliseconds
    val finalAngle: Float // degrees
)

