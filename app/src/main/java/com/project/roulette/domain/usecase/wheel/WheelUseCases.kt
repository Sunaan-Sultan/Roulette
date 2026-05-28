package com.project.roulette.domain.usecase.wheel

import kotlinx.coroutines.flow.Flow
import com.project.roulette.domain.repository.WheelRepository
import com.project.roulette.domain.model.Result
import com.project.roulette.domain.model.Wheel

/**
 * Use case: Retrieve all wheels.
 */
class GetAllWheelsUseCase(private val wheelRepository: WheelRepository) {
    operator fun invoke(): Flow<Result<List<Wheel>>> = wheelRepository.getAllWheels()
}

/**
 * Use case: Retrieve a wheel by id.
 */
class GetWheelByIdUseCase(private val wheelRepository: WheelRepository) {
    operator fun invoke(wheelId: String): Flow<Result<Wheel>> = wheelRepository.getWheelById(wheelId)
}

/**
 * Use case: Create a new wheel.
 */
class CreateWheelUseCase(private val wheelRepository: WheelRepository) {
    suspend operator fun invoke(wheel: Wheel): Result<String> = wheelRepository.createWheel(wheel)
}

/**
 * Use case: Update an existing wheel.
 */
class UpdateWheelUseCase(private val wheelRepository: WheelRepository) {
    suspend operator fun invoke(wheel: Wheel): Result<Unit> = wheelRepository.updateWheel(wheel)
}

/**
 * Use case: Delete a wheel by id.
 */
class DeleteWheelUseCase(private val wheelRepository: WheelRepository) {
    suspend operator fun invoke(wheelId: String): Result<Unit> = wheelRepository.deleteWheel(wheelId)
}

/**
 * Use case: Search wheels by query string.
 */
class SearchWheelsUseCase(private val wheelRepository: WheelRepository) {
    operator fun invoke(query: String): Flow<Result<List<Wheel>>> = wheelRepository.searchWheels(query)
}

/**
 * Use case: Update favorite status of a wheel.
 */
class ToggleFavoriteUseCase(private val wheelRepository: WheelRepository) {
    suspend operator fun invoke(wheelId: String, isFavorite: Boolean): Result<Unit> =
        wheelRepository.updateFavorite(wheelId, isFavorite)
}

/**
 * Use case: Get global statistics for all wheels.
 */
class GetGlobalStatsUseCase(
    private val wheelRepository: WheelRepository,
    private val spinHistoryRepository: com.project.roulette.domain.repository.SpinHistoryRepository
) {
    suspend operator fun invoke(): GlobalStats {
        val wheelsCount = try {
            // This is a bit suboptimal but works for now. 
            // Better to have a dedicated count query in repository.
            // For now let's just use a fixed value or try to get it if easy.
            0 // Default
        } catch (e: Exception) { 0 }
        
        return GlobalStats(
            totalWheels = 0, // Will be updated in ViewModel from wheels list
            totalSpins = spinHistoryRepository.getGlobalSpinCount(),
            spinsToday = spinHistoryRepository.getGlobalSpinsTodayCount()
        )
    }
}

data class GlobalStats(
    val totalWheels: Int,
    val totalSpins: Int,
    val spinsToday: Int
)
