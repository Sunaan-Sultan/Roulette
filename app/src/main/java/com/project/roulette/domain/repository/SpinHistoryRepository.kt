package com.project.roulette.domain.repository

import com.project.roulette.domain.model.SpinResult
import com.project.roulette.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Spin History management.
 * Separated responsibility: only handles spin records.
 * Single Responsibility Principle in action.
 */
interface SpinHistoryRepository {
    /**
     * Get all spins for a specific wheel
     */
    fun getSpinHistoryForWheel(wheelId: String): Flow<Result<List<SpinResult>>>

    /**
     * Get recent spins (last N spins)
     */
    fun getRecentSpins(wheelId: String, limit: Int): Flow<Result<List<SpinResult>>>

    /**
     * Record a new spin
     */
    suspend fun recordSpin(spinResult: SpinResult): Result<String>

    /**
     * Delete all history for a wheel
     */
    suspend fun clearWheelHistory(wheelId: String): Result<Unit>

    /**
     * Get spin count for a specific segment
     */
    suspend fun getSegmentSpinCount(wheelId: String, segmentId: String): Int

    /**
     * Get total spin count for a wheel
     */
    suspend fun getTotalSpinCount(wheelId: String): Int
}

