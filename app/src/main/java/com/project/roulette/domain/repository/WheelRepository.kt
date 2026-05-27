package com.project.roulette.domain.repository

import kotlinx.coroutines.flow.Flow
import com.project.roulette.domain.model.Result
import com.project.roulette.domain.model.Wheel

/**
 * Repository interface for Wheel management.
 * Defines abstract data operations for wheels so the domain layer
 * remains independent of concrete data sources.
 */
interface WheelRepository {
    /**
     * Get all wheels as a reactive stream.
     */
    fun getAllWheels(): Flow<Result<List<Wheel>>>

    /**
     * Get a specific wheel by ID as a reactive stream.
     */
    fun getWheelById(wheelId: String): Flow<Result<Wheel>>

    /**
     * Create a new wheel. Returns Result with created wheel ID on success.
     */
    suspend fun createWheel(wheel: Wheel): Result<String>

    /**
     * Update an existing wheel.
     */
    suspend fun updateWheel(wheel: Wheel): Result<Unit>

    /**
     * Delete a wheel by its ID.
     */
    suspend fun deleteWheel(wheelId: String): Result<Unit>

    /**
     * Search wheels by name (case-insensitive). Emits matching wheels as a Flow.
     */
    fun searchWheels(query: String): Flow<Result<List<Wheel>>>

    /**
     * Check if a wheel with the given ID exists.
     */
    suspend fun wheelExists(wheelId: String): Boolean

    /**
     * Update favorite status of a wheel.
     */
    suspend fun updateFavorite(wheelId: String, isFavorite: Boolean): Result<Unit>
}
