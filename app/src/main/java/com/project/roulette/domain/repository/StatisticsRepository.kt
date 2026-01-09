package com.project.roulette.domain.repository

import com.project.roulette.domain.model.WheelStatistics
import com.project.roulette.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Statistics management.
 * Separated responsibility: analytics and statistics computation.
 */
interface StatisticsRepository {
    /**
     * Get comprehensive statistics for a wheel
     */
    fun getWheelStatistics(wheelId: String): Flow<Result<WheelStatistics>>

    /**
     * Get statistics for multiple wheels
     */
    fun getStatisticsForWheels(wheelIds: List<String>): Flow<Result<List<WheelStatistics>>>

    /**
     * Clear statistics for a wheel
     */
    suspend fun clearWheelStatistics(wheelId: String): Result<Unit>
}

