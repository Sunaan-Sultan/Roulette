package com.project.roulette.data.repository

import com.project.roulette.data.local.database.SpinHistoryDao
import com.project.roulette.data.mapper.SpinHistoryMapper
import com.project.roulette.domain.model.Result
import com.project.roulette.domain.model.SpinResult
import com.project.roulette.domain.repository.SpinHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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
        spinHistoryDao.getSpinsForWheel(wheelId)
            .map { entities ->
                val results = entities.map { spinHistoryMapper.entityToSpinResult(it) }
                @Suppress("UNCHECKED_CAST")
                (Result.Success(results) as Result<List<SpinResult>>)
            }
            .catch { e ->
                emit(Result.Error(Exception("Failed to fetch spin history: ${e.message}", e)))
            }

    override fun getRecentSpins(wheelId: String, limit: Int): Flow<Result<List<SpinResult>>> =
        spinHistoryDao.getRecentSpins(wheelId, limit)
            .map { entities ->
                val results = entities.map { spinHistoryMapper.entityToSpinResult(it) }
                @Suppress("UNCHECKED_CAST")
                (Result.Success(results) as Result<List<SpinResult>>)
            }
            .catch { e ->
                emit(Result.Error(Exception("Failed to fetch recent spins: ${e.message}", e)))
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

    override fun getGlobalSpinCount(): Flow<Int> =
        spinHistoryDao.getGlobalSpinCountFlow()

    override fun getGlobalSpinsTodayCount(): Flow<Int> {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        return spinHistoryDao.getSpinsTodayCountFlow(startOfDay)
    }

    override fun getAllWheelSpinCounts(): Flow<Map<String, Int>> =
        spinHistoryDao.getAllWheelSpinCounts().map { list ->
            list.associate { it.wheelId to it.count }
        }

    override fun getRecentSpinsGlobal(limit: Int): Flow<List<SpinResult>> =
        spinHistoryDao.getRecentSpinsGlobal(limit).map { entities ->
            entities.map { spinHistoryMapper.entityToSpinResult(it) }
        }

    override fun getSpinsSince(since: Long): Flow<List<SpinResult>> =
        spinHistoryDao.getSpinsSince(since).map { entities ->
            entities.map { spinHistoryMapper.entityToSpinResult(it) }
        }
}
