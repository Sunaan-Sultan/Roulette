package com.project.roulette.data.repository

import com.project.roulette.data.local.database.SpinHistoryDao
import com.project.roulette.domain.model.Result
import com.project.roulette.domain.model.WheelStatistics
import com.project.roulette.domain.repository.SpinHistoryRepository
import com.project.roulette.domain.repository.StatisticsRepository
import com.project.roulette.domain.repository.WheelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of StatisticsRepository.
 * Demonstrates composition: combines data from multiple repositories
 * to compute statistics.
 */
class StatisticsRepositoryImpl @Inject constructor(
    private val spinHistoryRepository: SpinHistoryRepository,
    private val wheelRepository: WheelRepository,
    private val spinHistoryDao: SpinHistoryDao
) : StatisticsRepository {

    override fun getWheelStatistics(wheelId: String): Flow<Result<WheelStatistics>> =
        spinHistoryRepository.getSpinHistoryForWheel(wheelId)
            .combine(wheelRepository.getWheelById(wheelId)) { historyResult, wheelResult ->
                try {
                    if (historyResult !is Result.Success || wheelResult !is Result.Success) {
                        return@combine Result.Error(Exception("Failed to fetch required data"))
                    }

                    val spinResults = historyResult.data
                    val wheel = wheelResult.data

                    // Calculate selection counts
                    val selectionCounts = wheel.segments.associate { segment ->
                        segment.name to spinResults.count { it.selectedSegmentId == segment.id }
                    }

                    // Calculate percentages
                    val totalSpins = spinResults.size
                    val selectionPercentages = if (totalSpins > 0) {
                        selectionCounts.mapValues { (_, count) ->
                            (count.toFloat() / totalSpins) * 100f
                        }
                    } else {
                        selectionCounts.mapValues { 0f }
                    }

                    val stats = WheelStatistics(
                        wheelId = wheelId,
                        totalSpins = totalSpins,
                        selectionCounts = selectionCounts,
                        selectionPercentages = selectionPercentages,
                        mostFrequent = selectionCounts.maxByOrNull { it.value }?.key,
                        leastFrequent = selectionCounts.filterValues { it > 0 }
                            .minByOrNull { it.value }?.key
                    )

                    Result.Success(stats)
                } catch (e: Exception) {
                    Result.Error(e)
                }
            }
            .catch { e ->
                emit(Result.Error(Exception("Failed to compute statistics: ${e.message}", e)))
            }

    override fun getStatisticsForWheels(wheelIds: List<String>): Flow<Result<List<WheelStatistics>>> =
        try {
            flow {
                val allStats = mutableListOf<WheelStatistics>()
                wheelIds.forEach { wheelId ->
                    getWheelStatistics(wheelId).collect { result ->
                        if (result is Result.Success) {
                            allStats.add(result.data)
                        }
                    }
                }
                emit(Result.Success(allStats))
            }
        } catch (e: Exception) {
            flowOf(Result.Error(Exception("Failed to fetch multiple statistics: ${e.message}", e)))
        }

    override suspend fun clearWheelStatistics(wheelId: String): Result<Unit> =
        spinHistoryRepository.clearWheelHistory(wheelId)
}
