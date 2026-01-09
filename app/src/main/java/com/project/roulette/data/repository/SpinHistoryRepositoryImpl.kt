package com.project.roulette.data.repository

import com.project.roulette.data.local.database.SpinHistoryDao
import com.project.roulette.data.mapper.SpinHistoryMapper
import com.project.roulette.domain.model.Result
import com.project.roulette.domain.model.SpinResult
import com.project.roulette.domain.repository.SpinHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of SpinHistoryRepository using Room database.
 */
class SpinHistoryRepositoryImpl @Inject constructor(
    private val spinHistoryDao: SpinHistoryDao,
    private val spinHistoryMapper: SpinHistoryMapper
) : SpinHistoryRepository {

    override fun getSpinHistoryForWheel(wheelId: String): Flow<Result<List<SpinResult>>> =
        flow {
            try {
                spinHistoryDao.getSpinsForWheel(wheelId).collect { entities ->
                    val results = entities.map { spinHistoryMapper.entityToSpinResult(it) }
                    emit(Result.Success(results))
                }
            } catch (e: Exception) {
                emit(Result.Error(Exception("Failed to fetch spin history: ${e.message}", e)))
            }
        }

    override fun getRecentSpins(wheelId: String, limit: Int): Flow<Result<List<SpinResult>>> =
        flow {
            try {
                spinHistoryDao.getRecentSpins(wheelId, limit).collect { entities ->
                    val results = entities.map { spinHistoryMapper.entityToSpinResult(it) }
                    emit(Result.Success(results))
                }
            } catch (e: Exception) {
                emit(Result.Error(Exception("Failed to fetch recent spins: ${e.message}", e)))
            }
        }

    override suspend fun recordSpin(spinResult: SpinResult): Result<String> = try {
        spinHistoryDao.insertSpin(spinHistoryMapper.spinResultToEntity(spinResult))
        Result.Success(spinResult.id)
    } catch (e: Exception) {
        Result.Error(Exception("Failed to record spin: ${e.message}", e))
    }

    override suspend fun clearWheelHistory(wheelId: String): Result<Unit> = try {
        spinHistoryDao.clearWheelHistory(wheelId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(Exception("Failed to clear history: ${e.message}", e))
    }

    override suspend fun getSegmentSpinCount(wheelId: String, segmentId: String): Int =
        spinHistoryDao.getSegmentSpinCount(wheelId, segmentId)

    override suspend fun getTotalSpinCount(wheelId: String): Int =
        spinHistoryDao.getTotalSpinCount(wheelId)
}
