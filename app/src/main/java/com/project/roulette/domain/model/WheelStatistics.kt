package com.project.roulette.domain.model

/**
 * Statistics aggregation for a wheel.
 * Demonstrates single responsibility principle - only handles stat computation.
 */
data class WheelStatistics(
    val wheelId: String,
    val totalSpins: Int,
    val selectionCounts: Map<String, Int>, // segment name -> count
    val selectionPercentages: Map<String, Float>, // segment name -> percentage
    val mostFrequent: String?,
    val leastFrequent: String?
) {
    val selectionCountMap: Map<String, Int>
        get() = selectionCounts

    fun getSelectionPercentageForSegment(segmentName: String): Float =
        selectionPercentages[segmentName] ?: 0f
}

