package com.project.roulette.domain.usecase.spin

import com.project.roulette.domain.model.Result
import com.project.roulette.domain.model.Segment
import com.project.roulette.domain.model.SpinResult
import com.project.roulette.domain.repository.SpinHistoryRepository
import com.project.roulette.domain.repository.WheelRepository
import com.project.roulette.domain.usecase.selection.SelectionAlgorithmFactory
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import java.util.UUID

/**
 * Use case for spinning the wheel and selecting a segment.
 * Orchestrates multiple responsibilities in a single, focused operation.
 * Demonstrates dependency injection and composition over inheritance.
 */
class SpinWheelUseCase(
    private val wheelRepository: WheelRepository,
    private val spinHistoryRepository: SpinHistoryRepository,
    private val selectionAlgorithmFactory: SelectionAlgorithmFactory
) {
    /**
     * Execute a wheel spin operation.
     * @param wheelId the wheel to spin
     * @param algorithmType the selection algorithm to use
     * @param spinDuration animation duration in milliseconds
     * @return Result containing the selected segment and updated statistics
     */
    suspend operator fun invoke(
        wheelId: String,
        algorithmType: SelectionAlgorithmFactory.AlgorithmType,
        spinDuration: Long = 3000
    ): Result<SpinOutcome> {
        return try {
            // Get the wheel using `first()` to obtain the latest value and avoid collecting indefinitely
            val wheelResult = wheelRepository.getWheelById(wheelId).first()

            if (wheelResult !is Result.Success) {
                return Result.Error(Exception("Wheel not found"))
            }

            val activeSegments = wheelResult.data.getActiveSegments()
            if (activeSegments.isEmpty()) {
                return Result.Error(Exception("No active segments to spin"))
            }

            // Select segment using specified algorithm
            val algorithm = selectionAlgorithmFactory.createAlgorithm(algorithmType)
            val selectedSegment = algorithm.selectSegment(activeSegments)
                ?: return Result.Error(Exception("Selection algorithm failed"))

            // Record the spin
            val spinResult = SpinResult(
                id = UUID.randomUUID().toString(),
                wheelId = wheelId,
                selectedSegmentId = selectedSegment.id,
                selectedSegmentName = selectedSegment.name,
                spinTimestamp = Clock.System.now(),
                spinDuration = spinDuration,
                finalAngle = calculateFinalAngle(activeSegments, selectedSegment)
            )

            spinHistoryRepository.recordSpin(spinResult).onError {
                throw it
            }

            Result.Success(
                SpinOutcome(
                    selectedSegment = selectedSegment,
                    spinResult = spinResult,
                    newSelectionAlgorithm = algorithm.getName()
                )
            )
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Calculate the final angle based on segment position.
     * Business logic: abstracted from UI layer.
     * Uses segment weights to compute sweep sizes and returns the midpoint of the chosen segment
     * as an angle in degrees measured clockwise from the top (0..360).
     */
    private fun calculateFinalAngle(
        segments: List<Segment>,
        selectedSegment: Segment
    ): Float {
        val selectedIndex = segments.indexOfFirst { it.id == selectedSegment.id }
        if (selectedIndex < 0) return 0f

        // Compute weighted sweeps
        val totalWeight = segments.sumOf { it.weight.toDouble() }.toFloat().coerceAtLeast(0.0001f)
        var start = 0f
        var midpoint = 0f
        for ((index, segment) in segments.withIndex()) {
            val sweep = (segment.weight / totalWeight) * 360f
            if (index == selectedIndex) {
                midpoint = start + sweep / 2f
                break
            }
            start += sweep
        }

        // Push slightly toward the center to avoid exact boundary hits.
        val sweepOfSelected = (segments[selectedIndex].weight / totalWeight) * 360f
        val epsilon = (sweepOfSelected * 0.02f).coerceAtMost(2f) // at most 2 degrees or 2% of sweep
        val finalAngle = (midpoint + epsilon) % 360f
        return (finalAngle + 360f) % 360f
    }

    /**
     * Data class for spin operation outcome.
     */
    data class SpinOutcome(
        val selectedSegment: Segment,
        val spinResult: SpinResult,
        val newSelectionAlgorithm: String
    )
}

/**
 * Use case for getting spin history for a wheel.
 */
class GetSpinHistoryUseCase(private val spinHistoryRepository: SpinHistoryRepository) {
    operator fun invoke(wheelId: String): kotlinx.coroutines.flow.Flow<Result<List<SpinResult>>> =
        spinHistoryRepository.getSpinHistoryForWheel(wheelId)
}

/**
 * Use case for getting recent spins.
 */
class GetRecentSpinsUseCase(private val spinHistoryRepository: SpinHistoryRepository) {
    operator fun invoke(wheelId: String, limit: Int = 10): kotlinx.coroutines.flow.Flow<Result<List<SpinResult>>> =
        spinHistoryRepository.getRecentSpins(wheelId, limit)
}

/**
 * Use case for clearing spin history.
 */
class ClearSpinHistoryUseCase(private val spinHistoryRepository: SpinHistoryRepository) {
    suspend operator fun invoke(wheelId: String): Result<Unit> =
        spinHistoryRepository.clearWheelHistory(wheelId)
}
