package com.project.roulette.data.repository

import com.project.roulette.data.local.database.WheelDao
import com.project.roulette.data.mapper.WheelMapper
import com.project.roulette.domain.model.Result
import com.project.roulette.domain.model.Wheel
import com.project.roulette.domain.repository.WheelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of WheelRepository using Room database.
 * Uses WheelDao for data access and WheelMapper to convert between
 * entity and domain models.
 */
class WheelRepositoryImpl @Inject constructor(
    private val wheelDao: WheelDao,
    private val wheelMapper: WheelMapper
) : WheelRepository {

    override fun getAllWheels(): Flow<Result<List<Wheel>>> =
        wheelDao.getAllWheels()
            .map { entities ->
                @Suppress("UNCHECKED_CAST")
                (Result.Success(entities.map { wheelMapper.entityToWheel(it) }) as Result<List<Wheel>>)
            }
            .catch { e ->
                emit(Result.Error(Exception("Failed to fetch wheels: ${e.message}", e)))
            }

    override fun getWheelById(wheelId: String): Flow<Result<Wheel>> =
        wheelDao.getWheelById(wheelId)
            .map { entity ->
                if (entity != null) {
                    Result.Success(wheelMapper.entityToWheel(entity))
                } else {
                    Result.Error(Exception("Wheel not found: $wheelId"))
                }
            }
            .catch { e ->
                emit(Result.Error(Exception("Failed to fetch wheel: ${e.message}", e)))
            }

    override suspend fun createWheel(wheel: Wheel): Result<String> = try {
        wheelDao.insertWheel(wheelMapper.wheelToEntity(wheel))
        Result.Success(wheel.id)
    } catch (e: Exception) {
        Result.Error(Exception("Failed to create wheel: ${e.message}", e))
    }

    override suspend fun updateWheel(wheel: Wheel): Result<Unit> = try {
        wheelDao.updateWheel(wheelMapper.wheelToEntity(wheel))
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(Exception("Failed to update wheel: ${e.message}", e))
    }

    override suspend fun deleteWheel(wheelId: String): Result<Unit> = try {
        wheelDao.deleteWheelById(wheelId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(Exception("Failed to delete wheel: ${e.message}", e))
    }

    override fun searchWheels(query: String): Flow<Result<List<Wheel>>> =
        wheelDao.searchWheels(query)
            .map { entities ->
                @Suppress("UNCHECKED_CAST")
                (Result.Success(entities.map { wheelMapper.entityToWheel(it) }) as Result<List<Wheel>>)
            }
            .catch { e ->
                emit(Result.Error(Exception("Failed to search wheels: ${e.message}", e)))
            }

    override suspend fun wheelExists(wheelId: String): Boolean =
        wheelDao.wheelExists(wheelId)

    override suspend fun updateFavorite(wheelId: String, isFavorite: Boolean): Result<Unit> = try {
        wheelDao.updateFavorite(wheelId, isFavorite)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(Exception("Failed to update favorite: ${e.message}", e))
    }
}
