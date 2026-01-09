package com.project.roulette.domain.usecase.statistics

import com.project.roulette.domain.model.Result
import com.project.roulette.domain.model.WheelStatistics
import com.project.roulette.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving wheel statistics.
 */
class GetWheelStatisticsUseCase(private val statisticsRepository: StatisticsRepository) {
    operator fun invoke(wheelId: String): Flow<Result<WheelStatistics>> =
        statisticsRepository.getWheelStatistics(wheelId)
}

/**
 * Use case for clearing wheel statistics.
 */
class ClearStatisticsUseCase(private val statisticsRepository: StatisticsRepository) {
    suspend operator fun invoke(wheelId: String): Result<Unit> =
        statisticsRepository.clearWheelStatistics(wheelId)
}

