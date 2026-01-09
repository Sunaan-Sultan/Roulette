package com.project.roulette.data.mapper

import com.project.roulette.data.local.database.entity.SpinHistoryEntity
import com.project.roulette.domain.model.SpinResult
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mapper for converting between SpinHistory entities and domain models.
 */
@Singleton
class SpinHistoryMapper @Inject constructor() {
    fun entityToSpinResult(entity: SpinHistoryEntity): SpinResult {
        return SpinResult(
            id = entity.id,
            wheelId = entity.wheelId,
            selectedSegmentId = entity.selectedSegmentId,
            selectedSegmentName = entity.selectedSegmentName,
            spinTimestamp = Instant.fromEpochMilliseconds(entity.spinTimestamp),
            spinDuration = entity.spinDuration,
            finalAngle = entity.finalAngle
        )
    }

    fun spinResultToEntity(spinResult: SpinResult): SpinHistoryEntity {
        return SpinHistoryEntity(
            id = spinResult.id,
            wheelId = spinResult.wheelId,
            selectedSegmentId = spinResult.selectedSegmentId,
            selectedSegmentName = spinResult.selectedSegmentName,
            spinTimestamp = spinResult.spinTimestamp.toEpochMilliseconds(),
            spinDuration = spinResult.spinDuration,
            finalAngle = spinResult.finalAngle
        )
    }
}

