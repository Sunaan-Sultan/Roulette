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
 * Round-robin selection - ensures every segment is picked once before repeating.
 * Deterministic based on total spins count.
 */
class RoundRobinAlgorithm(private val totalSpins: Int = 0) : SelectionAlgorithm {
    override fun selectSegment(segments: List<Segment>): Segment? {
        if (segments.isEmpty()) return null
        
        val size = segments.size
        val roundNumber = totalSpins / size
        val indexInRound = totalSpins % size
        
        // Use roundNumber as seed to have a consistent shuffle for the entire round
        val shuffled = segments.shuffled(java.util.Random(roundNumber.toLong()))
        return shuffled[indexInRound]
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

    fun createAlgorithm(type: AlgorithmType, seed: Long? = null, totalSpins: Int = 0): SelectionAlgorithm {
        return when (type) {
            AlgorithmType.UNIFORM -> UniformRandomAlgorithm()
            AlgorithmType.WEIGHTED -> WeightedRandomAlgorithm()
            AlgorithmType.SEEDED -> SeededRandomAlgorithm(seed ?: System.currentTimeMillis())
            AlgorithmType.ROUND_ROBIN -> RoundRobinAlgorithm(totalSpins)
        }
    }

    fun getAllAlgorithmTypes(): List<AlgorithmType> = AlgorithmType.entries

    fun getAlgorithmName(type: AlgorithmType): String {
        return createAlgorithm(type).getName()
    }
}

