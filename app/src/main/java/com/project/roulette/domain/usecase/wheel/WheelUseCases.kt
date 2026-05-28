package com.project.roulette.domain.usecase.wheel

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import com.project.roulette.domain.repository.WheelRepository
import com.project.roulette.domain.model.Result
import com.project.roulette.domain.model.Wheel
import com.project.roulette.domain.model.Notification
import com.project.roulette.domain.model.NotificationType
import com.project.roulette.domain.repository.NotificationRepository
import java.util.UUID
import kotlinx.datetime.Clock

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
class CreateWheelUseCase(
    private val wheelRepository: WheelRepository,
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(wheel: Wheel): Result<String> {
        val result = wheelRepository.createWheel(wheel)
        if (result is Result.Success) {
            if (wheel.segments.size < 3) {
                notificationRepository.addNotification(
                    Notification(
                        id = UUID.randomUUID().toString(),
                        title = "Tip 💡",
                        message = "Try adding more segments to ${wheel.name} for more variety!",
                        type = NotificationType.TIP,
                        timestamp = Clock.System.now(),
                        targetId = result.data
                    )
                )
            }
        }
        return result
    }
}

/**
 * Use case: Update an existing wheel.
 */
class UpdateWheelUseCase(
    private val wheelRepository: WheelRepository,
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(wheel: Wheel): Result<Unit> {
        val result = wheelRepository.updateWheel(wheel)
        if (result is Result.Success) {
            notificationRepository.addNotification(
                Notification(
                    id = UUID.randomUUID().toString(),
                    title = "Wheel updated 🛠️",
                    message = "The wheel \"${wheel.name}\" was edited.",
                    type = NotificationType.UPDATE,
                    timestamp = Clock.System.now(),
                    targetId = wheel.id
                )
            )
        }
        return result
    }
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
class ToggleFavoriteUseCase(
    private val wheelRepository: WheelRepository,
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(wheelId: String, isFavorite: Boolean): Result<Unit> {
        val result = wheelRepository.updateFavorite(wheelId, isFavorite)
        if (result is Result.Success && isFavorite) {
            val wheelResult = wheelRepository.getWheelById(wheelId).first()
            if (wheelResult is Result.Success) {
                notificationRepository.addNotification(
                    Notification(
                        id = UUID.randomUUID().toString(),
                        title = "Favourite activity ⭐",
                        message = "You starred ${wheelResult.data.name}",
                        type = NotificationType.FAVOURITE,
                        timestamp = Clock.System.now(),
                        targetId = wheelId
                    )
                )
            }
        }
        return result
    }
}

/**
 * Use case: Get global statistics for all wheels.
 */
class GetGlobalStatsUseCase(
    private val wheelRepository: WheelRepository,
    private val spinHistoryRepository: com.project.roulette.domain.repository.SpinHistoryRepository
) {
    operator fun invoke(): Flow<GlobalStats> {
        return combine(
            spinHistoryRepository.getGlobalSpinCount(),
            spinHistoryRepository.getGlobalSpinsTodayCount()
        ) { total, today ->
            GlobalStats(
                totalWheels = 0, // ViewModel handles this
                totalSpins = total,
                spinsToday = today
            )
        }
    }

    fun getSpinCounts(): Flow<Map<String, Int>> = spinHistoryRepository.getAllWheelSpinCounts()
}

data class GlobalStats(
    val totalWheels: Int,
    val totalSpins: Int,
    val spinsToday: Int
)
