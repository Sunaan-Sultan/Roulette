package com.project.roulette.domain.usecase.selection

import com.project.roulette.domain.model.Segment

/**
 * Strategy pattern interface for segment selection algorithms.
 * Demonstrates polymorphism - different algorithms implement same interface.
 * Open/Closed Principle: open for extension, closed for modification.
 */
interface SelectionAlgorithm {
    /**
     * Select a segment from the list based on algorithm logic.
     * @return the selected segment, or null if no selection possible
     */
    fun selectSegment(segments: List<Segment>): Segment?

    /**
     * Get algorithm name for UI display
     */
    fun getName(): String
}

/**
 * Uniform random selection - each segment has equal probability.
 */
class UniformRandomAlgorithm : SelectionAlgorithm {
    override fun selectSegment(segments: List<Segment>): Segment? {
        if (segments.isEmpty()) return null
        return segments.random()
    }

    override fun getName(): String = "Uniform Random"
}

/**
 * Weighted random selection - probability proportional to segment weight.
 * More sophisticated: uses weight property for biased selection.
 */
class WeightedRandomAlgorithm : SelectionAlgorithm {
    override fun selectSegment(segments: List<Segment>): Segment? {
        if (segments.isEmpty()) return null

        val totalWeight = segments.sumOf { it.weight.toDouble() }
        if (totalWeight <= 0) return null

        var random = Math.random() * totalWeight
        return segments.firstOrNull {
            random -= it.weight
            random <= 0
        }
    }

    override fun getName(): String = "Weighted Random"
}

/**
 * Seeded random selection - deterministic for testing/reproducibility.
 * Useful for demos, testing, or replaying results.
 */
class SeededRandomAlgorithm(private val seed: Long = System.currentTimeMillis()) :
    SelectionAlgorithm {
    private val random = java.util.Random(seed)

    override fun selectSegment(segments: List<Segment>): Segment? {
        if (segments.isEmpty()) return null
        return segments[random.nextInt(segments.size)]
    }

    override fun getName(): String = "Seeded Random (Seed: $seed)"
}

/**
 * Round-robin selection - cycles through segments in order.
 * Deterministic: useful for lessons or tutorials.
 */
class RoundRobinAlgorithm : SelectionAlgorithm {
    private var currentIndex = 0

    override fun selectSegment(segments: List<Segment>): Segment? {
        if (segments.isEmpty()) return null
        val selected = segments[currentIndex % segments.size]
        currentIndex++
        return selected
    }

    override fun getName(): String = "Round Robin"
}

/**
 * Factory for creating selection algorithms.
 * Factory Pattern: encapsulates algorithm instantiation.
 * Demonstrates Single Responsibility: only responsible for creating algorithms.
 */
object SelectionAlgorithmFactory {
    enum class AlgorithmType {
        UNIFORM,
        WEIGHTED,
        SEEDED,
        ROUND_ROBIN
    }

    fun createAlgorithm(type: AlgorithmType, seed: Long? = null): SelectionAlgorithm {
        return when (type) {
            AlgorithmType.UNIFORM -> UniformRandomAlgorithm()
            AlgorithmType.WEIGHTED -> WeightedRandomAlgorithm()
            AlgorithmType.SEEDED -> SeededRandomAlgorithm(seed ?: System.currentTimeMillis())
            AlgorithmType.ROUND_ROBIN -> RoundRobinAlgorithm()
        }
    }

    fun getAllAlgorithmTypes(): List<AlgorithmType> = AlgorithmType.entries

    fun getAlgorithmName(type: AlgorithmType): String {
        return createAlgorithm(type).getName()
    }
}

