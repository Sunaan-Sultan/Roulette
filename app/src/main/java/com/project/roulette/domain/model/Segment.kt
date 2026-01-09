package com.project.roulette.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Represents a single segment on the wheel.
 * Demonstrates encapsulation — immutable data with validation in the initializer.
 */
data class Segment(
    val id: String,
    val name: String,
    val color: Color,
    val weight: Float = 1f, // For weighted selection algorithm
    val isActive: Boolean = true
) {
    init {
        require(name.isNotBlank()) { "Segment name cannot be blank" }
        require(weight > 0f) { "Segment weight must be positive" }
    }
}
