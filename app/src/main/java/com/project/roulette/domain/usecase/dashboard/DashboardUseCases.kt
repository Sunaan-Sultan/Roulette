package com.project.roulette.domain.usecase.dashboard

import com.project.roulette.domain.model.SpinResult
import com.project.roulette.domain.repository.SpinHistoryRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for the latest spins across every wheel, newest first.
 */
class GetGlobalRecentSpinsUseCase(private val spinHistoryRepository: SpinHistoryRepository) {
    operator fun invoke(limit: Int = 40): Flow<List<SpinResult>> =
        spinHistoryRepository.getRecentSpinsGlobal(limit)
}

/**
 * Use case for every spin recorded within a rolling window, used to build activity charts.
 */
class GetSpinsInWindowUseCase(private val spinHistoryRepository: SpinHistoryRepository) {
    operator fun invoke(since: Long): Flow<List<SpinResult>> =
        spinHistoryRepository.getSpinsSince(since)
}
