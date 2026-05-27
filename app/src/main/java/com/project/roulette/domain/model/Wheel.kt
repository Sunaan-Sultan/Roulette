package com.project.roulette.domain.model

import kotlinx.datetime.Instant

/**
 * Represents the complete wheel configuration.
 * Encapsulation: manages segments collection with validation.
 */
data class Wheel(
    val id: String,
    val name: String,
    val segments: List<Segment>,
    val createdAt: Instant,
    val updatedAt: Instant,
    val description: String = "",
    val isFavorite: Boolean = false
) {
    init {
        require(name.isNotBlank()) { "Wheel name cannot be blank" }
        require(segments.isNotEmpty()) { "Wheel must have at least one segment" }
        require(segments.size <= 100) { "Wheel cannot have more than 100 segments" }
        require(segments.map { it.id }.distinct().size == segments.size) {
            "Segment IDs must be unique"
        }
    }

    /**
     * Get all active segments (business logic encapsulation)
     */
    fun getActiveSegments(): List<Segment> = segments.filter { it.isActive }

    /**
     * Calculate total weight for weighted selection
     */
    fun getTotalWeight(): Float = getActiveSegments().sumOf { it.weight.toDouble() }.toFloat()

    /**
     * Find segment by ID
     */
    fun getSegmentById(id: String): Segment? = segments.find { it.id == id }
}

